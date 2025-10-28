package com.cat.connect.controller;// src/main/java/com/cat/connect/userrole/UserRoleController.java


import com.cat.connect.dto.UserRoleRequest;
import com.cat.connect.dto.UserRoleResponse;
import com.cat.connect.dto.UserRoleUpdateRequest;
import com.cat.connect.service.UserRoleService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user-roles")
public class UserRoleController {

    private final UserRoleService service;

    public UserRoleController(UserRoleService service) {
        this.service = service;
    }

    // Create mapping
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRoleRequest req) {
        try {
            service.add(req.userId(), req.roleId());
            return ResponseEntity
                    .created(URI.create("/api/user-roles/%d/%d".formatted(req.userId(), req.roleId())))
                    .body(new UserRoleResponse(req.userId(), req.roleId()));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(409)
                    .body("Mapping already exists for userId=%d roleId=%d".formatted(req.userId(), req.roleId()));
        }
    }

    // Read list (optionally filter by userId or roleId)
    @GetMapping
    public List<UserRoleResponse> list(@RequestParam Optional<Long> userId,
                                       @RequestParam Optional<Long> roleId) {
        if (userId.isPresent()) return service.listByUser(userId.get());
        if (roleId.isPresent()) return service.listByRole(roleId.get());
        return service.listAll();
    }

    // Delete mapping
    @DeleteMapping("/{userId}/{roleId}")
    public ResponseEntity<?> delete(@PathVariable long userId, @PathVariable long roleId) {
        int rows = service.remove(userId, roleId);
        return (rows > 0) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // Update mapping: change role for a user (old -> new)
    @PutMapping("/{userId}/{oldRoleId}")
    public ResponseEntity<?> update(@PathVariable long userId,
                                    @PathVariable long oldRoleId,
                                    @RequestBody UserRoleUpdateRequest body) {
        if (body == null || body.newRoleId() == null) {
            return ResponseEntity.badRequest().body("newRoleId is required");
        }
        try {
            service.changeRole(userId, oldRoleId, body.newRoleId());
            return ResponseEntity.ok(new UserRoleResponse(userId, body.newRoleId()));
        } catch (DuplicateKeyException e) {
            // new mapping already exists
            return ResponseEntity.status(409)
                    .body("Mapping already exists for userId=%d roleId=%d"
                            .formatted(userId, body.newRoleId()));
        }
    }
}
