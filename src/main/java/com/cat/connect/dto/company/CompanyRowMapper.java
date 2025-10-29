package com.cat.connect.dto.company;

import com.cat.connect.dto.company.Company;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class CompanyRowMapper implements RowMapper<Company> {
    @Override
    public Company mapRow(ResultSet rs, int rowNum) throws SQLException {
        Company c = new Company();
        c.setId(rs.getLong("id"));
        c.setCode(rs.getString("code"));
        c.setName(rs.getString("name"));
        var ts = rs.getTimestamp("created_at");
        c.setCreatedAt(ts != null ? ts.toInstant() : null);
        return c;
    }
}
