package com.example.dzcom.application.service.task;

/** 自动投资闭环运行前数据库结构预检。 */
public interface ClosedLoopSchemaPreflight {

    /**
     * 检查闭环依赖的关键表和字段是否已经迁移到当前代码需要的版本。
     *
     * @return 数据库结构预检结果
     * @author dz
     * @date 2026-07-01
     */
    ClosedLoopSchemaPreflightResult inspect();
}
