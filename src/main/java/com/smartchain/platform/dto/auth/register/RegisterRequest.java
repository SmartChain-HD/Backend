package com.smartchain.platform.dto.auth.register;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "이름을 입력해주세요")
    @Size(min = 2, max = 50, message = "이름은 2~50자 사이여야 합니다")
    private String name;
    
    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "올바른 이메일 형식이 아닙니다")
    private String email;
    
    @NotBlank(message = "비밀번호를 입력해주세요")
    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다")
    private String password;
    
    @NotBlank(message = "비밀번호 확인을 입력해주세요")
    private String passwordConfirm;
    
    @NotNull(message = "약관 동의 정보가 필요합니다")
    private TermsAgreementRequest terms;
}
