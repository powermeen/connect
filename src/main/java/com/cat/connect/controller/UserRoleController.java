// src/main/java/com/cat/connect/controller/UserRoleController.java
package com.cat.connect.controller;

import com.cat.connect.dto.user.UserRoleRequest;
import com.cat.connect.dto.user.UserRoleResponse;
import com.cat.connect.dto.user.UserRoleUpdateRequest;
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

    // Create mapping (companyId is taken from TenantContext inside the service)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody UserRoleRequest req) {
        if (req == null) {
            return ResponseEntity.badRequest().body("Request body is required");
        }
        try {
            service.add(req.userId(), req.roleId());
            return ResponseEntity
                    .created(URI.create("/api/user-roles/%d/%d".formatted(req.userId(), req.roleId())))
                    .body(new UserRoleResponse(req.userId(), req.roleId()));
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(409)
                    .body("Mapping already exists for userId=%d roleId=%d".formatted(req.userId(), req.roleId()));
        } catch (IllegalArgumentException e) {
            // e.g. userId/roleId not found within this tenant
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Read list (optionally filter by userId or roleId), scoped by TenantContext
    @GetMapping
    public List<UserRoleResponse> list(@RequestParam Optional<Long> userId,
                                       @RequestParam Optional<Long> roleId) {
        if (userId.isPresent()) return service.listByUser(userId.get());
        if (roleId.isPresent()) return service.listByRole(roleId.get());
        return service.listAll();
    }

    // Delete mapping within current tenant
    @DeleteMapping("/{userId}/{roleId}")
    public ResponseEntity<?> delete(@PathVariable long userId, @PathVariable long roleId) {
        int rows = service.remove(userId, roleId);
        return (rows > 0) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    // Update mapping: change role for a user (old -> new) within current tenant
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
            return ResponseEntity.status(409)
                    .body("Mapping already exists for userId=%d roleId=%d"
                            .formatted(userId, body.newRoleId()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
