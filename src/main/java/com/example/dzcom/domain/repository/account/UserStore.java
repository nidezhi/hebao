package com.example.dzcom.domain.repository.account;

import com.example.dzcom.application.common.page.PageResult;
import com.example.dzcom.domain.model.account.User;

import java.util.Optional;

/** 用户主体仓储端口。 */
public interface UserStore {
    /** 保存用户主体。 */
    User save(User user);

    /** 根据业务标识查询有效用户。 */
    Optional<User> findByBizId(String bizId);

    /** 根据筛选条件分页查询用户。 */
    PageResult<User> search(UserSearchCriteria criteria);
}
