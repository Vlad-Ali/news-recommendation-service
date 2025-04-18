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
public class ApacheBlogParser implements Parser {
    private static final String HOST_NAME = "news.apache.org";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (HOST_NAME.equals(url.getHost()) && url.getPath().isEmpty()) {
            try {
                return Optional.of(doParse(url.toExternalForm()));
            } catch (Exception e) {
                log.error("Error while parsing articles on page url {}", url, e);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse(final String url) throws IOException, ParseException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(url).get();
        final var posts = doc.select("article");

        for (final Element post : posts) {
            final var linkElement = post.selectFirst("h2.entry-title a");
            final String link = linkElement.attr("href");
            final String title = linkElement.text();
            final String description = post.selectFirst("div.entry-content p").text();
            final Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(
                    post.selectFirst("time.entry-date.published").text());
            final String author = post.selectFirst("span.author").text();

            result.add(new ParsedArticle(
                    title, description, date.toInstant(), link, author, url));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
