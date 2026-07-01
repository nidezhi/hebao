package com.example.dzcom.infrastructure.persistence.mapper.ai;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelCallAuditEntity;
import org.apache.ibatis.annotations.Mapper;

/** AI 模型调用审计 MyBatis-Plus Mapper。 */
@Mapper
public interface AiModelCallAuditMapper extends BaseMapper<AiModelCallAuditEntity> {
}
