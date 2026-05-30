package com.example.dzcom.interfaces.vo.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建用户请求
 */
@Data
public class CreateUserRequest {
    
    @NotBlank(message = "用户名不能为空")
    private String username;
    
    @Email(message = "邮箱格式不正确")
    private String email;
    
    private String phone;
    
    @NotBlank(message = "密码不能为空")
    private String password;
    
    private Integer riskLevel;
}
