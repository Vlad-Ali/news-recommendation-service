package org.hsse.news.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

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

    private List<ParsedArticle> doParse() throws IOException, ParseException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(BLOG_LINK).get();
        final var gridContainer = doc.selectFirst("div[class^=Grid-module--grid]");
        if (gridContainer == null) {
            throw new RuntimeException("Grid div is null");
        }

        final var posts = gridContainer.select("div[class^=Post-module--post]");
        for (final Element post : posts) {
            final String title = post.select("h1[class^=Post-module--title]").text();
            final String link = BASE_LINK + post.select("a").attr("href");
            final String description = post.select("p[class^=Post-module--description]").text();
            final Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                    .parse(post.select("span[class^=Post-module--publishDate]").text());

            result.add(new ParsedArticle(
                    title, description, date.toInstant(), link, "Fingerprint", BLOG_LINK));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
