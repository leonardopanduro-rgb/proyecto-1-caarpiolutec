package com.dbp.democarpultec.security;

import com.dbp.democarpultec.PostgresContainerTest;
import com.dbp.democarpultec.model.User;
import com.dbp.democarpultec.model.enums.Role;
import com.dbp.democarpultec.repository.UserRepository;
import com.dbp.democarpultec.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class SecurityIntegrationTest extends PostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Test
    void shouldAllowPublicPublicationListingWithoutToken() throws Exception {
        mockMvc.perform(get("/api/publications"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowHealthCheckWithoutTokenForLoadBalancer() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void shouldAllowRegistrationRouteWithoutToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnUnauthorizedWhenProtectedEndpointHasNoToken() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.path").value("/api/users/me"));
    }

    @Test
    void shouldRequireAuthenticationForSensitiveReadEndpoints() throws Exception {
        for (String endpoint : List.of(
                "/api/vehicles",
                "/api/request-publications",
                "/api/rides",
                "/api/ride-passengers",
                "/api/reviews")) {
            mockMvc.perform(get(endpoint))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void shouldReturnUnauthorizedWhenProtectedEndpointHasInvalidToken() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnUnauthorizedWhenCreatingPublicationWithoutToken() throws Exception {
        mockMvc.perform(post("/api/publications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnCurrentUserWhenTokenIsValid() throws Exception {
        User user = saveUser("juan.security@utec.edu.pe", Role.USER);
        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.email").value("juan.security@utec.edu.pe"))
                .andExpect(jsonPath("$.role").value("USER"));

        org.junit.jupiter.api.Assertions.assertEquals(user.getId(), jwtService.extractUserId(token));
        org.junit.jupiter.api.Assertions.assertEquals(Role.USER, jwtService.extractRole(token));
    }

    @Test
    void shouldIssueNewTokensWhenRefreshTokenIsValid() throws Exception {
        User user = saveUser("refresh.user@utec.edu.pe", Role.USER);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + jwtService.generateRefreshToken(user) + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.user.email").value("refresh.user@utec.edu.pe"));
    }

    @Test
    void shouldNotAuthenticateWhenRefreshTokenIsUsedAsAccessToken() throws Exception {
        User user = saveUser("refresh.header@utec.edu.pe", Role.USER);

        mockMvc.perform(get("/api/users/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateRefreshToken(user)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnForbiddenWhenUserRoleReadsAdministrativeUsersEndpoint() throws Exception {
        User user = saveUser("basic.user@utec.edu.pe", Role.USER);

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(user)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void shouldAllowAdminRoleToReadAdministrativeUsersEndpoint() throws Exception {
        User admin = saveUser("admin@utec.edu.pe", Role.ADMIN);

        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.generateToken(admin)))
                .andExpect(status().isOk());
    }

    private User saveUser(String email, Role role) {
        return userRepository.saveAndFlush(User.builder()
                .name("Juan")
                .lastName("Perez")
                .email(email)
                .passwordHash(passwordEncoder.encode("Password123"))
                .role(role)
                .build());
    }
}
