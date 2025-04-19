package org.hsse.news.database.userrequest;

import lombok.SneakyThrows;
import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.entity.UserRequestEntity;
import org.hsse.news.database.entity.WebsiteEntity;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.hsse.news.database.userrequest.exception.IncorrectURLException;
import org.hsse.news.database.userrequest.exception.RequestAlreadyExistsException;
import org.hsse.news.database.userrequest.model.TopUserRequests;
import org.hsse.news.database.userrequest.model.UserRequestDto;
import org.hsse.news.database.userrequest.model.UserRequestStat;
import org.hsse.news.database.userrequest.repository.JpaUserRequestRepository;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.repositories.JpaWebsitesRepository;
import org.hsse.news.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
public class UserRequestService {

    private final JpaUserRequestRepository userRequestRepository;
    private final JpaWebsitesRepository websitesRepository;
    private final JpaUsersRepository usersRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UserRequestService.class);

    public UserRequestService(final JpaUserRequestRepository userRequestRepository,final JpaWebsitesRepository websitesRepository,final JpaUsersRepository usersRepository) {
        this.userRequestRepository = userRequestRepository;
        this.websitesRepository = websitesRepository;
        this.usersRepository = usersRepository;
    }

    @SneakyThrows
    @Transactional
    public UserRequestDto addUserRequest(final UserId userId, final String url){
        LOG.debug("Method addUserRequest called");
        final UserEntity user = usersRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        final String normalizedUrl = UrlUtils.normalizeUrl(url);
        if (!UrlUtils.isUrlAccessible(normalizedUrl)){
            throw new IncorrectURLException(url + " некорректный URL");
        }
        final Optional<WebsiteEntity> websiteEntity = websitesRepository.findByUrlStartingWith(normalizedUrl);
        if (websiteEntity.isPresent()){
            throw new WebsiteAlreadyExistsException("Сайт с url "+url+" уже существует");
        }
        final Optional<UserRequestEntity> userRequest = userRequestRepository.getRequestByUserIdAndUrl(userId.value(), normalizedUrl);
        if (userRequest.isPresent()){
            throw new RequestAlreadyExistsException("Запрос с таким "+url+" уже отправлен");
        }
        user.assignRequest(new UserRequestEntity(user, normalizedUrl, Instant.now(Clock.system(ZoneId.of("Europe/London")))));
        final UserEntity userEntity = usersRepository.save(user);
        final UserRequestEntity userRequestEntity = userEntity.getUserRequests().stream()
                .filter(request -> request.getUrl().equals(normalizedUrl))
                .findFirst().get();
        return userRequestEntity.toDto();
    }

    @Transactional
    public void removeRequest(final Long id){
        LOG.debug("Method removeRequest called");
        userRequestRepository.deleteById(id);
    }

    @Transactional
    public void removeRequestsByUrl(final String url){
        LOG.debug("Method removeRequestsByUrl called");
        userRequestRepository.deleteAllByUrl(url);
    }

    @Transactional(readOnly = true)
    public Instant getLastRequestByUserId(final UserId userId){
        LOG.debug("Method getLastRequestByUserId called");
        return userRequestRepository.getLastRequestTimeByUserId(userId.value()).orElse(Instant.MIN);
    }

    @Transactional(readOnly = true)
    public TopUserRequests getTopRequests(){
        LOG.debug("Method getTopRequests called");
        final List<UserRequestStat> userRequestStatList = new ArrayList<>();
        final List<String> requestUrls = userRequestRepository.getTopOfRequests();
        for (final String url : requestUrls){
            final UserRequestStat userRequestStat = new UserRequestStat(url, userRequestRepository.getCountOfRequest(url).orElse(0));
            userRequestStatList.add(userRequestStat);
        }
        return new TopUserRequests(userRequestStatList);
    }


}
