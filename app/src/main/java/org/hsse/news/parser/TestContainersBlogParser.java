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
@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class TestContainersBlogParser implements Parser {
    private static final String HOST_NAME = "www.atomicjar.com";
    private static final String BASE_LINK = "https://" + HOST_NAME;
    private static final String BLOG_LINK = BASE_LINK + "/category/testcontainers/";

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
        final var posts = doc.select("article.masonry-blog-item");

        for (final Element post : posts) {
            final var linkElement = post.selectFirst("a.entire-meta-link");
            final String link = BASE_LINK + linkElement.attr("href");
            final String title = linkElement.attr("aria-label");
            final String description = post.selectFirst("div.excerpt").text();
            final Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                    .parse(post.selectFirst("div.grav-wrap span").text());
            final String author = post.select("div.grav-wrap a").get(1).text();

            result.add(new ParsedArticle(
                    title, description, date.toInstant(), link, author, BLOG_LINK));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
