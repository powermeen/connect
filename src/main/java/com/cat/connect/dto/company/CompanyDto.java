package com.cat.connect.dto.company;

import java.time.Instant;

public record CompanyDto(Long id, String code, String name, Instant createdAt) {}