package org.hsse.news.emailsender;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.database.userrequest.UserRequestService;
import org.hsse.news.database.userrequest.model.TopUserRequests;
import org.hsse.news.database.userrequest.model.UserRequestStat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Component
@EnableScheduling
@Slf4j
public class EmailSenderService {
    private final UserRequestService userRequestService;
    private final JavaMailSender mailSender;
    private static final Logger LOG = LoggerFactory.getLogger(EmailSenderService.class);

    @Value("${spring.mail.username}")
    private String adminMail;

    public EmailSenderService(final UserRequestService userRequestService,final JavaMailSender mailSender) {
        this.userRequestService = userRequestService;
        this.mailSender = mailSender;
    }

    public void sendEmail(final String toEmail,final String subject,final String body){
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminMail);
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        mailSender.send(message);
        LOG.debug("Message was sent to {}", toEmail);
    }

    @SneakyThrows
    @Scheduled(fixedRate = 60 * 60 * 1000, initialDelay = 10_000)
    public void sendTopRequests(){
        final TopUserRequests topUserRequests = userRequestService.getTopRequests();
        final StringBuilder res = new StringBuilder();
        for (final UserRequestStat userRequestStat : topUserRequests.userRequestStats()){
            res.append(userRequestStat.url()).append(": (Количество: ").append(userRequestStat.count()).append(')').append('\n');
        }
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(adminMail);
        message.setTo(adminMail);
        message.setSubject("Топ запросов на добавление парсера");
        message.setText(res.toString());
        mailSender.send(message);
        LOG.debug("Top requests were sent to admin");
    }
}
