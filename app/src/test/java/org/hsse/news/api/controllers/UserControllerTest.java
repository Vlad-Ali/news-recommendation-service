package org.hsse.news.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.hsse.news.api.configuration.SecurityConfig;
import org.hsse.news.api.schemas.request.user.UserPasswordChangeRequest;
import org.hsse.news.api.schemas.request.user.UserRegisterRequest;
import org.hsse.news.api.schemas.shared.UserInfo;
import org.hsse.news.database.jwt.JwtService;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.InvalidCurrentPasswordException;
import org.hsse.news.database.user.exceptions.SameNewPasswordException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.util.SampleDataUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    private final static String AUTHORIZATION_HEADER_NAME = "Authorization";

    @Test
    void testRegister() throws Exception {
        final UserRegisterRequest request = new UserRegisterRequest(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD,
                SampleDataUtil.DEFAULT_USER.username());

        when(userService.register(request)).thenReturn(
                SampleDataUtil.DEFAULT_USER.withPasswordHash(SampleDataUtil.DEFAULT_PASSWORD_HASH));
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.generateToken(SampleDataUtil.DEFAULT_USER.id())).thenReturn(SampleDataUtil.DEFAULT_TOKEN);

        mockMvc.perform(post("/user/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(SampleDataUtil.DEFAULT_TOKEN));

        verify(userService).register(request);
    }

    @Test
    void testHandleEmailConflict() throws Exception {
        final UserRegisterRequest request = new UserRegisterRequest(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD,
                SampleDataUtil.DEFAULT_USER.username());

        when(userService.register(request)).thenThrow(new EmailConflictException(null));

        mockMvc.perform(post("/user/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        verify(userService).register(request);
    }

    @Test
    void testSignIn() throws Exception {
        final AuthenticationCredentials request = new AuthenticationCredentials(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD);

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(userService.authenticate(request)).thenReturn(
                Optional.of(SampleDataUtil.DEFAULT_USER.id()));
        when(jwtService.generateToken(SampleDataUtil.DEFAULT_USER.id()))
                .thenReturn(SampleDataUtil.DEFAULT_TOKEN);

        mockMvc.perform(post("/user/sign-in").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string(SampleDataUtil.DEFAULT_TOKEN));

        verify(userService).authenticate(request);
    }

    @Test
    void testGet() throws Exception {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId(SampleDataUtil.DEFAULT_TOKEN)).thenReturn(SampleDataUtil.DEFAULT_USER.id());
        when(userService.findById(SampleDataUtil.DEFAULT_USER.id())).thenReturn(
                Optional.of(SampleDataUtil.DEFAULT_USER));

        mockMvc.perform(get("/user").with(csrf())
                        .header(AUTHORIZATION_HEADER_NAME,
                                SampleDataUtil.DEFAULT_AUTHORIZATION_HEADER))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new UserInfo(
                        SampleDataUtil.DEFAULT_USER.email(),
                        SampleDataUtil.DEFAULT_USER.username()))));

        verify(jwtService).getUserId(SampleDataUtil.DEFAULT_TOKEN);
        verify(userService).findById(SampleDataUtil.DEFAULT_USER.id());
    }

    @Test
    void testUpdate() throws Exception {
        final UserInfo request = new UserInfo(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_USER.username());

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId(SampleDataUtil.DEFAULT_TOKEN)).thenReturn(SampleDataUtil.DEFAULT_USER.id());

        mockMvc.perform(put("/user").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(AUTHORIZATION_HEADER_NAME,
                                SampleDataUtil.DEFAULT_AUTHORIZATION_HEADER))
                .andExpect(status().isNoContent());

        verify(userService).update(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_USER.username());
    }

    @Test
    void testChangePassword() throws Exception {
        final UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId(SampleDataUtil.DEFAULT_TOKEN))
                .thenReturn(SampleDataUtil.DEFAULT_USER.id());

        mockMvc.perform(put("/user/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(AUTHORIZATION_HEADER_NAME,
                                SampleDataUtil.DEFAULT_AUTHORIZATION_HEADER))
                .andExpect(status().isNoContent());

        verify(userService).updatePassword(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);
    }

    @Test
    void testHandleSameNewPassword() throws Exception {
        final UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId(SampleDataUtil.DEFAULT_TOKEN)).thenReturn(SampleDataUtil.DEFAULT_USER.id());
        doThrow(new SameNewPasswordException()).when(userService)
                .updatePassword(SampleDataUtil.DEFAULT_USER.id(),
                        SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);

        mockMvc.perform(put("/user/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(AUTHORIZATION_HEADER_NAME,
                                SampleDataUtil.DEFAULT_AUTHORIZATION_HEADER))
                .andExpect(status().isAlreadyReported());

        verify(userService).updatePassword(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);
    }

    @Test
    void testHandleInvalidCurrentPassword() throws Exception {
        final UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId("token")).thenReturn(SampleDataUtil.DEFAULT_USER.id());
        doThrow(new InvalidCurrentPasswordException()).when(userService)
                .updatePassword(SampleDataUtil.DEFAULT_USER.id(),
                        SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);

        mockMvc.perform(put("/user/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header(AUTHORIZATION_HEADER_NAME,
                                SampleDataUtil.DEFAULT_AUTHORIZATION_HEADER))
                .andExpect(status().isPreconditionFailed());

        verify(userService).updatePassword(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.NEW_PASSWORD);
    }
}
