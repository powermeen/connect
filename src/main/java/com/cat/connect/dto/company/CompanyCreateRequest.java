package com.cat.connect.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyCreateRequest(
        @NotBlank @Size(max = 64) String code,
        @NotBlank @Size(max = 255) String name
) {}
