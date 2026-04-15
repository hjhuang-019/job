package com.example.jobplatform.service.impl;

import com.example.jobplatform.common.BusinessException;
import com.example.jobplatform.config.DoubaoArkProperties;
import com.example.jobplatform.dto.JobSeekerAiInterpretRequest;
import com.example.jobplatform.dto.JobSeekerAiPriorTurn;
import com.example.jobplatform.service.JobSeekerAiService;
import com.example.jobplatform.vo.JobSeekerAiInterpretVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JobSeekerAiServiceImpl implements JobSeekerAiService {

    private static final Logger log = LoggerFactory.getLogger(JobSeekerAiServiceImpl.class);

    private static final String SYSTEM_PROMPT = """
            你是「残疾人就业 Web 平台」求职者端的语音助手后端大脑。用户通过语音输入，你需要理解意图并输出**唯一一段 JSON**（不要 Markdown，不要代码围栏，不要多余文字）。

            场景 scene 取值含义：
            - PROFILE：求职者个人资料页
            - RESUME：新增/编辑简历页
            - JOB_LIST：岗位列表（pageContext 可能含 jobs 摘要）
            - JOB_DETAIL：岗位详情（pageContext 含 jobId）
            - MY_APPLICATIONS：我的投递
            - RECOMMEND_JOBS：推荐岗位
            - MESSAGES：消息中心
            - VERIFY：求职者认证
            - HOME：求职者首页
            - GLOBAL：其它

            必须输出的 JSON 字段（缺失用空对象或空字符串）：
            {
              "intent": "FILL_PROFILE|FILL_RESUME|SET_JOB_FILTERS|NAVIGATE|APPLY_JOB|SUGGEST_SEARCH|SUMMARIZE_PAGE|GENERAL|CLARIFY",
              "spokenReply": "给用户听的简短中文，适合语音播报",
              "profile": {},
              "resume": {},
              "jobFilters": {},
              "navigation": { "target": "", "jobId": null },
              "apply": { "jobId": null, "resumeId": null, "resumeIndex": null, "useDefaultResume": false }
            }

            profile 仅包含能从用户话里确定的字段，键名必须严格使用：
            realName, phone, email, disabilityType, disabilityLevel, skills（字符串，多个用英文逗号分隔）,
            expectedCity, expectedSalary, expectedJob, acceptRemote（布尔）, introduction

            resume 键名：title, education, experience, projectExperience, skills, selfEvaluation, isDefault（布尔）

            jobFilters 键名：keyword, city, workMode（OFFLINE/REMOTE/HYBRID 或空字符串）, disabilitySupportType

            navigation.target 枚举（大写）：
            PROFILE, RESUME_LIST, RESUME_NEW, JOBS, JOB_DETAIL, MY_APPLICATIONS, RECOMMEND, MESSAGES, HOME, VERIFY
            若去岗位详情且已知 jobId，填 navigation.jobId（数字）。

            APPLY_JOB：仅在用户明确要投递且场景合理时设置 intent=APPLY_JOB，并在 apply 中给出 jobId（详情页可用 pageContext.jobId）、resumeId 或 useDefaultResume=true 或 resumeIndex（本页简历列表序号，从1开始）。

            不要编造用户未提供的手机号等隐私；不确定时 intent=CLARIFY 并让 spokenReply 追问。

            用户输入与页面上下文可能为中文口语，请容错。

            若提供多轮历史消息，请结合上文理解用户最新一句，仍只输出上述 JSON，不要输出多段 JSON。
            """;

    private final DoubaoArkProperties doubaoArkProperties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public JobSeekerAiServiceImpl(DoubaoArkProperties doubaoArkProperties,
                                  RestTemplate restTemplate,
                                  ObjectMapper objectMapper) {
        this.doubaoArkProperties = doubaoArkProperties;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public JobSeekerAiInterpretVO interpret(JobSeekerAiInterpretRequest request) {
        try {
            if (!StringUtils.hasText(doubaoArkProperties.getApiKey()) || !StringUtils.hasText(doubaoArkProperties.getModel())) {
                throw new BusinessException("未配置豆包方舟 API，请在服务端设置 doubao.ark.api-key 与 doubao.ark.model");
            }

            String userText = request.getUserText() == null ? "" : request.getUserText().trim();
            int maxChars = Math.max(200, doubaoArkProperties.getMaxUserChars());
            if (userText.length() > maxChars) {
                userText = userText.substring(0, maxChars);
            }

            Map<String, Object> pageContext = toMapContext(request.getPageContext());
            List<Map<String, String>> messages = buildChatMessages(request.getScene(), pageContext, userText, request.getPriorTurns());

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", doubaoArkProperties.getModel());
            body.put("temperature", 0.2);
            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(doubaoArkProperties.getApiKey().trim());

            String base = doubaoArkProperties.getBaseUrl() == null ? "" : doubaoArkProperties.getBaseUrl().trim();
            if (!StringUtils.hasText(base)) {
                base = "https://ark.cn-beijing.volces.com/api/v3";
            }
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            String url = base + "/chat/completions";

            try {
                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
                ResponseEntity<JsonNode> response = restTemplate.exchange(url, HttpMethod.POST, entity, JsonNode.class);
                JsonNode root = response.getBody();
                if (root == null) {
                    return fallback("AI 返回为空", "CLARIFY");
                }
                String content = root.path("choices").path(0).path("message").path("content").asText("");
                return parseModelJson(content);
            } catch (RestClientException ex) {
                log.warn("Doubao Ark HTTP error: {}", ex.getMessage());
                return fallback("AI 服务暂时不可用，请稍后重试", "GENERAL");
            } catch (Exception ex) {
                log.warn("Doubao Ark response handling failed", ex);
                return fallback("解析 AI 结果失败，请换一种说法试试", "CLARIFY");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Throwable t) {
            log.error("JobSeeker AI interpret failed", t);
            throw new BusinessException("智能助手处理失败，请查看服务端日志或稍后重试");
        }
    }

    private Map<String, Object> toMapContext(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || !node.isObject()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> map = objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {
            });
            return map != null ? map : Collections.emptyMap();
        } catch (IllegalArgumentException ex) {
            return Collections.emptyMap();
        }
    }

    private String assistantJsonToString(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText();
        }
        return node.toString();
    }

    private List<Map<String, String>> buildChatMessages(String scene,
                                                        Map<String, Object> pageContext,
                                                        String userText,
                                                        List<JobSeekerAiPriorTurn> priorTurns) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
        if (priorTurns != null) {
            for (JobSeekerAiPriorTurn turn : priorTurns) {
                if (turn == null || !StringUtils.hasText(turn.getUserText())) {
                    continue;
                }
                String priorUser = buildUserPayloadJson(scene, pageContext, turn.getUserText().trim());
                messages.add(Map.of("role", "user", "content", priorUser));
                String assistant = assistantJsonToString(turn.getAssistantJson());
                if (!StringUtils.hasText(assistant)) {
                    assistant = "{}";
                }
                messages.add(Map.of("role", "assistant", "content", assistant));
            }
        }
        String currentUser = buildUserPayloadJson(scene, pageContext, userText);
        messages.add(Map.of("role", "user", "content", currentUser));
        return messages;
    }

    private String buildUserPayloadJson(String scene, Map<String, Object> pageContext, String userText) {
        Map<String, Object> userPayload = new LinkedHashMap<>();
        userPayload.put("scene", scene);
        userPayload.put("userText", userText);
        userPayload.put("pageContext", pageContext == null ? Collections.emptyMap() : pageContext);
        try {
            return objectMapper.writeValueAsString(userPayload);
        } catch (Exception ex) {
            throw new BusinessException("请求参数序列化失败");
        }
    }

    private JobSeekerAiInterpretVO parseModelJson(String raw) {
        String text = raw == null ? "" : raw.trim();
        text = stripMarkdownFence(text);
        JsonNode node;
        try {
            node = objectMapper.readTree(text);
        } catch (Exception ex) {
            return fallback("我没有听清，请再说一遍", "CLARIFY");
        }
        if (!node.isObject()) {
            return fallback("我没有听清，请再说一遍", "CLARIFY");
        }

        JobSeekerAiInterpretVO vo = new JobSeekerAiInterpretVO();
        vo.setIntent(textOr(node.path("intent"), "GENERAL"));
        vo.setSpokenReply(textOr(node.path("spokenReply"), "好的。"));
        vo.setProfile(readMap(node.path("profile")));
        vo.setResume(readMap(node.path("resume")));
        vo.setJobFilters(readMap(node.path("jobFilters")));
        vo.setNavigation(readMap(node.path("navigation")));
        vo.setApply(readMap(node.path("apply")));
        return jsonSafeVo(vo);
    }

    private Map<String, Object> readMap(JsonNode node) {
        if (node == null || node.isNull() || node.isMissingNode() || !node.isObject()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> map = objectMapper.convertValue(node, new TypeReference<Map<String, Object>>() {
            });
            return map == null ? Collections.emptyMap() : map;
        } catch (IllegalArgumentException ex) {
            return Collections.emptyMap();
        }
    }

    private static String textOr(JsonNode node, String defaultValue) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return defaultValue;
        }
        String value = node.asText("");
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private static String stripMarkdownFence(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String trimmed = text.trim();
        if (trimmed.startsWith("```")) {
            int firstBreak = trimmed.indexOf('\n');
            if (firstBreak > 0) {
                trimmed = trimmed.substring(firstBreak + 1);
            } else {
                trimmed = trimmed.replaceFirst("^```", "");
            }
            int endFence = trimmed.lastIndexOf("```");
            if (endFence >= 0) {
                trimmed = trimmed.substring(0, endFence);
            }
        }
        return trimmed.trim();
    }

    private JobSeekerAiInterpretVO fallback(String spokenReply, String intent) {
        JobSeekerAiInterpretVO vo = new JobSeekerAiInterpretVO();
        vo.setIntent(intent);
        vo.setSpokenReply(spokenReply);
        vo.setProfile(new HashMap<>());
        vo.setResume(new HashMap<>());
        vo.setJobFilters(new HashMap<>());
        vo.setNavigation(new HashMap<>());
        vo.setApply(new HashMap<>());
        return jsonSafeVo(vo);
    }

    /**
     * 通过 JSON 往返剔除 Map 中无法安全序列化的类型，避免 HttpMessageNotWritableException。
     */
    private JobSeekerAiInterpretVO jsonSafeVo(JobSeekerAiInterpretVO vo) {
        if (vo == null) {
            return fallback("数据异常", "CLARIFY");
        }
        try {
            String json = objectMapper.writeValueAsString(vo);
            JobSeekerAiInterpretVO copy = objectMapper.readValue(json, JobSeekerAiInterpretVO.class);
            if (copy != null) {
                return copy;
            }
        } catch (Exception ex) {
            log.warn("VO JSON round-trip failed, returning minimal payload", ex);
        }
        return minimalInterpretVo(vo);
    }

    private JobSeekerAiInterpretVO minimalInterpretVo(JobSeekerAiInterpretVO vo) {
        JobSeekerAiInterpretVO minimal = new JobSeekerAiInterpretVO();
        minimal.setIntent(StringUtils.hasText(vo.getIntent()) ? vo.getIntent() : "GENERAL");
        minimal.setSpokenReply(vo.getSpokenReply() != null ? vo.getSpokenReply() : "");
        minimal.setProfile(new HashMap<>());
        minimal.setResume(new HashMap<>());
        minimal.setJobFilters(new HashMap<>());
        minimal.setNavigation(new HashMap<>());
        minimal.setApply(new HashMap<>());
        return minimal;
    }
}
