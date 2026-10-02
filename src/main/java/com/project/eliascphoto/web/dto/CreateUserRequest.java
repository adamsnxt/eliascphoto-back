package com.project.eliascphoto.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CreateUserRequest {

    @NotBlank
    @Size(max = 50)
    private String userName;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;
}
