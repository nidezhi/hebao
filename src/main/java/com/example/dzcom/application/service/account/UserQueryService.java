package com.example.dzcom.application.service.account;

import com.example.dzcom.application.assembler.account.AccountViewAssembler;
import com.example.dzcom.application.dto.account.UserView;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.page.PageQuery;
import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.enums.account.AccountStatus;
import com.example.dzcom.domain.enums.account.KycStatus;
import com.example.dzcom.domain.model.account.User;
import com.example.dzcom.domain.repository.account.AccountStore;
import com.example.dzcom.domain.repository.account.UserSearchCriteria;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserQueryService {
    private static final Set<String> SORT_FIELDS = Set.of("createdAt", "userNo", "status", "lastLoginAt");

    private final AccountStore store;
    private final AccountViewAssembler assembler;
    private final CurrentOperatorProvider currentOperator;

    @Transactional(readOnly = true)
    public UserView detail(String bizId) {
        requireAdmin();
        return assembler.assemble(requiredUser(bizId));
    }

    @Transactional(readOnly = true)
    public PageResult<UserView> list(String keyword, AccountStatus status, KycStatus kycStatus,
                                     Integer riskLevel, PageQuery pageQuery) {
        requireAdmin();
        String sort = pageQuery.safeSort(SORT_FIELDS, "createdAt");
        PageResult<User> page = store.searchUsers(new UserSearchCriteria(keyword, status, kycStatus, riskLevel,
            pageQuery.page(), pageQuery.size(), sort, "asc".equals(pageQuery.direction())));
        return PageResult.<UserView>builder()
            .items(page.items().stream().map(assembler::assemble).toList())
            .total(page.total())
            .page(page.page())
            .size(page.size())
            .totalPages(page.totalPages())
            .build();
    }

    private User requiredUser(String bizId) {
        return store.findUser(bizId)
            .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "用户不存在"));
    }

    private void requireAdmin() {
        if (!currentOperator.required().hasRole("ADMIN")) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "需要管理员权限");
        }
    }
}
