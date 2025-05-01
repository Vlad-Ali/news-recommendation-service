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
public class VladMihalceaBlogParser implements Parser {
    private static final String HOST_NAME = "vladmihalcea.com";
    private static final String BLOG_LINK = "https://" + HOST_NAME + "/blog/";

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
        final var posts = doc.select("div.blog-holder");

        for (final Element post : posts) {
            final var linkElement = post.selectFirst("h2.headline a");
            final String link = linkElement.attr("href");
            final String title = linkElement.attr("title");
            final String description = post.selectFirst("div.article > p").text();
            final Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(
                    post.selectFirst("span.post-date-entry").text()
                            .replace("Posted on ", ""));

            result.add(new ParsedArticle(
                    title, description, date.toInstant(), link, "Vlad Mihalcea", BLOG_LINK));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
