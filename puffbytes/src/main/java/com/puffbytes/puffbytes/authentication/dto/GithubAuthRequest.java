package com.puffbytes.puffbytes.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GithubAuthRequest {

    @NotBlank(message = "Code is required")
    private String code;
}