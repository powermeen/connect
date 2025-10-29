// src/main/java/com/cat/connect/repository/userrole/UserRoleRepository.java
package com.cat.connect.repository.userrole;

import com.cat.connect.dto.user.UserRoleResponse;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRoleRepository {

    private final JdbcTemplate jdbc;

    public UserRoleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private static final RowMapper<UserRoleResponse> MAPPER = (rs, i) ->
            new UserRoleResponse(rs.getLong("user_id"), rs.getLong("role_id"));

    // ---------- CRUD (scoped by company_id) ----------

    /** Insert mapping for a tenant (PK: company_id, user_id, role_id). */
    public int insert(long companyId, long userId, long roleId) throws DuplicateKeyException {
        return jdbc.update(
                "INSERT INTO user_role (company_id, user_id, role_id) VALUES (?, ?, ?)",
                companyId, userId, roleId
        );
    }

    /** List all mappings for a tenant. */
    public List<UserRoleResponse> findAll(long companyId) {
        return jdbc.query(
                "SELECT user_id, role_id FROM user_role " +
                        "WHERE company_id = ? ORDER BY user_id, role_id",
                MAPPER, companyId
        );
    }

    /** List mappings by user within a tenant. */
    public List<UserRoleResponse> findByUserId(long companyId, long userId) {
        return jdbc.query(
                "SELECT user_id, role_id FROM user_role " +
                        "WHERE company_id = ? AND user_id = ? ORDER BY role_id",
                MAPPER, companyId, userId
        );
    }

    /** List mappings by role within a tenant. */
    public List<UserRoleResponse> findByRoleId(long companyId, long roleId) {
        return jdbc.query(
                "SELECT user_id, role_id FROM user_role " +
                        "WHERE company_id = ? AND role_id = ? ORDER BY user_id",
                MAPPER, companyId, roleId
        );
    }

    /** Check mapping existence within a tenant. */
    public boolean exists(long companyId, long userId, long roleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_role WHERE company_id = ? AND user_id = ? AND role_id = ?",
                Integer.class, companyId, userId, roleId
        );
        return n != null && n > 0;
    }

    /** Delete mapping within a tenant. */
    public int delete(long companyId, long userId, long roleId) {
        return jdbc.update(
                "DELETE FROM user_role WHERE company_id = ? AND user_id = ? AND role_id = ?",
                companyId, userId, roleId
        );
    }

    // ---------- Parent existence checks (tenant-safe) ----------

    /** app_user must exist in the same company. */
    public boolean userExists(long companyId, long userId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM app_user WHERE company_id = ? AND id = ?",
                Integer.class, companyId, userId
        );
        return n != null && n > 0;
    }

    /** role must exist in the same company. */
    public boolean roleExists(long companyId, long roleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM role WHERE company_id = ? AND id = ?",
                Integer.class, companyId, roleId
        );
        return n != null && n > 0;
    }
}
