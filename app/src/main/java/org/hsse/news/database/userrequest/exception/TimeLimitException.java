package org.hsse.news.database.userrequest.exception;

public class TimeLimitException extends RuntimeException {
    public TimeLimitException(final String message) {
        super(message);
    }
}
