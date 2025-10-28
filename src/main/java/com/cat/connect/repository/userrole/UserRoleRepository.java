// src/main/java/com/cat/connect/userrole/UserRoleRepository.java
package com.cat.connect.repository.userrole;

import com.cat.connect.dto.UserRoleResponse;
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

    public int insert(long userId, long roleId) throws DuplicateKeyException {
        // PK(user_id, role_id) avoids duplicates; DuplicateKeyException if already exists
        return jdbc.update("INSERT INTO user_role (user_id, role_id) VALUES (?, ?)", userId, roleId);
    }

    public List<UserRoleResponse> findAll() {
        return jdbc.query("SELECT user_id, role_id FROM user_role ORDER BY user_id, role_id", MAPPER);
    }

    public List<UserRoleResponse> findByUserId(long userId) {
        return jdbc.query("SELECT user_id, role_id FROM user_role WHERE user_id = ? ORDER BY role_id", MAPPER, userId);
    }

    public List<UserRoleResponse> findByRoleId(long roleId) {
        return jdbc.query("SELECT user_id, role_id FROM user_role WHERE role_id = ? ORDER BY user_id", MAPPER, roleId);
    }

    public boolean exists(long userId, long roleId) {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_role WHERE user_id = ? AND role_id = ?",
                Integer.class, userId, roleId);
        return n != null && n > 0;
    }

    public int delete(long userId, long roleId) {
        return jdbc.update("DELETE FROM user_role WHERE user_id = ? AND role_id = ?", userId, roleId);
    }
}
