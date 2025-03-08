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
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void testRegister() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                SampleDataUtil.DEFAULT_USER.email(),
                "password",
                SampleDataUtil.DEFAULT_USER.username());

        when(userService.register(request)).thenReturn(
                SampleDataUtil.DEFAULT_USER.withPasswordHash("password hash"));
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.generateToken(SampleDataUtil.DEFAULT_USER.id())).thenReturn("token");

        mockMvc.perform(post("/user/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));

        verify(userService).register(request);
    }

    @Test
    void testHandleEmailConflict() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest(
                SampleDataUtil.DEFAULT_USER.email(),
                "password",
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
        AuthenticationCredentials request = new AuthenticationCredentials(
                SampleDataUtil.DEFAULT_USER.email(),
                "password");

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(userService.authenticate(request)).thenReturn(
                Optional.of(SampleDataUtil.DEFAULT_USER.id()));
        when(jwtService.generateToken(SampleDataUtil.DEFAULT_USER.id())).thenReturn("token");

        mockMvc.perform(post("/user/sign-in").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("token"));

        verify(userService).authenticate(request);
    }

    @Test
    void testGet() throws Exception {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId("token")).thenReturn(SampleDataUtil.DEFAULT_USER.id());
        when(userService.findById(SampleDataUtil.DEFAULT_USER.id())).thenReturn(
                Optional.of(SampleDataUtil.DEFAULT_USER));

        mockMvc.perform(get("/user").with(csrf())
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(new UserInfo(
                        SampleDataUtil.DEFAULT_USER.email(),
                        SampleDataUtil.DEFAULT_USER.username()))));
    }

    @Test
    void testUpdate() throws Exception {
        UserInfo request = new UserInfo(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_USER.username());

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId("token")).thenReturn(SampleDataUtil.DEFAULT_USER.id());

        mockMvc.perform(put("/user").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(userService).update(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_USER.username());
    }

    @Test
    void testChangePassword() throws Exception {
        UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                "old password", "new password");

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId("token")).thenReturn(SampleDataUtil.DEFAULT_USER.id());

        mockMvc.perform(put("/user/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isNoContent());

        verify(userService).updatePassword(
                SampleDataUtil.DEFAULT_USER.id(),
                "old password", "new password");
    }

    @Test
    void testHandleSameNewPassword() throws Exception {
        UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                "old password", "new password");

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId("token")).thenReturn(SampleDataUtil.DEFAULT_USER.id());
        doThrow(new SameNewPasswordException()).when(userService)
                .updatePassword(SampleDataUtil.DEFAULT_USER.id(),
                        "old password", "new password");

        mockMvc.perform(put("/user/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isAlreadyReported());

        verify(userService).updatePassword(
                SampleDataUtil.DEFAULT_USER.id(),
                "old password", "new password");
    }

    @Test
    void testHandleInvalidCurrentPassword() throws Exception {
        UserPasswordChangeRequest request = new UserPasswordChangeRequest(
                "old password", "new password");

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        when(jwtService.getUserId("token")).thenReturn(SampleDataUtil.DEFAULT_USER.id());
        doThrow(new InvalidCurrentPasswordException()).when(userService)
                .updatePassword(SampleDataUtil.DEFAULT_USER.id(),
                        "old password", "new password");

        mockMvc.perform(put("/user/password").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isPreconditionFailed());

        verify(userService).updatePassword(
                SampleDataUtil.DEFAULT_USER.id(),
                "old password", "new password");
    }
}
