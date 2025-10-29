package com.cat.connect.service;


import com.cat.connect.dto.company.Company;
import com.cat.connect.dto.company.CompanyCreateRequest;
import com.cat.connect.dto.company.CompanyDto;
import com.cat.connect.dto.company.CompanyUpdateRequest;
import com.cat.connect.repository.company.CompanyJdbcRepository;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyJdbcRepository repo;

    public CompanyService(CompanyJdbcRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public CompanyDto create(CompanyCreateRequest req) {
        // quick app-level check (DB has UNIQUE(code) as well)
        repo.findByCode(req.code()).ifPresent(c -> {
            throw new IllegalArgumentException("Company code already exists: " + req.code());
        });

        Company c = new Company();
        c.setCode(req.code());
        c.setName(req.name());
        try {
            Company saved = repo.insert(c);
            return toDto(saved);
        } catch (DuplicateKeyException e) {
            // race condition fallback
            throw new IllegalArgumentException("Company code already exists: " + req.code());
        }
    }

    @Transactional(readOnly = true)
    public CompanyDto get(Long id) {
        Company c = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: id=" + id));
        return toDto(c);
    }

    @Transactional(readOnly = true)
    public PagedResult<CompanyDto> list(String q, int page, int size, String sort, String dir) {
        int total = repo.count(q);
        List<CompanyDto> items = repo.findPage(q, page, size, sort, dir)
                .stream().map(this::toDto).toList();
        return new PagedResult<>(items, page, size, total);
    }

    @Transactional
    public CompanyDto update(Long id, CompanyUpdateRequest req) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("Company not found: id=" + id);
        }
        repo.updateName(id, req.name());
        return get(id);
    }

    @Transactional
    public void delete(Long id) {
        if (!repo.existsById(id)) return; // idempotent delete
        repo.deleteById(id);
    }

    private CompanyDto toDto(Company c) {
        return new CompanyDto(c.getId(), c.getCode(), c.getName(), c.getCreatedAt());
    }

    // Simple page wrapper (or use Spring Data's Page if you prefer)
    public record PagedResult<T>(List<T> content, int page, int size, int total) {}
}

