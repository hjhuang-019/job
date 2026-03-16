package com.example.jobplatform.mapper;

import com.example.jobplatform.entity.FavoriteJob;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FavoriteJobMapper {

    FavoriteJob selectById(Long id);

    List<FavoriteJob> selectByJobSeekerUserId(Long jobSeekerUserId);

    List<FavoriteJob> selectAll();

    int insert(FavoriteJob favoriteJob);

    int deleteById(Long id);
}
