// src/main/java/com/cat/connect/service/UserRoleService.java
package com.cat.connect.service;

import com.cat.connect.dto.UserRoleResponse;
import com.cat.connect.repository.userrole.UserRoleRepository;
import com.cat.connect.tenant.TenantContext;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserRoleService {

    private final UserRoleRepository repo;

    public UserRoleService(UserRoleRepository repo) {
        this.repo = repo;
    }

    private long company() {
        Long c = TenantContext.getCompanyId();
        if (c == null) throw new IllegalStateException("No companyId in TenantContext");
        return c;
    }

    public List<UserRoleResponse> listAll() { return repo.findAll(company()); }
    public List<UserRoleResponse> listByUser(long userId) { return repo.findByUserId(company(), userId); }
    public List<UserRoleResponse> listByRole(long roleId) { return repo.findByRoleId(company(), roleId); }
    public boolean exists(long userId, long roleId) { return repo.exists(company(), userId, roleId); }

    public void add(long userId, long roleId) throws DuplicateKeyException {
        long cid = company();
        if (!repo.userExists(cid, userId)) throw new IllegalArgumentException("userId not found in this company: " + userId);
        if (!repo.roleExists(cid, roleId)) throw new IllegalArgumentException("roleId not found in this company: " + roleId);
        repo.insert(cid, userId, roleId);
    }

    public int remove(long userId, long roleId) {
        return repo.delete(company(), userId, roleId);
    }

    @Transactional
    public void changeRole(long userId, long oldRoleId, long newRoleId) {
        if (oldRoleId == newRoleId) return;
        long cid = company();
        if (!repo.roleExists(cid, newRoleId)) throw new IllegalArgumentException("newRoleId not found in this company: " + newRoleId);
        repo.delete(cid, userId, oldRoleId);
        repo.insert(cid, userId, newRoleId);
    }
}
