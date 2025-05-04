package org.hsse.news.database.userrequest.exception;

public class RequestAlreadyExistsException extends RuntimeException {
    public RequestAlreadyExistsException(final String message) {
        super(message);
    }
}
