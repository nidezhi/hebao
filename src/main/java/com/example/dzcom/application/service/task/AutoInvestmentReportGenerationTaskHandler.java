package com.example.dzcom.application.service.task;

import com.example.dzcom.application.command.ai.GenerateInvestmentAnalysisCommand;
import com.example.dzcom.application.service.ai.InvestmentAnalysisApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** 自动投资报告生成任务。 */
@Service
@RequiredArgsConstructor
public class AutoInvestmentReportGenerationTaskHandler implements InvestmentTaskHandler {
    private static final String TASK_TYPE = "AUTO_INVESTMENT_REPORT_GENERATION";
    private static final String DEFAULT_MODEL_CODE = "openai-compatible-analysis";
    private static final BigDecimal DEFAULT_INITIAL_CAPITAL = BigDecimal.valueOf(100000);

    private final InvestmentAnalysisApplicationService analysis;

    /**
     * 判断当前处理器是否支持自动投资报告生成任务。
     *
     * @param taskType 任务类型
     * @return 类型为 AUTO_INVESTMENT_REPORT_GENERATION 时返回 true
     * @author dz
     * @date 2026-06-24
     */
    @Override
    public boolean supports(String taskType) {
        return TASK_TYPE.equals(taskType);
    }

    /**
     * 按任务配置批量生成投资分析报告。
     *
     * <p>该任务不直接解析行情和资讯，而是复用投资分析应用服务。任务参数中的
     * {@code themes} 控制报告主题集合，{@code modelCode} 默认使用 OpenAI 兼容模型，
     * 前端可通过任务配置接口调整模型、Cron、回看窗口和初始资金。</p>
     *
     * @param event 任务触发事件
     * @return 本次生成报告摘要
     * @author dz
     * @date 2026-06-24
     */
    @Override
    @Transactional
    public String execute(InvestmentTaskEvent event) {
        String providerCode = TaskParameterParser.string(event.parameters(), "providerCode", "OPENAI_COMPATIBLE");
        String modelCode = TaskParameterParser.string(event.parameters(), "modelCode", DEFAULT_MODEL_CODE);
        String marketScope = TaskParameterParser.marketScope(event.parameters());
        int lookbackDays = TaskParameterParser.positiveInt(event.parameters(), "lookbackDays", 30);
        BigDecimal initialCapital = parseInitialCapital(event);
        List<String> themeCodes = resolveThemeCodes(event);
        if (themeCodes.isEmpty()) {
            analysis.generate(command(providerCode, modelCode, marketScope, null, lookbackDays, initialCapital));
            return "已生成 1 份市场级自动投资分析报告";
        }
        themeCodes.forEach(themeCode -> analysis.generate(
            command(providerCode, modelCode, marketScope, themeCode, lookbackDays, initialCapital)
        ));
        return "已生成 " + themeCodes.size() + " 份主题自动投资分析报告";
    }

    /**
     * 解析自动报告主题编码。
     *
     * @param event 任务触发事件
     * @return 主题编码列表
     * @author dz
     * @date 2026-06-24
     */
    private List<String> resolveThemeCodes(InvestmentTaskEvent event) {
        List<String> explicitThemeCodes = TaskParameterParser.list(event.parameters(), "themeCodes");
        if (!explicitThemeCodes.isEmpty()) {
            return explicitThemeCodes;
        }
        return new ArrayList<>(TaskParameterParser.themes(event.parameters()).keySet()).stream()
            .map(TaskParameterParser::themeCode)
            .toList();
    }

    /**
     * 解析报告模拟初始资金。
     *
     * @param event 任务触发事件
     * @return 初始资金
     * @author dz
     * @date 2026-06-24
     */
    private BigDecimal parseInitialCapital(InvestmentTaskEvent event) {
        String value = TaskParameterParser.string(event.parameters(), "initialCapital", "");
        if (value.isBlank()) {
            return DEFAULT_INITIAL_CAPITAL;
        }
        BigDecimal capital = new BigDecimal(value.trim());
        if (capital.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("initialCapital 必须大于 0");
        }
        return capital;
    }

    /**
     * 构建投资分析命令。
     *
     * @param providerCode Provider 校验编码
     * @param modelCode 模型稳定编码
     * @param marketScope 市场范围
     * @param themeCode 主题编码
     * @param lookbackDays 回看天数
     * @param initialCapital 初始资金
     * @return 投资分析命令
     * @author dz
     * @date 2026-06-24
     */
    private GenerateInvestmentAnalysisCommand command(
        String providerCode,
        String modelCode,
        String marketScope,
        String themeCode,
        int lookbackDays,
        BigDecimal initialCapital
    ) {
        return GenerateInvestmentAnalysisCommand.builder()
            .providerCode(providerCode)
            .modelCode(modelCode)
            .marketScope(marketScope)
            .themeCode(themeCode)
            .lookbackDays(lookbackDays)
            .initialCapital(initialCapital)
            .build();
    }
}
