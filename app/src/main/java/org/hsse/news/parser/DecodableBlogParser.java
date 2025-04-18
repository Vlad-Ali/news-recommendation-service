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
public class DecodableBlogParser implements Parser {
    private static final String HOST_NAME = "decodable.co";
    private static final String BASE_LINK = "https://www." + HOST_NAME;
    private static final String BLOG_LINK = BASE_LINK + "/blog";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (HOST_NAME.equals(url.getHost()) || url.getHost().equals("www." + HOST_NAME)) {
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
        final var posts = doc.select(".blog-post-related_content");
        for (final Element post : posts) {
            final String link = BASE_LINK + post.parent().attr("href");
            final String title = post.select("h3.heading-style-h5").text();
            final String description = post.select(".margin-bottom.margin-small .text-size-small").text();
            final Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US).parse(
                    post.select(".blog-grid_meta-wrapper div").first().text());
            final String author = post.selectFirst("div.div-block-38").text()
                    .replace("By ", "");

            result.add(new ParsedArticle(
                    title, description, date.toInstant(), link, author, BLOG_LINK));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
