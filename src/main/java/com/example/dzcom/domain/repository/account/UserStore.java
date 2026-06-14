package com.example.dzcom.domain.repository.account;

import com.example.dzcom.common.page.PageResult;
import com.example.dzcom.domain.model.account.User;

import java.util.Optional;

public interface UserStore {
    User save(User user);

    Optional<User> findByBizId(String bizId);

    PageResult<User> search(UserSearchCriteria criteria);
}
