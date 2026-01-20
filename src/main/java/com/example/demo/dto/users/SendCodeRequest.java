package com.example.demo.dto.users;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "发送验证码请求")
public class SendCodeRequest {
    @Schema(description = "邮箱地址", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    @Schema(description = "使用场景: register-注册, forgetPassword-找回密码", example = "register", allowableValues = {"register", "forgetPassword"})
    private String scenario;
}
