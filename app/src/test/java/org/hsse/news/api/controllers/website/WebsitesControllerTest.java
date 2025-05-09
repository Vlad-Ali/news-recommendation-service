package org.hsse.news.api.controllers.website;

/*import org.hsse.news.Application;
import org.hsse.news.api.configuration.SecurityConfig;
import org.hsse.news.api.controllers.UserController;
import org.hsse.news.api.filters.JwtTokenFilter;
import org.hsse.news.api.schemas.request.website.CustomWebsiteCreateRequest;
import org.hsse.news.api.schemas.request.website.SubWebsitesUpdateRequest;
import org.hsse.news.api.schemas.response.website.WebsitesResponse;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.jwt.JwtService;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.util.SampleDataUtil;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebsitesController.class)
@ContextConfiguration(classes = {Application.class, SecurityConfig.class, JwtTokenFilter.class, JwtService.class, UserController.class})
public class WebsitesControllerTest {
    private final WebsiteInfo testWebsiteInfo = SampleDataUtil.DEFAULT_WEBSITE_INFO;
    private final WebsiteDto testWebsiteDto = SampleDataUtil.DEFAULT_WEBSITE_DTO;
    private final UserDto testUserDto = SampleDataUtil.DEFAULT_USER_DTO;
    private static final String USER_REGISTER = "{\"email\":\"test@example.com\", \"password\":\"test_password\",\"username\":\"TestUser\"}";
    private static final String USER_SIGN_IN = "{\"email\":\"test@example.com\", \"password\":\"test_password\"}";
    private static final String SUB_WEBSITES_UPDATE = "{\"websiteIds\":[1,2]}";
    private static final String CUSTOM_WEBSITE_CREATE = "{\"url\":\"https://alex.com/RSS\", \"decsription\":\"xxxx\"}";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private final WebsitesResponse testWebsitesResponse = new WebsitesResponse(List.of(SampleDataUtil.DEFAULT_WEBSITE_INFO), List.of(SampleDataUtil.NEW_WEBSITE_INFO));
    private final SubWebsitesUpdateRequest testSubWebsitesUpdateRequest = new SubWebsitesUpdateRequest(List.of(1L,2L));
    private final CustomWebsiteCreateRequest testCustomWebsiteCreateRequest = new CustomWebsiteCreateRequest(SampleDataUtil.DEFAULT_WEBSITE_DTO.url(), SampleDataUtil.DEFAULT_WEBSITE_DTO.description());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WebsiteService websiteService;

    @MockitoBean
    private TopicService topicService;

    @BeforeEach
    void beforeEach() throws Exception {
        when(userService.register(any(UserDto.class))).thenReturn(testUserDto);
        mockMvc.perform(post("/user/register").contentType(CONTENT_TYPE_JSON).content(USER_REGISTER))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    // Method to get jwt user's token
    private String getUserToken() throws Exception {
        when(userService.authenticate(any(AuthenticationCredentials.class))).thenReturn(Optional.of(testUserDto.id()));
        return mockMvc.perform(post("/user/sign-in").contentType(CONTENT_TYPE_JSON).content(USER_SIGN_IN))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

    }


    @Test
    public void shouldReturnWebsite() throws Exception {
        final String token = getUserToken();
        when(websiteService.findById(any(WebsiteId.class))).thenReturn(Optional.of(new WebsiteInfo(testWebsiteInfo.websiteId(), testWebsiteInfo.url(), testWebsiteInfo.description())));
        mockMvc.perform(get("/websites/1").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.websiteId").value(testWebsiteInfo.websiteId()))
                .andExpect(jsonPath("$.url").value(testWebsiteInfo.url()))
                .andExpect(jsonPath("$.description").value(testWebsiteInfo.description()));
    }
    @Test
    public void shouldNotReturnWebsite() throws Exception {
        final String token = getUserToken();
        when(websiteService.findById(any(WebsiteId.class))).thenReturn(Optional.empty());
        mockMvc.perform(get("/websites/2").header("Authorization", "Bearer "+token))
              .andExpect(status().isBadRequest());
    }


    @Test
    public void shouldReturnUserWebsites() throws Exception {
        final String token = getUserToken();
        when(websiteService.getSubAndUnSubWebsites(any(UserId.class))).thenReturn(testWebsitesResponse);
        mockMvc.perform(get("/websites/user").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").isArray())
                .andExpect(jsonPath("$.subscribed[0].websiteId").value(testWebsitesResponse.subscribed().get(0).websiteId()))
                .andExpect(jsonPath("$.subscribed[0].url").value(testWebsitesResponse.subscribed().get(0).url()))
                .andExpect(jsonPath("$.subscribed[0].description").value(testWebsitesResponse.subscribed().get(0).description()))
                .andExpect(jsonPath("$.other").isArray())
                .andExpect(jsonPath("$.other[0].websiteId").value(testWebsitesResponse.other().get(0).websiteId()))
                .andExpect(jsonPath("$.other[0].url").value(testWebsitesResponse.other().get(0).url()))
                .andExpect(jsonPath("$.other[0].description").value(testWebsitesResponse.other().get(0).description()));
    }

    @Test
    public void shouldUpdateSubWebsites() throws Exception {
        final String token = getUserToken();
        doNothing().when(websiteService).tryUpdateSubscribedWebsites(eq(testSubWebsitesUpdateRequest.websiteIds().stream().map(WebsiteId::new).toList()), any(UserId.class));
        mockMvc.perform(patch("/websites").contentType(CONTENT_TYPE_JSON).content(SUB_WEBSITES_UPDATE).header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotUpdateSubWebsitesBecauseLimit() throws Exception {
        final String token = getUserToken();
    doThrow(new QuantityLimitExceededWebsitesPerUserException(""))
        .when(websiteService)
        .tryUpdateSubscribedWebsites(
            eq(
                testSubWebsitesUpdateRequest.websiteIds().stream()
                    .map(WebsiteId::new)
                    .toList()),
            any(UserId.class));
        mockMvc.perform(patch("/websites").contentType(CONTENT_TYPE_JSON).content(SUB_WEBSITES_UPDATE).header("Authorization", "Bearer "+token))
                .andExpect(status().isLengthRequired());
    }

    @Test
    public void shouldNotUpdateSubWebsitesBecauseWebsiteNotFound() throws Exception {
        final String token = getUserToken();
        doThrow(new WebsiteNotFoundException(""))
                .when(websiteService)
                .tryUpdateSubscribedWebsites(
                        eq(
                                testSubWebsitesUpdateRequest.websiteIds().stream()
                                        .map(WebsiteId::new)
                                        .toList()),
                        any(UserId.class));
        mockMvc.perform(patch("/websites").contentType(CONTENT_TYPE_JSON).content(SUB_WEBSITES_UPDATE).header("Authorization", "Bearer "+token))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldCreateCustomWebsite() throws Exception {
        final String token = getUserToken();
        when(websiteService.create(any(WebsiteDto.class))).thenReturn(testWebsiteDto);
        mockMvc.perform(post("/websites/custom").contentType(CONTENT_TYPE_JSON).content(CUSTOM_WEBSITE_CREATE).header("Authorization", "Bearer "+token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.websiteId").value(testWebsiteInfo.websiteId()))
                .andExpect(jsonPath("$.url").value(testWebsiteInfo.url()))
                .andExpect(jsonPath("$.description").value(testWebsiteInfo.description()));
    }

    @Test
    public void shouldNotCreateCustomWebsite() throws Exception {
        final String token = getUserToken();
        when(websiteService.create(any(WebsiteDto.class))).thenThrow(new WebsiteAlreadyExistsException(new WebsiteId(5L), testWebsiteDto.url()));
        mockMvc.perform(post("/websites/custom").contentType(CONTENT_TYPE_JSON).content(CUSTOM_WEBSITE_CREATE).header("Authorization", "Bearer "+token))
                .andExpect(status().isConflict());
    }

    @Test
    public void shouldNotCreateCustomWebsiteBecauseNotValidRSS() throws Exception {
        final String token = getUserToken();
        when(websiteService.create(any(WebsiteDto.class))).thenThrow(new WebsiteRSSNotValidException(""));
        mockMvc.perform(post("/websites/custom").contentType(CONTENT_TYPE_JSON).content(CUSTOM_WEBSITE_CREATE).header("Authorization", "Bearer "+token))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    public void shouldDeleteCustomWebsite() throws Exception {
        final String token = getUserToken();
        doNothing().when(websiteService).delete(any(WebsiteId.class), any(UserId.class));
        mockMvc.perform(delete("/websites/custom/1").header("Authorization", "Bearer "+token))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldNotDeleteCustomWebsite() throws Exception {
        final String token = getUserToken();
        doThrow(new WebsiteNotFoundException("")).when(websiteService).delete(any(WebsiteId.class), any(UserId.class));
        mockMvc.perform(delete("/websites/custom/1").header("Authorization", "Bearer "+token))
                .andExpect(status().isBadRequest());
    }

}*/
