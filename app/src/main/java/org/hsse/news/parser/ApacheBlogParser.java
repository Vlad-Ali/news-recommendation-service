package org.hsse.news.parser;

import org.hsse.news.database.article.models.Article;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.reverse;

@Slf4j
public class ApacheBlogParser implements Parser {
    private static final String HOST_NAME = "news.apache.org";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (url.getHost().equals(HOST_NAME) && url.getPath().isEmpty()) {
            try {
                return Optional.of(doParse(url.toExternalForm()));
            } catch (Exception e) {
                log.error("Error while parsing articles on page url {}", url, e);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse(final String url) throws IOException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(url).get();
        final var posts = doc.select("article");

        for (final Element post : posts) {
            final var linkElement = post.selectFirst("h2.entry-title a");
            final var link = linkElement.attr("href");
            final var title = linkElement.text();
            final var description = post.selectFirst("div.entry-content p").text();
            final var date = post.selectFirst("time.entry-date.published").text();
            result.add(new ParsedArticle(
                    title, description, Instant.parse(date), link, Set.of(), "", url));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
