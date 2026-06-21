package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.domain.repository.task.NewsArticleRelationSearchCriteria;
import com.example.dzcom.infrastructure.persistence.entity.task.NewsArticleRelationEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 根据条件分页查询资讯主题产品关联。
     *
     * @param criteria 关联查询条件
     * @param offset 分页偏移量
     * @param sortColumn 排序数据库列白名单结果
     * @return 关联持久化实体列表
     * @author dz
     * @date 2026-06-21
     */
    List<NewsArticleRelationEntity> search(
        @Param("criteria") NewsArticleRelationSearchCriteria criteria,
        @Param("offset") int offset,
        @Param("sortColumn") String sortColumn
    );

    /**
     * 统计符合筛选条件的资讯主题产品关联数量。
     *
     * @param criteria 关联查询条件
     * @return 符合条件的总条数
     * @author dz
     * @date 2026-06-21
     */
    long count(@Param("criteria") NewsArticleRelationSearchCriteria criteria);
}
