package org.hsse.news.database.website.exceptions;

import org.hsse.news.database.user.models.UserId;

public class QuantityLimitExceededWebsitesPerUserException extends RuntimeException {
    private static final String DEFAULT_MESSAGE = "User with id=%s exceeded quantity limit websites";
    private static final String DEFAULT_MESSAGE_1 = "Exceeded quantity limit websites = 10";
    public QuantityLimitExceededWebsitesPerUserException(final UserId userId) {
        super(String.format(DEFAULT_MESSAGE, userId));
    }

    public QuantityLimitExceededWebsitesPerUserException(final String message) {
        super(message);
    }

    public QuantityLimitExceededWebsitesPerUserException(){
        super(DEFAULT_MESSAGE_1);
    }

}
