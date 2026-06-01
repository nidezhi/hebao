package com.example.dzcom.interfaces.vo.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 更新用户请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    private String email;
    
    private String phone;
    
    @Min(value = 1, message = "风险等级最小为1")
    @Max(value = 5, message = "风险等级最大为5")
    private Integer riskLevel;
}
