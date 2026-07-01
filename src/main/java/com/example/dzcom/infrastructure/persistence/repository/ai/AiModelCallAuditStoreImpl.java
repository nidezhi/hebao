package com.example.dzcom.infrastructure.persistence.repository.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.ai.AiModelCallAudit;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditStore;
import com.example.dzcom.infrastructure.persistence.entity.ai.AiModelCallAuditEntity;
import com.example.dzcom.infrastructure.persistence.mapper.ai.AiModelCallAuditMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** AI 模型调用审计仓储实现，使用 MyBatis-Plus 标准 Mapper 和分页能力。 */
@Repository
@RequiredArgsConstructor
public class AiModelCallAuditStoreImpl implements AiModelCallAuditStore {
    private final AiModelCallAuditMapper mapper;

    /**
     * 保存模型调用审计记录。
     *
     * <p>审计记录以 {@code bizId} 为主键、{@code callId} 为调用追踪键；开始记录与结束记录
     * 通过同一领域对象更新，保证一次模型调用只沉淀一条流水。</p>
     *
     * @param audit 待保存的模型调用审计领域对象
     * @return 保存后的领域对象
     */
    @Override
    public AiModelCallAudit save(AiModelCallAudit audit) {
        AiModelCallAuditEntity entity = toEntity(audit);
        if (mapper.selectById(audit.bizId()) == null) {
            mapper.insert(entity);
        } else {
            mapper.updateById(entity);
        }
        return audit;
    }

    /**
     * 根据审计业务标识查询详情。
     *
     * @param bizId 审计记录业务唯一标识
     * @return 存在时返回完整审计记录
     */
    @Override
    public Optional<AiModelCallAudit> findByBizId(String bizId) {
        return Optional.ofNullable(mapper.selectById(bizId)).map(this::toDomain);
    }

    /**
     * 根据模型调用追踪标识查询审计详情。
     *
     * <p>{@code callId} 建有唯一索引，查询只依赖 MyBatis-Plus 条件构造器，不追加手写
     * limit SQL。</p>
     *
     * @param callId 单次模型调用追踪标识
     * @return 存在时返回完整审计记录
     */
    @Override
    public Optional<AiModelCallAudit> findByCallId(String callId) {
        return Optional.ofNullable(mapper.selectOne(new LambdaQueryWrapper<AiModelCallAuditEntity>()
            .eq(AiModelCallAuditEntity::getCallId, callId))).map(this::toDomain);
    }

    /**
     * 按模型、业务对象、Prompt/Skill 等维度分页查询调用审计。
     *
     * <p>分页通过 {@link Page} 与 MyBatis-Plus 分页拦截器完成，避免仓储层拼接 SQL。
     * 排序字段由应用服务白名单收敛后传入。</p>
     *
     * @param criteria 审计查询条件和分页排序信息
     * @return 分页后的审计记录
     */
    @Override
    public PageResult<AiModelCallAudit> search(AiModelCallAuditSearchCriteria criteria) {
        LambdaQueryWrapper<AiModelCallAuditEntity> wrapper = wrapper(criteria);
        selectListColumns(wrapper);
        applySort(wrapper, criteria.sort(), criteria.asc());
        IPage<AiModelCallAuditEntity> page = mapper.selectPage(
            Page.of(criteria.page(), criteria.size(), true),
            wrapper
        );
        List<AiModelCallAudit> items = page.getRecords().stream().map(this::toDomain).toList();
        return PageResult.<AiModelCallAudit>builder()
            .items(items)
            .total(page.getTotal())
            .page(criteria.page())
            .size(criteria.size())
            .totalPages((int) page.getPages())
            .build();
    }

    /**
     * 组装审计查询条件。
     *
     * @param criteria 应用层已归一化的筛选条件
     * @return MyBatis-Plus Lambda 查询包装器
     */
    private LambdaQueryWrapper<AiModelCallAuditEntity> wrapper(AiModelCallAuditSearchCriteria criteria) {
        LambdaQueryWrapper<AiModelCallAuditEntity> wrapper = new LambdaQueryWrapper<>();
        eq(wrapper, AiModelCallAuditEntity::getOperationCode, criteria.operationCode());
        eq(wrapper, AiModelCallAuditEntity::getCallStatus, criteria.callStatus());
        eq(wrapper, AiModelCallAuditEntity::getProviderCode, criteria.providerCode());
        eq(wrapper, AiModelCallAuditEntity::getModelCode, criteria.modelCode());
        eq(wrapper, AiModelCallAuditEntity::getModelVersion, criteria.modelVersion());
        eq(wrapper, AiModelCallAuditEntity::getBusinessType, criteria.businessType());
        eq(wrapper, AiModelCallAuditEntity::getBusinessBizId, criteria.businessBizId());
        eq(wrapper, AiModelCallAuditEntity::getTaskCode, criteria.taskCode());
        eq(wrapper, AiModelCallAuditEntity::getEventId, criteria.eventId());
        eq(wrapper, AiModelCallAuditEntity::getRunBizId, criteria.runBizId());
        eq(wrapper, AiModelCallAuditEntity::getRunNo, criteria.runNo());
        eq(wrapper, AiModelCallAuditEntity::getReportBizId, criteria.reportBizId());
        eq(wrapper, AiModelCallAuditEntity::getPromptCode, criteria.promptCode());
        eq(wrapper, AiModelCallAuditEntity::getSkillCode, criteria.skillCode());
        eq(wrapper, AiModelCallAuditEntity::getScenarioCode, criteria.scenarioCode());
        eq(wrapper, AiModelCallAuditEntity::getEnvironment, criteria.environment());
        return wrapper;
    }

    /**
     * 选择列表页需要的轻量字段。
     *
     * <p>完整输入输出 payload 只在详情查询返回，避免审计列表随着大模型上下文膨胀而变慢。</p>
     *
     * @param wrapper 查询包装器
     */
    private void selectListColumns(LambdaQueryWrapper<AiModelCallAuditEntity> wrapper) {
        wrapper.select(
            AiModelCallAuditEntity::getBizId,
            AiModelCallAuditEntity::getCallId,
            AiModelCallAuditEntity::getOperationCode,
            AiModelCallAuditEntity::getCallStatus,
            AiModelCallAuditEntity::getProviderCode,
            AiModelCallAuditEntity::getModelCode,
            AiModelCallAuditEntity::getModelVersion,
            AiModelCallAuditEntity::getRemoteModel,
            AiModelCallAuditEntity::getEndpoint,
            AiModelCallAuditEntity::getHttpMethod,
            AiModelCallAuditEntity::getHttpStatus,
            AiModelCallAuditEntity::getDurationMs,
            AiModelCallAuditEntity::getSystemPromptHash,
            AiModelCallAuditEntity::getUserPromptHash,
            AiModelCallAuditEntity::getResponseHash,
            AiModelCallAuditEntity::getRequestPreview,
            AiModelCallAuditEntity::getResponsePreview,
            AiModelCallAuditEntity::getInputSummary,
            AiModelCallAuditEntity::getOutputSummary,
            AiModelCallAuditEntity::getBusinessType,
            AiModelCallAuditEntity::getBusinessBizId,
            AiModelCallAuditEntity::getBusinessLabel,
            AiModelCallAuditEntity::getTaskCode,
            AiModelCallAuditEntity::getEventId,
            AiModelCallAuditEntity::getRunBizId,
            AiModelCallAuditEntity::getRunNo,
            AiModelCallAuditEntity::getReportBizId,
            AiModelCallAuditEntity::getPromptBizId,
            AiModelCallAuditEntity::getPromptCode,
            AiModelCallAuditEntity::getPromptVersion,
            AiModelCallAuditEntity::getSkillBizId,
            AiModelCallAuditEntity::getSkillCode,
            AiModelCallAuditEntity::getSkillVersion,
            AiModelCallAuditEntity::getModelSkillBindingBizId,
            AiModelCallAuditEntity::getScenarioCode,
            AiModelCallAuditEntity::getEnvironment,
            AiModelCallAuditEntity::getErrorCode,
            AiModelCallAuditEntity::getErrorMessage,
            AiModelCallAuditEntity::getCreatedAt,
            AiModelCallAuditEntity::getUpdatedAt
        );
    }

    /**
     * 应用审计列表排序。
     *
     * @param wrapper 查询包装器
     * @param sort 白名单排序字段
     * @param asc 是否升序
     */
    private void applySort(LambdaQueryWrapper<AiModelCallAuditEntity> wrapper, String sort, boolean asc) {
        switch (sort) {
            case "durationMs" -> wrapper.orderBy(true, asc, AiModelCallAuditEntity::getDurationMs);
            case "operationCode" -> wrapper.orderBy(true, asc, AiModelCallAuditEntity::getOperationCode);
            case "callStatus" -> wrapper.orderBy(true, asc, AiModelCallAuditEntity::getCallStatus);
            case "modelCode" -> wrapper.orderBy(true, asc, AiModelCallAuditEntity::getModelCode);
            default -> wrapper.orderBy(true, asc, AiModelCallAuditEntity::getCreatedAt);
        }
    }

    /**
     * 按非空值追加等值查询条件。
     *
     * @param wrapper 查询包装器
     * @param column 实体字段引用
     * @param value 待匹配的查询值
     * @param <T> 字段类型
     */
    private <T> void eq(LambdaQueryWrapper<AiModelCallAuditEntity> wrapper,
                       com.baomidou.mybatisplus.core.toolkit.support.SFunction<AiModelCallAuditEntity, T> column,
                       T value) {
        if (value instanceof String text && text.isBlank()) {
            return;
        }
        if (value != null) {
            wrapper.eq(column, value);
        }
    }

    /**
     * 将领域审计对象转换为持久化实体。
     *
     * @param audit 模型调用审计领域对象
     * @return 可由 MyBatis-Plus 持久化的实体
     */
    private AiModelCallAuditEntity toEntity(AiModelCallAudit audit) {
        return AiModelCallAuditEntity.builder()
            .bizId(audit.bizId())
            .callId(audit.callId())
            .operationCode(audit.operationCode())
            .callStatus(audit.callStatus())
            .providerCode(audit.providerCode())
            .modelCode(audit.modelCode())
            .modelVersion(audit.modelVersion())
            .remoteModel(audit.remoteModel())
            .endpoint(audit.endpoint())
            .httpMethod(audit.httpMethod())
            .httpStatus(audit.httpStatus())
            .durationMs(audit.durationMs())
            .systemPromptHash(audit.systemPromptHash())
            .userPromptHash(audit.userPromptHash())
            .responseHash(audit.responseHash())
            .requestPreview(audit.requestPreview())
            .responsePreview(audit.responsePreview())
            .requestPayload(audit.requestPayload())
            .responsePayload(audit.responsePayload())
            .inputSummary(audit.inputSummary())
            .outputSummary(audit.outputSummary())
            .businessType(audit.businessType())
            .businessBizId(audit.businessBizId())
            .businessLabel(audit.businessLabel())
            .taskCode(audit.taskCode())
            .eventId(audit.eventId())
            .runBizId(audit.runBizId())
            .runNo(audit.runNo())
            .reportBizId(audit.reportBizId())
            .promptBizId(audit.promptBizId())
            .promptCode(audit.promptCode())
            .promptVersion(audit.promptVersion())
            .skillBizId(audit.skillBizId())
            .skillCode(audit.skillCode())
            .skillVersion(audit.skillVersion())
            .modelSkillBindingBizId(audit.modelSkillBindingBizId())
            .scenarioCode(audit.scenarioCode())
            .environment(audit.environment())
            .errorCode(audit.errorCode())
            .errorMessage(audit.errorMessage())
            .createdAt(audit.createdAt())
            .updatedAt(audit.updatedAt())
            .build();
    }

    /**
     * 将持久化实体转换为领域审计对象。
     *
     * @param entity MyBatis-Plus 查询出的审计实体
     * @return 应用层使用的审计领域对象
     */
    private AiModelCallAudit toDomain(AiModelCallAuditEntity entity) {
        return AiModelCallAudit.builder()
            .bizId(entity.getBizId())
            .callId(entity.getCallId())
            .operationCode(entity.getOperationCode())
            .callStatus(entity.getCallStatus())
            .providerCode(entity.getProviderCode())
            .modelCode(entity.getModelCode())
            .modelVersion(entity.getModelVersion())
            .remoteModel(entity.getRemoteModel())
            .endpoint(entity.getEndpoint())
            .httpMethod(entity.getHttpMethod())
            .httpStatus(entity.getHttpStatus())
            .durationMs(entity.getDurationMs())
            .systemPromptHash(entity.getSystemPromptHash())
            .userPromptHash(entity.getUserPromptHash())
            .responseHash(entity.getResponseHash())
            .requestPreview(entity.getRequestPreview())
            .responsePreview(entity.getResponsePreview())
            .requestPayload(entity.getRequestPayload())
            .responsePayload(entity.getResponsePayload())
            .inputSummary(entity.getInputSummary())
            .outputSummary(entity.getOutputSummary())
            .businessType(entity.getBusinessType())
            .businessBizId(entity.getBusinessBizId())
            .businessLabel(entity.getBusinessLabel())
            .taskCode(entity.getTaskCode())
            .eventId(entity.getEventId())
            .runBizId(entity.getRunBizId())
            .runNo(entity.getRunNo())
            .reportBizId(entity.getReportBizId())
            .promptBizId(entity.getPromptBizId())
            .promptCode(entity.getPromptCode())
            .promptVersion(entity.getPromptVersion())
            .skillBizId(entity.getSkillBizId())
            .skillCode(entity.getSkillCode())
            .skillVersion(entity.getSkillVersion())
            .modelSkillBindingBizId(entity.getModelSkillBindingBizId())
            .scenarioCode(entity.getScenarioCode())
            .environment(entity.getEnvironment())
            .errorCode(entity.getErrorCode())
            .errorMessage(entity.getErrorMessage())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}
