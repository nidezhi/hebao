package com.example.dzcom.application.service.account;

public interface CurrentOperatorProvider {
    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    CurrentOperator required();
}
