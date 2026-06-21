package com.example.dzcom.domain.repository.task;

import com.example.dzcom.domain.model.task.NewsArticleRelation;

import java.util.List;

/** 资讯主题产品关联仓储端口。 */
public interface NewsArticleRelationStore {

    /**
     * 批量保存资讯与主题、产品的显式关联。
     *
     * @param relations 待保存的关联集合
     * @author dz
     * @date 2026-06-21
     */
    void saveBatch(List<NewsArticleRelation> relations);
}
