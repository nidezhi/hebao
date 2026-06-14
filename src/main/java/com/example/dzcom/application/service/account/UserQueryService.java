package com.example.dzcom.application.service.account;

import com.example.dzcom.application.assembler.account.AccountViewAssembler;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.application.common.exception.BusinessException;
import com.example.dzcom.application.common.page.PageQuery;
import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.domain.model.account.User;
import com.example.dzcom.domain.repository.account.UserStore;
import com.example.dzcom.domain.repository.account.UserSearchCriteria;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "userNo", "status", "lastLoginAt");

    private final UserStore users;
    private final AccountViewAssembler assembler;
    private final CurrentOperatorProvider currentOperator;

    /**
     * 根据指定条件查询业务数据。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public UserView detail(String bizId) {
        requireAdmin();
        return assembler.assemble(requiredUser(bizId));
    }

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @param keyword 模糊查询关键字
     * @param status 目标状态或目标值
     * @param kycStatus kycStatus 参数
     * @param riskLevel riskLevel 参数
     * @param pageQuery 分页查询参数
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public PageResult<UserView> list(String keyword, AccountStatus status, KycStatus kycStatus,
                                     Integer riskLevel, PageQuery pageQuery) {
        requireAdmin();
        String sort = pageQuery.safeSort(SORT_FIELDS, "createdAt");
        PageResult<User> page = users.search(new UserSearchCriteria(keyword, status, kycStatus, riskLevel,
            pageQuery.page(), pageQuery.size(), sort, "asc".equals(pageQuery.direction())));
        return PageResult.<UserView>builder()
            .items(page.items().stream().map(assembler::assemble).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    /**
     * 获取必需的业务对象，不存在时终止当前流程。
     *
     * @param bizId 业务对象的唯一标识
     * @return 查询到的业务数据
     * @author dz
     * @date 2026-06-14
     */
    private User requiredUser(String bizId) {
        return users.findByBizId(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    /**
     * 执行 require admin 处理。
     *
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void requireAdmin() {
        if (!currentOperator.required().hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }
}
