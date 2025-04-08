package org.hsse.news.bot;

import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Configuration
@Setter
public class TelegramBotConfiguration implements ApplicationContextAware {
    private ApplicationContext applicationContext;

    public Object parseArg(final String arg, final Class<?> type) {
        if (type == String.class) {
            return arg;
        } else if (type == int.class) {
            return Integer.parseInt(arg);
        } else if (type == long.class) {
            return Long.parseLong(arg);
        } else {
            try {
                return type.getMethod("fromString", String.class)
                        .invoke(null, arg);
            } catch (ReflectiveOperationException ignored) {
            }

            try {
                return type.getMethod("valueOf", String.class)
                        .invoke(null, arg);
            } catch (ReflectiveOperationException ignored) {
            }

            throw new IllegalArgumentException("Can't create a parameter of type " + type);
        }
    }

    public static Class<?> parameterType(final Method method, final int parameterIndex) {
        return new MethodParameter(method, parameterIndex).getParameterType();
    }

    @SneakyThrows
    private Optional<Message> runMethod(
            final Object object, final Method method,
            final List<String> args, final ChatId chatId) {
        final List<String> mutableArgs = new ArrayList<>(args);
        final List<Object> methodArgs = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            final Class<?> parameterType = parameterType(method, i);
            if (parameterType == TelegramBot.class) {
                methodArgs.add(this);
            } else if (parameterType == ChatId.class) {
                methodArgs.add(chatId);
            } else {
                methodArgs.add(parseArg(mutableArgs.remove(0),
                        parameterType));
            }
        }

        Object result;
        result = method.invoke(object, methodArgs.toArray());

        if (result instanceof Message) {
            return Optional.of((Message) result);
        } else {
            return Optional.empty();
        }
    }

    @Bean
    public TelegramBot telegramBot(final @Value("${tg-bot.token}") String token) {
        final TelegramBot bot = new TelegramBot(token);
        for (final String beanName : applicationContext.getBeanDefinitionNames()) {
            if ("telegramBot".equals(beanName)) {
                continue;
            }

            final Object bean = applicationContext.getBean(beanName);
            final Class<?> beanClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.doWithMethods(beanClass, (method) -> {
                final BotMapping annotation =
                        AnnotationUtils.findAnnotation(method, BotMapping.class);
                if (annotation == null) {
                    return;
                }


                bot.addCommand(annotation.value(), (args, chatId) ->
                        runMethod(bean, method, args, chatId));
            });
        }
        return bot;
    }
}
