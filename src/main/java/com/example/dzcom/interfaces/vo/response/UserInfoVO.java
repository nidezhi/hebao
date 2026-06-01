package com.example.dzcom.interfaces.vo.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVO {
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户编号
     */
    private String userNo;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
}
