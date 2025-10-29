package com.cat.connect.controller;


import com.cat.connect.dto.company.CompanyCreateRequest;
import com.cat.connect.dto.company.CompanyDto;
import com.cat.connect.dto.company.CompanyUpdateRequest;
import com.cat.connect.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyDto create(@Valid @RequestBody CompanyCreateRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public CompanyDto get(@PathVariable Long id) {
        return service.get(id);
    }

    @GetMapping
    public CompanyService.PagedResult<CompanyDto> list(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "asc") String dir
    ) {
        return service.list(q, page, size, sort, dir);
    }

    @PutMapping("/{id}")
    public CompanyDto update(@PathVariable Long id,
                             @Valid @RequestBody CompanyUpdateRequest req) {
        return service.update(id, req);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
