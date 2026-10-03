package com.portfolio.hr_system;

import com.portfolio.hr_system.repository.UserRepository;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.util.UriComponentsBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Uses dummy client credentials. The provider-cancellation callback never calls Google.
@SpringBootTest(properties = {"GOOGLE_CLIENT_ID=test-client-id", "GOOGLE_CLIENT_SECRET=test-client-secret"})
@AutoConfigureMockMvc
@ActiveProfiles({"test", "oauth"})
class GoogleLoginFlowTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @Test void enabledGoogleLoginUsesStateAndDedicatedFailurePage() throws Exception {
        long accountsBefore = users.count();
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("/oauth2/authorization/google")));
        var authorization = mvc.perform(get("/oauth2/authorization/google"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String location = authorization.getResponse().getHeader("Location");
        assertThat(location).startsWith("https://accounts.google.com/");
        String state = UriComponentsBuilder.fromUriString(location).build().getQueryParams().getFirst("state");
        assertThat(state).isNotBlank();
        MockHttpSession session = (MockHttpSession) authorization.getRequest().getSession(false);
        assertThat(session).isNotNull();
        mvc.perform(get("/login/oauth2/code/google").session(session)
                        .param("error", "access_denied")
                        .param("state", URLDecoder.decode(state, StandardCharsets.UTF_8)))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?oauthError"));
        assertThat(users.count()).isEqualTo(accountsBefore);
    }
}
