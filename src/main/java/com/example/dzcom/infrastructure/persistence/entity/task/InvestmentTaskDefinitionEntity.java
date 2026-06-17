package com.example.dzcom.infrastructure.persistence.entity.task;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 投资定时任务定义持久化实体。 */
@TableName("aiw_investment_task_definition")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InvestmentTaskDefinitionEntity {
    @TableId(value = "biz_id", type = IdType.INPUT)
    private String bizId;
    private String taskCode;
    private String taskType;
    private String cron;
    private String zone;
    private boolean enabled;
    private String parameters;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
