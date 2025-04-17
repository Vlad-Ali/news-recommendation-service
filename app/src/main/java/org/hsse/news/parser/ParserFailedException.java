package org.hsse.news.parser;

public class ParserFailedException extends RuntimeException {
    public ParserFailedException(final Throwable cause) {
        super(cause);
    }
}
