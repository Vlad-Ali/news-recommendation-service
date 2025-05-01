package org.hsse.news.parser;

import java.net.URL;
import java.util.List;
import java.util.Optional;

public interface Parser {
    Optional<List<ParsedArticle>> parse(URL url);
}
