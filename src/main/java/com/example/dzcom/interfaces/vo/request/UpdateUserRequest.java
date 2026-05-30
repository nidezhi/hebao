package com.example.dzcom.interfaces.vo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.Data;

/**
 * 更新用户请求
 */
@Data
public class UpdateUserRequest {
    
    private String email;
    
    private String phone;
    
    @Min(value = 1, message = "风险等级最小为1")
    @Max(value = 5, message = "风险等级最大为5")
    private Integer riskLevel;
}
