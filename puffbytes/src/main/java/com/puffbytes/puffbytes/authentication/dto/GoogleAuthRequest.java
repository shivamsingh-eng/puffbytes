package com.puffbytes.puffbytes.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank(message = "ID token is required")
    private String idToken;
}
