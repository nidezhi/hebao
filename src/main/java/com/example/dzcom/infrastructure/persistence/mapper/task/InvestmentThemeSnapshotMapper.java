package com.example.dzcom.infrastructure.persistence.mapper.task;

import com.example.dzcom.infrastructure.persistence.entity.task.InvestmentThemeSnapshotEntity;
import org.apache.ibatis.annotations.Mapper;

/** 投资主题快照 MyBatis Mapper。 */
@Mapper
public interface InvestmentThemeSnapshotMapper {
    /** 新增主题快照。 */
    int insert(InvestmentThemeSnapshotEntity entity);
}
