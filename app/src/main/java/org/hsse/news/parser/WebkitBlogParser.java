package org.hsse.news.parser;

import lombok.extern.slf4j.Slf4j;
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

import static java.util.Collections.reverse;

@Slf4j
public class WebkitBlogParser implements Parser {
    private static final String HOST_NAME = "webkit.org";
    private static final String DEFAULT_BLOG_LINK = "https://" + HOST_NAME + "/blog/category/news/";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) throws ParserFailedException {
        if (HOST_NAME.equals(url.getHost())) {
            try {
                return Optional.of(doParse(
                        url.getFile().isEmpty() ? DEFAULT_BLOG_LINK : url.toExternalForm()));
            } catch (Exception e) {
                log.error("Error while parsing articles on page url {}", url, e);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse(final String url) throws IOException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(url).get();
        final var posts = doc.select("div.tile");

        for (final Element post : posts) {
            final var title = post.select("h1").text();
            final var link = post.select(".tile-link").attr("href");
            final var description = post.select(".summary p").text();
            // final var date = "no_date";
            result.add(new ParsedArticle(
                    title, description, Instant.now(), link, Set.of(), "", url));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
