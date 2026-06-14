package com.example.dzcom.application.service.account;

import com.example.dzcom.application.dto.account.PreferenceView;
import com.example.dzcom.common.exception.BusinessException;
import com.example.dzcom.common.service.ClockProvider;
import com.example.dzcom.common.service.IdGenerator;
import com.example.dzcom.domain.model.account.UserPreference;
import com.example.dzcom.domain.repository.account.AccountStore;
import com.example.dzcom.application.service.account.CurrentOperatorProvider;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserPreferenceApplicationService {
    private static final Set<String> ALLOWED_KEYS = Set.of(
        "language", "timezone", "theme", "market", "notification", "dashboard"
    );

    private final AccountStore store;
    private final CurrentOperatorProvider currentOperator;
    private final IdGenerator idGenerator;
    private final ClockProvider clock;
    private final ObjectMapper objectMapper;

    /**
     * 根据查询条件获取业务数据列表。
     *
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    @Transactional(readOnly = true)
    public List<PreferenceView> list() {
        return store.findPreferences(currentOperator.required().userBizId())
            .stream().map(this::toView).toList();
    }

    /**
     * 执行 set 处理。
     *
     * @param key 数据键
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public PreferenceView set(String key, JsonNode value) {
        validateKey(key);
        if (value == null || value.isNull()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "偏好值不能为空");
        }
        String userBizId = currentOperator.required().userBizId();
        UserPreference existing = store.findPreference(userBizId, key, true).orElse(null);
        UserPreference preference = UserPreference.builder()
            .bizId(existing == null ? idGenerator.newBizId() : existing.bizId())
            .userBizId(userBizId)
            .key(key)
            .valueType(valueType(value))
            .jsonValue(value.toString())
            .updatedAt(clock.now())
            .deleted(0)
            .build();
        return toView(store.savePreference(preference));
    }

    /**
     * 删除或逻辑删除对应的业务数据。
     *
     * @param key 数据键
     * @author dz
     * @date 2026-06-14
     */
    @Transactional
    public void delete(String key) {
        validateKey(key);
        String userBizId = currentOperator.required().userBizId();
        store.findPreference(userBizId, key, false).ifPresent(existing ->
            store.savePreference(existing.toBuilder().updatedAt(clock.now()).deleted(1).build()));
    }

    /**
     * 将源对象转换为目标视图或领域对象。
     *
     * @param preference preference 参数
     * @return 转换后的目标对象
     * @throws IllegalStateException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private PreferenceView toView(UserPreference preference) {
        try {
            return PreferenceView.builder()
                .key(preference.key())
                .valueType(preference.valueType())
                .value(objectMapper.readTree(preference.jsonValue()))
                .updatedAt(preference.updatedAt())
                .build();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("偏好数据格式非法", e);
        }
    }

    /**
     * 校验输入值是否满足业务约束。
     *
     * @param key 数据键
     * @throws BusinessException 输入或业务状态不满足要求时抛出
     * @author dz
     * @date 2026-06-14
     */
    private void validateKey(String key) {
        if (key == null || !ALLOWED_KEYS.contains(key)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "不支持的偏好键");
        }
    }

    /**
     * 执行 value type 处理。
     *
     * @param value 待处理的数据值
     * @return 方法执行后的结果
     * @author dz
     * @date 2026-06-14
     */
    private String valueType(JsonNode value) {
        if (value.isBoolean()) return "BOOLEAN";
        if (value.isNumber()) return "NUMBER";
        if (value.isTextual()) return "STRING";
        return "JSON";
    }
}
