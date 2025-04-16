package org.hsse.news.parser;

public class ParserFailedException extends RuntimeException {
    public ParserFailedException(Throwable cause) {
        super(cause);
    }
}
