package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.infrastructure.persistence.entity.task.NewsArticleRelationEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/** 资讯主题产品关联 MyBatis Mapper。 */
@Mapper
public interface NewsArticleRelationMapper {

    /**
     * 批量新增或更新资讯主题产品关联。
     *
     * @param entities 关联持久化实体集合
     * @return 影响行数
     * @author dz
     * @date 2026-06-21
     */
    int saveBatch(List<NewsArticleRelationEntity> entities);
}
