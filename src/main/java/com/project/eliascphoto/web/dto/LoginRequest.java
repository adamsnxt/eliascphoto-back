package com.project.eliascphoto.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    @Size(max = 50)
    private String userName;

    @NotBlank
    @Size(max = 72)
    private String password;
}
