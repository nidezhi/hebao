package com.example.dzcom.infrastructure.task;

import com.example.dzcom.application.service.task.ClosedLoopSchemaPreflight;
import com.example.dzcom.application.service.task.ClosedLoopSchemaPreflightResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/** 基于 JDBC 元数据执行自动投资闭环数据库结构预检。 */
@Component
@RequiredArgsConstructor
public class JdbcClosedLoopSchemaPreflight implements ClosedLoopSchemaPreflight {
    private static final int REQUIRED_ORDER_IDEMPOTENCY_LENGTH = 512;

    private final DataSource dataSource;

    /**
     * 检查闭环关键表字段是否已完成 Flyway 迁移，避免运行到下单阶段才暴露结构漂移。
     *
     * @return 数据库结构预检结果
     * @author dz
     * @date 2026-07-01
     */
    @Override
    public ClosedLoopSchemaPreflightResult inspect() {
        List<String> reasons = new ArrayList<>();
        Map<String, Object> detail = new LinkedHashMap<>();
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            ColumnInfo idempotencyKey = column(metaData, connection.getCatalog(), "aiw_order", "idempotency_key");
            detail.put("aiw_order.idempotency_key.length", idempotencyKey.size());
            if (!idempotencyKey.exists()) {
                reasons.add("ORDER_IDEMPOTENCY_KEY_MISSING");
            } else if (idempotencyKey.size() < REQUIRED_ORDER_IDEMPOTENCY_LENGTH) {
                reasons.add("ORDER_IDEMPOTENCY_KEY_TOO_SHORT");
            }
            ColumnInfo chatSnapshot = column(metaData, connection.getCatalog(), "aiw_investment_analysis_report", "chat_snapshot");
            detail.put("aiw_investment_analysis_report.chat_snapshot.exists", chatSnapshot.exists());
            if (!chatSnapshot.exists()) {
                reasons.add("REPORT_CHAT_SNAPSHOT_MISSING");
            }
            boolean auditTableExists = tableExists(metaData, connection.getCatalog(), "aiw_ai_model_call_audit");
            detail.put("aiw_ai_model_call_audit.exists", auditTableExists);
            if (!auditTableExists) {
                reasons.add("AI_MODEL_CALL_AUDIT_TABLE_MISSING");
            }
            detail.put("requiredOrderIdempotencyLength", REQUIRED_ORDER_IDEMPOTENCY_LENGTH);
            return new ClosedLoopSchemaPreflightResult(reasons.isEmpty(), reasons, detail);
        } catch (SQLException exception) {
            reasons.add("SCHEMA_PREFLIGHT_METADATA_FAILED");
            detail.put("errorMessage", exception.getMessage());
            return new ClosedLoopSchemaPreflightResult(false, reasons, detail);
        }
    }

    /** 读取字段元数据。 */
    private ColumnInfo column(DatabaseMetaData metaData, String catalog, String tableName, String columnName) throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(catalog, null, tableName, columnName)) {
            if (resultSet.next()) {
                return new ColumnInfo(true, resultSet.getInt("COLUMN_SIZE"));
            }
        }
        return new ColumnInfo(false, 0);
    }

    /** 判断表是否存在。 */
    private boolean tableExists(DatabaseMetaData metaData, String catalog, String tableName) throws SQLException {
        try (ResultSet resultSet = metaData.getTables(catalog, null, tableName, new String[]{"TABLE"})) {
            return resultSet.next();
        }
    }

    /** 字段存在性和长度。 */
    private record ColumnInfo(boolean exists, int size) {
    }
}
