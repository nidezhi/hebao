package com.example.dzcom.application.service.ai;

import com.example.dzcom.application.common.json.Jsons;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.application.common.service.ClockProvider;
import com.example.dzcom.application.common.service.IdGenerator;
import com.example.dzcom.application.dto.ai.AiModelCallAuditContext;
import com.example.dzcom.application.dto.ai.AiModelCallAuditView;
import com.example.dzcom.application.dto.ai.AiModelRuntimeConfig;
import com.example.dzcom.domain.model.ai.AiModelCallAudit;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditSearchCriteria;
import com.example.dzcom.domain.repository.ai.AiModelCallAuditStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * AI 模型调用审计应用服务。
 *
 * <p>该服务为所有大模型调用提供统一埋点入口：开始时保存模型、Prompt、Skill 和业务关联，
 * 结束时补齐耗时、输出摘要和失败上下文。上层页面依赖这里提供的 display 字段做结构化展示。</p>
 */
@Service
@RequiredArgsConstructor
public class AiModelCallAuditApplicationService {
    private static final Set<String> SORTS = Set.of("createdAt", "durationMs", "operationCode", "callStatus", "modelCode");
    private static final int PREVIEW_LIMIT = 4000;

    private final AiModelCallAuditStore audits;
    private final IdGenerator ids;
    private final ClockProvider clock;

    /**
     * 创建模型调用开始审计记录。
     *
     * @param callId 单次模型调用追踪标识
     * @param operationCode 业务操作编码，用于区分投资报告、数据采集等调用来源
     * @param modelConfig 模型运行时配置快照
     * @param endpoint 脱敏后的远端调用地址
     * @param systemPromptHash 系统提示词哈希
     * @param userPromptHash 用户提示词哈希
     * @param systemPrompt 系统提示词原文，入库前会截断并仅用于审计预览
     * @param userPrompt 用户提示词原文，入库前会截断并仅用于审计预览
     * @param context 业务关联上下文
     * @return STARTED 状态的模型调用审计记录
     */
    @Transactional
    public AiModelCallAudit start(
        String callId,
        String operationCode,
        AiModelRuntimeConfig modelConfig,
        String endpoint,
        String systemPromptHash,
        String userPromptHash,
        String systemPrompt,
        String userPrompt,
        AiModelCallAuditContext context
    ) {
        LocalDateTime now = clock.now();
        AiModelCallAuditContext safeContext = context == null ? AiModelCallAuditContext.empty() : context;
        return audits.save(AiModelCallAudit.builder()
            .bizId(ids.newBizId())
            .callId(callId)
            .operationCode(trim(operationCode))
            .callStatus("STARTED")
            .providerCode(modelConfig.providerCode())
            .modelCode(modelConfig.modelCode())
            .modelVersion(modelConfig.modelVersion())
            .remoteModel(modelConfig.remoteModel())
            .endpoint(endpoint)
            .httpMethod("POST")
            .systemPromptHash(systemPromptHash)
            .userPromptHash(userPromptHash)
            .requestPreview(preview(joinPromptPreview(systemPrompt, userPrompt)))
            .inputSummary(toJson(safeContext.inputSummary()))
            .businessType(trim(safeContext.businessType()))
            .businessBizId(trim(safeContext.businessBizId()))
            .businessLabel(trim(safeContext.businessLabel()))
            .taskCode(trim(safeContext.taskCode()))
            .eventId(trim(safeContext.eventId()))
            .runBizId(trim(safeContext.runBizId()))
            .runNo(trim(safeContext.runNo()))
            .reportBizId(trim(safeContext.reportBizId()))
            .promptBizId(trim(safeContext.promptBizId()))
            .promptCode(trim(safeContext.promptCode()))
            .promptVersion(trim(safeContext.promptVersion()))
            .skillBizId(trim(safeContext.skillBizId()))
            .skillCode(trim(safeContext.skillCode()))
            .skillVersion(trim(safeContext.skillVersion()))
            .modelSkillBindingBizId(trim(safeContext.modelSkillBindingBizId()))
            .scenarioCode(trim(safeContext.scenarioCode()))
            .environment(trim(safeContext.environment()))
            .createdAt(now)
            .updatedAt(now)
            .build());
    }

    /**
     * 将模型调用审计标记为成功。
     *
     * @param callId 单次模型调用追踪标识
     * @param httpStatus 远端 HTTP 状态
     * @param durationMs 调用耗时毫秒
     * @param responseHash 输出内容哈希
     * @param responseText 输出原文，入库前会截断并脱敏保存为预览
     * @param outputSummary 可扩展输出摘要，不替代稳定 display 字段
     */
    @Transactional
    public void succeed(String callId, Integer httpStatus, long durationMs, String responseHash,
                        String responseText, Map<String, Object> outputSummary) {
        update(callId, "SUCCEEDED", httpStatus, durationMs, responseHash, responseText,
            outputSummary, null, null);
    }

    /**
     * 将模型调用审计标记为失败。
     *
     * @param callId 单次模型调用追踪标识
     * @param httpStatus 远端 HTTP 状态，未到达远端时为空
     * @param durationMs 调用耗时毫秒
     * @param responseHash 失败响应内容哈希
     * @param responseText 失败响应原文，入库前会截断保存
     * @param errorCode 结构化错误编码
     * @param errorMessage 可展示失败原因摘要
     */
    @Transactional
    public void fail(String callId, Integer httpStatus, long durationMs, String responseHash,
                     String responseText, String errorCode, String errorMessage) {
        update(callId, "FAILED", httpStatus, durationMs, responseHash, responseText,
            Map.of(), errorCode, errorMessage);
    }

    /**
     * 分页查询模型调用审计记录。
     *
     * <p>过滤条件全部来自稳定字段；页面可使用真实返回的业务展示名称、Prompt 和 Skill
     * 作为选择项，不需要用户手填业务 id。</p>
     *
     * @return 带 display 字段的审计分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<AiModelCallAuditView> list(
        String operationCode,
        String callStatus,
        String providerCode,
        String modelCode,
        String modelVersion,
        String businessType,
        String businessBizId,
        String taskCode,
        String eventId,
        String runBizId,
        String runNo,
        String reportBizId,
        String promptCode,
        String skillCode,
        String scenarioCode,
        String environment,
        PageQuery pageQuery
    ) {
        PageResult<AiModelCallAudit> page = audits.search(new AiModelCallAuditSearchCriteria(
            trim(operationCode),
            trim(callStatus),
            trim(providerCode),
            trim(modelCode),
            trim(modelVersion),
            trim(businessType),
            trim(businessBizId),
            trim(taskCode),
            trim(eventId),
            trim(runBizId),
            trim(runNo),
            trim(reportBizId),
            trim(promptCode),
            trim(skillCode),
            trim(scenarioCode),
            trim(environment),
            pageQuery.page(),
            pageQuery.size(),
            pageQuery.safeSort(SORTS, "createdAt"),
            "asc".equalsIgnoreCase(pageQuery.direction())
        ));
        return PageResult.<AiModelCallAuditView>builder()
            .items(page.items().stream().map(this::toView).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 查询模型调用审计详情。
     *
     * @param bizId 审计记录业务唯一标识
     * @return 包含脱敏输入输出预览和业务关联的审计详情
     */
    @Transactional(readOnly = true)
    public AiModelCallAuditView detail(String bizId) {
        return audits.findByBizId(bizId)
            .map(this::toView)
            .orElseThrow(() -> new com.example.dzcom.application.common.exception.BusinessException(
                org.springframework.http.HttpStatus.NOT_FOUND, "模型调用审计不存在"));
    }

    /**
     * 更新审计结果字段。
     *
     * @param callId 单次模型调用追踪标识
     * @param status 最终调用状态
     * @param httpStatus 远端 HTTP 状态
     * @param durationMs 调用耗时毫秒
     * @param responseHash 输出内容哈希
     * @param responseText 输出或失败响应原文
     * @param outputSummary 可扩展输出摘要
     * @param errorCode 失败错误编码
     * @param errorMessage 失败原因摘要
     */
    private void update(String callId, String status, Integer httpStatus, long durationMs, String responseHash,
                        String responseText, Map<String, Object> outputSummary, String errorCode, String errorMessage) {
        audits.findByCallId(callId).ifPresent(existing -> audits.save(existing.toBuilder()
            .callStatus(status)
            .httpStatus(httpStatus)
            .durationMs(durationMs)
            .responseHash(responseHash)
            .responsePreview(preview(responseText))
            .outputSummary(toJson(outputSummary))
            .errorCode(trim(errorCode))
            .errorMessage(preview(errorMessage, 1024))
            .updatedAt(clock.now())
            .build()));
    }

    /**
     * 转换审计视图并补齐前端可直接展示的稳定字段。
     *
     * @param audit 模型调用审计领域对象
     * @return 面向接口层的审计视图
     */
    private AiModelCallAuditView toView(AiModelCallAudit audit) {
        return AiModelCallAuditView.builder()
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
            .inputSummary(audit.inputSummary())
            .outputSummary(audit.outputSummary())
            .modelDisplay(modelDisplay(audit))
            .businessDisplay(businessDisplay(audit))
            .promptDisplay(promptDisplay(audit))
            .skillDisplay(skillDisplay(audit))
            .durationDisplay(durationDisplay(audit.durationMs()))
            .failureDisplay(failureDisplay(audit.errorCode(), audit.errorMessage()))
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
     * 拼接可展示输入预览。
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词
     * @return 包含系统提示词和用户提示词的 JSON 文本
     */
    private String joinPromptPreview(String systemPrompt, String userPrompt) {
        Map<String, Object> preview = new LinkedHashMap<>();
        preview.put("systemPrompt", systemPrompt == null ? "" : systemPrompt);
        preview.put("userPrompt", userPrompt == null ? "" : userPrompt);
        return Jsons.toJson(preview);
    }

    /**
     * 序列化可扩展摘要。
     *
     * @param value 摘要键值
     * @return 空摘要返回 null，非空摘要返回 JSON 文本
     */
    private String toJson(Map<String, Object> value) {
        return value == null || value.isEmpty() ? null : Jsons.toJson(value);
    }

    /**
     * 按默认长度截断审计预览。
     *
     * @param value 原始文本
     * @return 截断后的审计预览
     */
    private String preview(String value) {
        return preview(value, PREVIEW_LIMIT);
    }

    /**
     * 按指定长度截断审计预览。
     *
     * @param value 原始文本
     * @param limit 最大字符数
     * @return 截断后的文本
     */
    private String preview(String value, int limit) {
        if (value == null) {
            return null;
        }
        String trimmed = value.strip();
        return trimmed.length() <= limit ? trimmed : trimmed.substring(0, limit) + "...";
    }

    /**
     * 归一化字符串查询值。
     *
     * @param value 原始字符串
     * @return 去除前后空格后的文本，空白值返回 null
     */
    private String trim(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * 生成模型展示名称。
     *
     * @param audit 模型调用审计记录
     * @return 模型编码、版本和远端模型组合后的展示文本
     */
    private String modelDisplay(AiModelCallAudit audit) {
        String base = trim(audit.modelCode()) == null ? "-" : audit.modelCode();
        String version = trim(audit.modelVersion()) == null ? "-" : audit.modelVersion();
        String remote = trim(audit.remoteModel());
        return remote == null ? base + "@" + version : base + "@" + version + " / " + remote;
    }

    /**
     * 生成业务对象展示名称。
     *
     * @param audit 模型调用审计记录
     * @return 优先使用业务标签，其次使用业务类型和对象标识
     */
    private String businessDisplay(AiModelCallAudit audit) {
        String label = trim(audit.businessLabel());
        if (label != null) {
            return label;
        }
        String type = trim(audit.businessType());
        String bizId = trim(audit.businessBizId());
        if (type == null && bizId == null) {
            return "-";
        }
        return (type == null ? "业务对象" : type) + (bizId == null ? "" : " · " + bizId);
    }

    /**
     * 生成 Prompt 展示名称。
     *
     * @param audit 模型调用审计记录
     * @return Prompt 编码和版本组合后的展示文本
     */
    private String promptDisplay(AiModelCallAudit audit) {
        return versionedDisplay(audit.promptCode(), audit.promptVersion());
    }

    /**
     * 生成 Skill 展示名称。
     *
     * @param audit 模型调用审计记录
     * @return Skill 编码和版本组合后的展示文本
     */
    private String skillDisplay(AiModelCallAudit audit) {
        return versionedDisplay(audit.skillCode(), audit.skillVersion());
    }

    /**
     * 生成耗时展示文本。
     *
     * @param durationMs 调用耗时毫秒
     * @return 可直接展示的耗时文本
     */
    private String durationDisplay(Long durationMs) {
        return durationMs == null ? "-" : durationMs + " ms";
    }

    /**
     * 生成失败原因展示文本。
     *
     * @param errorCode 错误编码
     * @param errorMessage 错误消息
     * @return 可直接展示的失败原因
     */
    private String failureDisplay(String errorCode, String errorMessage) {
        String code = trim(errorCode);
        String message = trim(errorMessage);
        if (code == null && message == null) {
            return "-";
        }
        if (code == null) {
            return message;
        }
        if (message == null) {
            return code;
        }
        return code + " · " + message;
    }

    /**
     * 生成带版本号的展示文本。
     *
     * @param code 业务编码
     * @param version 版本号
     * @return 编码和版本组合后的展示文本
     */
    private String versionedDisplay(String code, String version) {
        String normalizedCode = trim(code);
        if (normalizedCode == null) {
            return "-";
        }
        String normalizedVersion = trim(version);
        return normalizedVersion == null ? normalizedCode : normalizedCode + "@" + normalizedVersion;
    }
}
