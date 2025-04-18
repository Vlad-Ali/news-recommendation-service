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
public class FingerprintBlogParser implements Parser {
    private static final String HOST_NAME = "fingerprint.com";
    private static final String BASE_LINK = "https://" + HOST_NAME;
    private static final String BLOG_LINK = BASE_LINK + "/blog";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (HOST_NAME.equals(url.getHost())) {
            try {
                return Optional.of(doParse());
            } catch (Exception e) {
                log.error("Error while parsing articles on page url {}", BLOG_LINK, e);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse() throws IOException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(BLOG_LINK).get();
        final var gridContainer = doc.selectFirst("div[class^=Grid-module--grid]");
        if (gridContainer == null) {
            throw new RuntimeException("Grid div is null");
        }

        final var posts = gridContainer.select("div[class^=Post-module--post]");
        for (final Element post : posts) {
            final var title = post.select("h1[class^=Post-module--title]").text();
            final var link = BASE_LINK + post.select("a").attr("href");
            final var description = post.select("p[class^=Post-module--description]").text();
            // final var date = post.select("span[class^=Post-module--publishDate]").text();
            result.add(new ParsedArticle(
                    title, description, Instant.now(), link, Set.of(), "", BLOG_LINK));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
