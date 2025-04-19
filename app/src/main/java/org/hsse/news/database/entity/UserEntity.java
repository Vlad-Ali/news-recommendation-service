package org.hsse.news.database.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"email"}), @UniqueConstraint(columnNames = {"chat_id"})})
public class UserEntity{
    @Id
    @Column(name = "user_id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "email")
    @Setter
    @NotNull
    private String email;

    @Column(name = "password")
    @Setter
    @NotNull
    private String password;

    @Column(name = "username")
    @Setter
    @NotNull
    private String username;

    @Column(name = "chat_id")
    @Setter
    @NotNull
    private Long chatId;

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<WebsiteEntity> createdWebsites = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_websites",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "website_id")
    )
    private Set<WebsiteEntity> subscribedWebsites = new HashSet<>();

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TopicEntity> createdTopics = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserArticlesEntity> userArticles = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "user_topics",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    private Set<TopicEntity> subscribedTopics = new HashSet<>();

    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRequestEntity> userRequests = new HashSet<>();

    protected UserEntity() {}

    public UserEntity(final String email,final String password,final String username, final Long chatId) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.chatId = chatId;
    }

    public void assignRequest(final UserRequestEntity userRequest){
        userRequests.add(userRequest);
        userRequest.setUser(this);
    }

    public void assignArticle(final ArticleEntity articleEntity,final Integer grade){
        userArticles.add(new UserArticlesEntity(this, articleEntity, grade));
    }

    public void addWebsite(final WebsiteEntity website) {
        createdWebsites.add(website);
        website.setCreator(this);
    }

    public void removeWebsite(final WebsiteEntity website) {
        createdWebsites.remove(website);
        website.setCreator(null);
    }

    public void addTopic(final TopicEntity topic){
        createdTopics.add(topic);
        topic.setCreator(this);
    }

    public void removeTopic(final TopicEntity topic){
        createdTopics.remove(topic);
        topic.setCreator(null);
    }

    public void subscribeToWebsite(final WebsiteEntity website) {
        subscribedWebsites.add(website);
        website.getSubscribers().add(this);
    }

    public void unsubscribeFromWebsite(final WebsiteEntity website) {
        subscribedWebsites.remove(website);
        website.getSubscribers().remove(this);
    }

    public void subscribeToTopic(final TopicEntity topic){
        subscribedTopics.add(topic);
        topic.getSubscribers().add(this);
    }

    public void unsubscribeFromTopic(final TopicEntity topic){
        subscribedTopics.remove(topic);
        topic.getSubscribers().remove(this);
    }

    public UUID getId() {
        return id;
    }

    public @NotNull String getEmail() {
        return email;
    }

    public void setEmail(final @NotNull String email) {
        this.email = email;
    }

    public @NotNull String getPassword() {
        return password;
    }

    public void setPassword(final @NotNull String password) {
        this.password = password;
    }

    public @NotNull String getUsername() {
        return username;
    }

    public void setUsername(final @NotNull String username) {
        this.username = username;
    }

    public Set<WebsiteEntity> getCreatedWebsites() {
        return createdWebsites;
    }

    public Set<WebsiteEntity> getSubscribedWebsites() {
        return subscribedWebsites;
    }

    public @NotNull Long getChatId() {
        return chatId;
    }

    public Set<TopicEntity> getCreatedTopics() {
        return createdTopics;
    }

    public Set<TopicEntity> getSubscribedTopics() {
        return subscribedTopics;
    }

    public UserDto toUserDto(){
        final UUID id = this.getId();
        final String email = this.getEmail();
        final String username = this.getUsername();
        final String password = this.getPassword();
        final Long chatId = this.getChatId();
        return new UserDto(new UserId(id), email, password, username, chatId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof UserEntity user)) {
            return false;
        }
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return UserEntity.class.hashCode();
    }

    public Set<UserArticlesEntity> getUserArticles() {
        return userArticles;
    }
}
