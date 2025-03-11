package org.hsse.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

@Component
public class NewsBot extends AbilityBot {
    @Autowired
    public NewsBot(Environment environment) {
        super(environment.getProperty("bot-token"), "HsseNewsTeam1Bot");
    }

    @Override
    public long creatorId() {
        return 1;
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info("Starts working with a bot")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    SendMessage message = new SendMessage();
                    message.setChatId(ctx.chatId());
                    message.setText("Welcome!");

                    silent.execute(message);
                })
                .build();
    }
}
