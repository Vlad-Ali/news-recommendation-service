package org.hsse.news.bot.website;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.bot.BotMapping;
import org.hsse.news.bot.ChatId;
import org.hsse.news.bot.Message;
import org.hsse.news.bot.TelegramBot;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.hsse.news.util.ParserMainPartUrl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class WebsitesBotHandlers {
    private final static String MENU_COMMAND = "/menu";
    private final static String WEBSITES_MENU_COMMAND = "/websites";
    private final static String LIST_SUBBED_WEBSITES_COMMAND = "/sub-websites";
    private final static String LIST_NOT_SUBBED_WEBSITES_COMMAND = "/unsub-websites";
    private final static String RECOMMENDED_WEBSITES = "/recommended-websites";
    private final static String RECOMMEND_WEBSITE = "/recommend-website";
    private final static String VIEW_WEBSITE_COMMAND = "/view-website";
    private final static String VIEW_WEBSITE_COMMAND_FROM_RECOMMENDATIONS = "/view-website-from-recommendations";
    private final static String SUB_WEBSITE_FROM_RECOMMENDATIONS = "/sub-website-from-recommendations";
    private final static String SUB_WEBSITE_COMMAND = "/sub-website";
    private final static String UNSUB_WEBSITE_COMMAND = "/unsub-website";
    private final static String SUB_CUSTOM_WEBSITE_COMMAND = "/sub-custom-website";
    private final static String DELETE_CUSTOM_WEBSITE = "/delete-custom-website";

    private final static String BACK_TEXT = "Назад";

    private final WebsitesDataProvider websitesDataProvider;
    private TelegramBot bot;

    public WebsitesBotHandlers(final WebsitesDataProvider websitesDataProvider) {
        this.websitesDataProvider = websitesDataProvider;
    }

    @Autowired
    @Lazy
    public void setBot(final TelegramBot bot) {
        this.bot = bot;
    }

    public void sendMessage(final ChatId chatId, final String text) {
        bot.sendMessage(chatId, Message.builder().text(text).build());
    }

    private InlineKeyboardMarkup websiteMenuKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("Подписан")
                        .callbackData(LIST_SUBBED_WEBSITES_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Не подписан")
                        .callbackData(LIST_NOT_SUBBED_WEBSITES_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Добавить свой сайт")
                        .callbackData(SUB_CUSTOM_WEBSITE_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Рекомендация по сайтам")
                        .callbackData(RECOMMENDED_WEBSITES).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(MENU_COMMAND).build())));
    }

    @BotMapping(WEBSITES_MENU_COMMAND)
    public Message websitesMenu() {
        return Message.builder().text("Источники").keyboard(websiteMenuKeyboard()).build();
    }

    private Message buildWebsitesListMenu(final String text, final List<WebsiteInfo> websites) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(
                websites.stream().map(
                        website -> InlineKeyboardButton.builder()
                                .text(website.description())
                                .callbackData(VIEW_WEBSITE_COMMAND + " " + website.websiteId())
                                .build()).toList());
        buttons.add(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(WEBSITES_MENU_COMMAND).build());
        return Message.builder().text(text).verticalKeyboard(buttons).build();
    }

    @SneakyThrows
    private Message viewWebsiteMessage(final Long websiteId, final ChatId chatId) {
        final WebsiteInfo website = websitesDataProvider.findWebsite(websiteId).orElseThrow();
        final String subCommand =
                (websitesDataProvider.isSubbedWebsite(chatId.value(), websiteId) ? UNSUB_WEBSITE_COMMAND : SUB_WEBSITE_COMMAND)
                        + " " + websiteId;
        if (!websitesDataProvider.isCustomCreatedWebsiteByUser(chatId.value(), websiteId)) {
            return Message.builder().text(website.description() + "\n" + ParserMainPartUrl.extractBaseUrlFromRss(website.url()))
                    .verticalKeyboard(List.of(
                            InlineKeyboardButton.builder()
                                    .text(websitesDataProvider.isSubbedWebsite(chatId.value(), websiteId) ? "Отписаться" : "Подписаться")
                                    .callbackData(subCommand).build(),
                            InlineKeyboardButton.builder()
                                    .text(BACK_TEXT)
                                    .callbackData(websitesDataProvider.isSubbedWebsite(chatId.value(), websiteId)
                                            ? LIST_SUBBED_WEBSITES_COMMAND
                                            : LIST_NOT_SUBBED_WEBSITES_COMMAND)
                                    .build())).build();
        }
        return Message.builder().text(website.description() + "\n" + ParserMainPartUrl.extractBaseUrlFromRss(website.url()))
                .verticalKeyboard(List.of(InlineKeyboardButton.builder()
                                .text(websitesDataProvider.isSubbedWebsite(chatId.value(), websiteId) ? "Отписаться" : "Подписаться")
                                .callbackData(subCommand).build(),
                        InlineKeyboardButton.builder()
                                .text("Удалить свой созданный сайт")
                                .callbackData(DELETE_CUSTOM_WEBSITE + " " + websiteId).build(),
                        InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(websitesDataProvider.isSubbedWebsite(chatId.value(), websiteId)
                                        ? LIST_SUBBED_WEBSITES_COMMAND
                                        : LIST_NOT_SUBBED_WEBSITES_COMMAND)
                                .build())).build();
    }

    private Message subOrUnSubWebsite(final Long websiteId,final ChatId chatId) {
        if (websitesDataProvider.isSubbedWebsite(chatId.value(), websiteId)) {
            websitesDataProvider.unSubWebsite(chatId.value(), websiteId);
            return viewUserSubWebsites(chatId);
        } else {
            if (websitesDataProvider.subWebsite(chatId.value(), websiteId)) {
                return viewUserUnSubWebsites(chatId);
            }
            sendMessage(chatId, "Превышен лимит по выбранным сайтам");
            return viewUserUnSubWebsites(chatId);
        }
    }

    private Message createCustomWebsite(final String text) {
        final List<String> args = Arrays.stream(text.split(" ")).toList();
        final long chatId = Long.parseLong(args.get(1));
        final String url = args.get(0);
        final String description = args.get(0);
        try {
            websitesDataProvider.createCustomWebsite(chatId, url, description);
            return Message.builder().text("Источник " + url + " добавлен").keyboard(websiteMenuKeyboard()).build();
        } catch (WebsiteRSSNotValidException | WebsiteAlreadyExistsException e) {
            log.debug("url not valid");
            sendMessage(new ChatId(chatId), e.getMessage());
            return Message.builder().text("Источник " + url + " не добавлен").keyboard(websiteMenuKeyboard()).build();
        }
    }

    private Message showUserSubTopics(final String text,final List<TopicInfo> topics) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(
                topics.stream().map(
                        topic -> InlineKeyboardButton.builder()
                                .text(topic.description())
                                .callbackData(RECOMMEND_WEBSITE + " " + topic.topicID())
                                .build()).toList());
        buttons.add(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(WEBSITES_MENU_COMMAND).build());
        return Message.builder().text(text).verticalKeyboard(buttons).build();
    }

    private Message showRecommendedWebsites(final String text,final List<WebsiteInfo> websites) {
        final List<String> args = Arrays.stream(text.split(" ")).toList();
        final String viewText = args.get(0).concat(" " + args.get(1));
        final Long topicId = Long.parseLong(args.get(2));
        final List<InlineKeyboardButton> buttons = new ArrayList<>(
                websites.stream().map(
                        website -> InlineKeyboardButton.builder()
                                .text(website.description())
                                .callbackData(VIEW_WEBSITE_COMMAND_FROM_RECOMMENDATIONS + " " + topicId + "&" + website.websiteId())
                                .build()).toList());
        buttons.add(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(RECOMMENDED_WEBSITES).build());
        return Message.builder().text(viewText).verticalKeyboard(buttons).build();
    }

    @SneakyThrows
    private Message viewRecommendedWebsite(final Long topicId, final Long websiteId) {
        final WebsiteInfo website = websitesDataProvider.findWebsite(websiteId).orElseThrow();
        final String subCommand =
                SUB_WEBSITE_FROM_RECOMMENDATIONS + " " + topicId
                        + "&" + websiteId;
        return Message.builder().text(website.description() + "\n" + ParserMainPartUrl.extractBaseUrlFromRss(website.url()))
                .verticalKeyboard(List.of(InlineKeyboardButton.builder()
                                .text("Подписаться")
                                .callbackData(subCommand).build(),
                        InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(RECOMMEND_WEBSITE + " " + topicId)
                                .build())).build();
    }

    private Message subRecommendedWebsite(final Long topicId, final Long websiteId, final Long chatId) {
        if (websitesDataProvider.subWebsite(chatId, websiteId)) {
            return viewRecommendedWebsites(String.valueOf(topicId), new ChatId(chatId));
        }
        sendMessage(new ChatId(chatId), "Превышен лимит по выбранным сайтам");
        return viewRecommendedWebsites(String.valueOf(topicId), new ChatId(chatId));
    }

    @BotMapping(LIST_SUBBED_WEBSITES_COMMAND)
    public Message viewUserSubWebsites(final ChatId chatId) {
        return buildWebsitesListMenu("Подписки:", websitesDataProvider.getSubbedWebsites(chatId.value()));
    }

    @BotMapping(LIST_NOT_SUBBED_WEBSITES_COMMAND)
    public Message viewUserUnSubWebsites(final ChatId chatId) {
        return buildWebsitesListMenu("Вы не подписаны на:", websitesDataProvider
                .getUnsubbedWebsites(chatId.value()));
    }

    @BotMapping(VIEW_WEBSITE_COMMAND)
    public Message viewWebsite(final String arg, final ChatId chatId) {
        final Long websiteId = Long.parseLong(arg);
        return viewWebsiteMessage(websiteId, chatId);
    }

    @BotMapping(SUB_WEBSITE_COMMAND)
    public Message subWebsite(final String arg, final ChatId chatId) {
        final Long websiteId = Long.parseLong(arg);
        return subOrUnSubWebsite(websiteId, chatId);
    }

    @BotMapping(UNSUB_WEBSITE_COMMAND)
    public Message unSubWebsite(final String arg, final ChatId chatId) {
        final Long websiteId = Long.parseLong(arg);
        return subOrUnSubWebsite(websiteId, chatId);
    }

    @BotMapping(SUB_CUSTOM_WEBSITE_COMMAND)
    public Message subCustomWebsite(final ChatId chatId) {
        return Message.builder().text("Введите URI:").singleButton(
                InlineKeyboardButton.builder()
                        .text("Отмена")
                        .callbackData(WEBSITES_MENU_COMMAND).build()
        ).onNextMessage(text -> createCustomWebsite(text.concat(" " + chatId.value()))).build();
    }

    @BotMapping(RECOMMENDED_WEBSITES)
    public Message showUserSubTopicsForRecommendedWebsites(final ChatId chatId) {
        return showUserSubTopics("Выбранные темы", websitesDataProvider.getUserSubTopics(chatId.value()));
    }

    @BotMapping(RECOMMEND_WEBSITE)
    public Message viewRecommendedWebsites(final String arg,final ChatId chatId) {
        final Long topicId = Long.parseLong(arg);
        return showRecommendedWebsites("Рекомендованные сайты" + " " + topicId, websitesDataProvider.recommendWebsitesByTopic(chatId.value(), topicId));
    }

    @BotMapping(VIEW_WEBSITE_COMMAND_FROM_RECOMMENDATIONS)
    public Message viewRecommendedWebsite(final String args,final ChatId chatId) {
        final List<String> stringList = Arrays.stream(args.split("&")).toList();
        log.debug("stringList {}", stringList);
        final Long topicId = Long.parseLong(stringList.get(0));
        final Long websiteId = Long.parseLong(stringList.get(1));
        return viewRecommendedWebsite(topicId, websiteId);
    }

    @BotMapping(SUB_WEBSITE_FROM_RECOMMENDATIONS)
    public Message subWebsiteFromRecommendations(final String args,final ChatId chatId) {
        final List<String> stringList = Arrays.stream(args.split("&")).toList();
        final Long topicId = Long.parseLong(stringList.get(0));
        final Long websiteId = Long.parseLong(stringList.get(1));
        return subRecommendedWebsite(topicId, websiteId, chatId.value());
    }
}

