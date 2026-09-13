package com.portfolio.hr_system;

import com.portfolio.hr_system.entity.*;
import com.portfolio.hr_system.repository.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.MediaType;
import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test") @Transactional
class HrSystemApplicationTests {
    @Autowired MockMvc mvc;
    @Autowired EmployeeRepository employees;
    @Autowired UserRepository users;
    @Autowired DepartmentRepository departments;
    private static final String EMPLOYEE = "{\"name\":\"田中\",\"email\":\"tanaka@example.test\",\"position\":\"Engineer\"}";

    @Test void anonymousApiRequestReturns401() throws Exception {
        mvc.perform(get("/api/employees")).andExpect(status().isUnauthorized());
    }
    @Test void userCanReadEmployees() throws Exception {
        mvc.perform(get("/api/employees").with(user("reader"))).andExpect(status().isOk());
    }
    @Test void userCannotWriteApiEvenWithCsrf() throws Exception {
        mvc.perform(post("/api/employees").with(user("reader")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE)).andExpect(status().isForbidden());
        mvc.perform(put("/api/employees/1").with(user("reader")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/employees/1").with(user("reader")).with(csrf())).andExpect(status().isForbidden());
    }
    @Test void adminWritesRequireCsrf() throws Exception {
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE)).andExpect(status().isForbidden());
        mvc.perform(post("/departments/save").with(user("admin").roles("ADMIN"))
                .param("name", "Engineering")).andExpect(status().isForbidden());
    }
    @Test void adminCanCreateWithCsrfAndReceivesDto() throws Exception {
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.data.email").value("tanaka@example.test"))
                .andExpect(jsonPath("$.data.deleted").doesNotExist());
        employees.flush();
        Employee saved = employees.findAll().get(0);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("admin");
    }
    @Test void ordinaryUserCannotMutateFormsOrManageUsers() throws Exception {
        for (String url : new String[]{"/users", "/employees/new", "/departments/new"})
            mvc.perform(get(url).with(user("reader"))).andExpect(status().isForbidden());
        for (String url : new String[]{"/employees/save", "/departments/save", "/users/save"})
            mvc.perform(post(url).with(user("reader")).with(csrf())).andExpect(status().isForbidden());
    }
    @Test void getNeverDeletesRecords() throws Exception {
        for (String url : new String[]{"/employees/delete/1", "/departments/delete/1", "/users/delete/1"})
            mvc.perform(get(url).with(user("admin").roles("ADMIN"))).andExpect(status().isMethodNotAllowed());
    }
    @Test void missingResourcesReturn404() throws Exception {
        mvc.perform(get("/api/employees/999").with(user("reader"))).andExpect(status().isNotFound());
        mvc.perform(put("/api/employees/999").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE)).andExpect(status().isNotFound());
        mvc.perform(delete("/api/employees/999").with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isNotFound());
    }
    @Test void invalidEmailAndMalformedJsonReturn400() throws Exception {
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE.replace("tanaka@example.test", "")))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.data.email").exists());
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
    }
    @Test void duplicateEmailReturns409() throws Exception {
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE)).andExpect(status().isCreated());
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE)).andExpect(status().isConflict());
    }
    @Test void softDeletedEmployeesDisappearFromSearchAndCounts() throws Exception {
        Employee e = new Employee(); e.setName("Tanaka"); e.setEmail("soft@example.test");
        employees.saveAndFlush(e);
        mvc.perform(delete("/api/employees/" + e.getId()).with(user("admin").roles("ADMIN")).with(csrf()))
                .andExpect(status().isOk());
        employees.flush();
        mvc.perform(get("/api/employees/" + e.getId()).with(user("reader"))).andExpect(status().isNotFound());
        assertThat(employees.count()).isZero();
        mvc.perform(get("/api/employees/search").with(user("reader"))).andExpect(jsonPath("$.data.totalElements").value(0));
    }
    @Test void invalidEmploymentDatesReturn400() throws Exception {
        mvc.perform(post("/api/employees").with(user("admin").roles("ADMIN")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(EMPLOYEE.replace("}", ",\"employmentStatus\":\"RETIRED\"}")))
                .andExpect(status().isBadRequest());
    }
    @Test void pagesRenderForBothRolesAndNeverExposePasswordHash() throws Exception {
        User account = new User(); account.setUsername("test-admin"); account.setRole(Role.ADMIN);
        account.setPassword("a-private-hash"); users.saveAndFlush(account);
        Department department = new Department(); department.setName("Engineering"); departments.saveAndFlush(department);
        for (String url : new String[]{"/dashboard", "/employees", "/departments", "/users", "/users/new",
                "/users/edit/"+account.getId(), "/departments/new", "/departments/edit/"+department.getId(), "/employees/new"}) {
            mvc.perform(get(url).with(user("admin").roles("ADMIN"))).andExpect(status().isOk())
                    .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("a-private-hash"))));
        }
        mvc.perform(get("/employees").with(user("reader"))).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString("id=\"employeeForm\""))));
        mvc.perform(get("/login")).andExpect(status().isOk());
    }
    @Test void departmentApiIsPagedAndHidesAuditFields() throws Exception {
        Department department = new Department(); department.setName("Engineering"); departments.saveAndFlush(department);
        mvc.perform(get("/api/departments?size=1").with(user("reader"))).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Engineering"))
                .andExpect(jsonPath("$.data.content[0].createdBy").doesNotExist());
    }
    @Test void swaggerIsNotPublic() throws Exception {
        mvc.perform(get("/swagger-ui/index.html").with(user("reader"))).andExpect(status().isForbidden());
        mvc.perform(get("/v3/api-docs").with(user("reader"))).andExpect(status().isForbidden());
    }
}
