package com.cat.connect.service;// src/main/java/com/cat/connect/userrole/UserRoleService.java

import com.cat.connect.dto.UserRoleResponse;
import com.cat.connect.repository.userrole.UserRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoleService {
    private final UserRoleRepository repo;

    public UserRoleService(UserRoleRepository repo) {
        this.repo = repo;
    }

    public void add(long userId, long roleId) {
        repo.insert(userId, roleId);
    }

    public List<UserRoleResponse> listAll() { return repo.findAll(); }
    public List<UserRoleResponse> listByUser(long userId) { return repo.findByUserId(userId); }
    public List<UserRoleResponse> listByRole(long roleId) { return repo.findByRoleId(roleId); }
    public boolean exists(long userId, long roleId) { return repo.exists(userId, roleId); }
    public int remove(long userId, long roleId) { return repo.delete(userId, roleId); }

    @Transactional
    public void changeRole(long userId, long oldRoleId, long newRoleId) {
        if (oldRoleId == newRoleId) return;
        repo.delete(userId, oldRoleId);
        repo.insert(userId, newRoleId);
    }
}
