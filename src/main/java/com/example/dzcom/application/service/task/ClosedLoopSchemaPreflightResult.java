package com.example.dzcom.application.service.task;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** 自动投资闭环数据库结构预检结果。 */
public record ClosedLoopSchemaPreflightResult(boolean passed, List<String> reasons, Map<String, Object> detail) {

    /** 返回一个通过的预检结果，用于测试或无数据库预检实现时保持闭环可运行。 */
    public static ClosedLoopSchemaPreflightResult pass() {
        return new ClosedLoopSchemaPreflightResult(true, List.of(), Map.of("status", "PASS"));
    }

    /**
     * 将预检结果转换成可写入闭环步骤的摘要。
     *
     * @return 可序列化摘要
     * @author dz
     * @date 2026-07-01
     */
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("passed", passed);
        result.put("reasons", reasons == null ? List.of() : reasons);
        result.put("detail", detail == null ? Map.of() : detail);
        return result;
    }
}
