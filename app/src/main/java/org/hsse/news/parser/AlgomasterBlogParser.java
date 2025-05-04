package org.hsse.news.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.reverse;

@Slf4j
public class AlgomasterBlogParser implements Parser {
    private static final String HOST_NAME = "blog.algomaster.io";
    private static final String DEFAULT_BLOG_LINK = "https://blog.algomaster.io/t/system-design";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (HOST_NAME.equals(url.getHost())) {
            try {
                return Optional.of(doParse(
                        url.getPath().isEmpty() ? DEFAULT_BLOG_LINK : url.toExternalForm()));
            } catch (Exception e) {
                log.error("Error while parsing articles on page url {}", url, e);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse(final String url) throws IOException, ParseException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(url).get();
        final var gridContainer = doc.selectFirst("div[class=portable-archive-list]");
        if (gridContainer == null) {
            throw new RuntimeException("Grid container should exist");
        }

        final var posts = gridContainer.select("div.container-Qnseki");
        for (final Element post : posts) {
            final String title = post.selectFirst("a[data-testid=post-preview-title]").text();
            final String description = post.selectFirst("div:nth-of-type(2) a").text();
            final String link = post.selectFirst("a[data-testid=post-preview-title]").attr("href");
            final Instant date = Instant.parse(post.selectFirst("time").attr("datetime"));
            final String author = post.selectFirst("div.profile-hover-card-target").text();

            result.add(new ParsedArticle(title, description, date, link, author, url));
        }

        reverse(result);
        return result;
    }
}
