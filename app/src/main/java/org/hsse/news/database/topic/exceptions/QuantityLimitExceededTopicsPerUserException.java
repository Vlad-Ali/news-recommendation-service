package org.hsse.news.database.topic.exceptions;

import org.hsse.news.database.user.models.UserId;

public class QuantityLimitExceededTopicsPerUserException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User with id=%s exceeded quantity limit topics";
    private static final String DEFAULT_MESSAGE_1 = "Exceeded quantity limit topics = 10";
    public QuantityLimitExceededTopicsPerUserException(final UserId userId){
        super(String.format(DEFAULT_MESSAGE, userId));
    }

    public QuantityLimitExceededTopicsPerUserException(final String message) {
        super(message);
    }

    public QuantityLimitExceededTopicsPerUserException(){super(DEFAULT_MESSAGE_1);}
}
