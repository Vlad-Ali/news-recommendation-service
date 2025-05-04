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
public class FlinkBlogParser implements Parser {
    private static final String HOST_NAME = "flink.apache.org";
    private static final String BASE_LINK = "https://" + HOST_NAME;
    private static final String BLOG_LINK = BASE_LINK + "/posts/";
    private static final String PAGE_BLOG_LINK = BASE_LINK + "/posts/page/%s/";
    private static final int MAX_PAGES = 0;

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (HOST_NAME.equals(url.getHost())) {
            try {
                return Optional.of(doParse());
            } catch (Exception e) {
                log.error("Error while parsing articles on website url {}", BLOG_LINK);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse() throws IOException, ParseException {
        final List<ParsedArticle> result = new ArrayList<>(parseArticlesOnPage(BLOG_LINK));
        // other pages
        for (int pageNumber = 2; pageNumber <= MAX_PAGES; pageNumber++) {
            result.addAll(parseArticlesOnPage(PAGE_BLOG_LINK.formatted(pageNumber)));
        }
        // очередность: от старого к свежему
        reverse(result);
        return result;
    }

    private List<ParsedArticle> parseArticlesOnPage(final String pageUrl)
            throws IOException, ParseException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(pageUrl).get();
        final var posts = doc.select("article.markdown.book-post");

        for (final Element post : posts) {
            final var titleElement = post.selectFirst("h3 > a");
            final String title = titleElement.text();
            final String link = BASE_LINK + titleElement.attr("href");
            final Date date = new SimpleDateFormat("MMMM dd, yyyy", Locale.US)
                    .parse(post.ownText().split(" - ")[0]);
            final String author = post.ownText().split(" - ")[1];
            final String description = post.selectFirst("p").text();

            result.add(new ParsedArticle(
                    title, description, date.toInstant(), link, author, BLOG_LINK));
        }

        return result;
    }
}
