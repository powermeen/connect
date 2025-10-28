// src/test/java/com/cat/connect/userrole/UserRoleControllerTest.java
package com.cat.connect.controller;


import com.cat.connect.dto.UserRoleResponse;
import com.cat.connect.service.UserRoleService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserRoleController.class)
class UserRoleControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    @MockBean
    UserRoleService service;

    @Nested
    class CreateMapping {

        @Test
        @DisplayName("POST /api/user-roles -> 201 Created")
        void create_ok() throws Exception {
            // service.add(...) just succeeds
            doNothing().when(service).add(1L, 2L);

            var body = """
                    {"userId":1,"roleId":2}
                    """;
            mvc.perform(post("/api/user-roles").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isCreated()).andExpect(header().string("Location", "/api/user-roles/1/2")).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$.userId").value(1)).andExpect(jsonPath("$.roleId").value(2));

            verify(service).add(1L, 2L);
        }

        @Test
        @DisplayName("POST /api/user-roles when duplicate -> 409 Conflict")
        void create_conflict() throws Exception {
            doThrow(new DuplicateKeyException("dup")).when(service).add(1L, 2L);

            var body = """
                    {"userId":1,"roleId":2}
                    """;
            mvc.perform(post("/api/user-roles").contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict()).andExpect(content().string(containsString("Mapping already exists")));
        }
    }

    @Nested
    class ReadList {
        @Test
        @DisplayName("GET /api/user-roles -> list all")
        void list_all() throws Exception {
            given(service.listAll()).willReturn(List.of(new UserRoleResponse(1L, 2L), new UserRoleResponse(1L, 3L)));

            mvc.perform(get("/api/user-roles")).andExpect(status().isOk()).andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)).andExpect(jsonPath("$", hasSize(2))).andExpect(jsonPath("$[0].userId").value(1)).andExpect(jsonPath("$[0].roleId").value(2));
        }

        @Test
        @DisplayName("GET /api/user-roles?userId=1 -> list by user")
        void list_by_user() throws Exception {
            given(service.listByUser(1L)).willReturn(List.of(new UserRoleResponse(1L, 2L)));

            mvc.perform(get("/api/user-roles").param("userId", "1")).andExpect(status().isOk()).andExpect(jsonPath("$[0].roleId").value(2));

            verify(service).listByUser(1L);
        }

        @Test
        @DisplayName("GET /api/user-roles?roleId=3 -> list by role")
        void list_by_role() throws Exception {
            given(service.listByRole(3L)).willReturn(List.of(new UserRoleResponse(1L, 3L), new UserRoleResponse(2L, 3L)));

            mvc.perform(get("/api/user-roles").param("roleId", "3")).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));

            verify(service).listByRole(3L);
        }
    }

    @Nested
    class DeleteMapping {
        @Test
        @DisplayName("DELETE /api/user-roles/{userId}/{roleId} -> 204 when deleted")
        void delete_noContent() throws Exception {
            given(service.remove(1L, 2L)).willReturn(1);

            mvc.perform(delete("/api/user-roles/{userId}/{roleId}", 1, 2)).andExpect(status().isNoContent());

            verify(service).remove(1L, 2L);
        }

        @Test
        @DisplayName("DELETE -> 404 when not found")
        void delete_notFound() throws Exception {
            given(service.remove(anyLong(), anyLong())).willReturn(0);

            mvc.perform(delete("/api/user-roles/{userId}/{roleId}", 9, 9)).andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdateMapping {
        @Test
        @DisplayName("PUT /api/user-roles/{userId}/{oldRoleId} -> 200 and returns new mapping")
        void update_ok() throws Exception {
            doNothing().when(service).changeRole(1L, 2L, 3L);

            var body = """
                    {"newRoleId":3}
                    """;
            mvc.perform(put("/api/user-roles/{userId}/{oldRoleId}", 1, 2).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isOk()).andExpect(jsonPath("$.userId").value(1)).andExpect(jsonPath("$.roleId").value(3));

            verify(service).changeRole(1L, 2L, 3L);
        }

        @Test
        @DisplayName("PUT missing newRoleId -> 400 Bad Request")
        void update_badRequest_whenMissingField() throws Exception {
            var bad = "{}";
            mvc.perform(put("/api/user-roles/{userId}/{oldRoleId}", 1, 2).contentType(MediaType.APPLICATION_JSON).content(bad)).andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("PUT duplicate target mapping -> 409 Conflict")
        void update_conflict() throws Exception {
            doThrow(new DuplicateKeyException("dup")).when(service).changeRole(1L, 2L, 3L);

            var body = """
                    {"newRoleId":3}
                    """;
            mvc.perform(put("/api/user-roles/{userId}/{oldRoleId}", 1, 2).contentType(MediaType.APPLICATION_JSON).content(body)).andExpect(status().isConflict()).andExpect(content().string(containsString("Mapping already exists")));
        }
    }
}
