<template>
  <div class="app-shell">
    <a class="skip-link" href="#main-content">跳转到主要内容</a>

    <header class="app-header">
      <div class="container header-content">
        <div>
          <h1 class="site-title">残疾人就业 Web 平台</h1>
          <p class="site-subtitle">面向求职者、企业与管理员的就业服务基础骨架</p>
        </div>
        <nav aria-label="主导航">
          <RouterLink class="nav-link" to="/">首页</RouterLink>
        </nav>
      </div>
    </header>

    <main id="main-content" class="app-main container" tabindex="-1">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
</script>
