package org.hsse.news.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import static java.util.Collections.reverse;

@Slf4j
public class WebkitBlogParser {
    private static final String BLOG_LINK = "https://webkit.org/blog/category/privacy/";

    public List<ParsedArticle> parseLastArticles() throws ParserFailedException {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(BLOG_LINK).get();
            final var posts = doc.select("div.tile");
            for (final Element post : posts) {
                final var title = post.select("h1").text();
                final var link = post.select(".tile-link").attr("href");
                final var description = post.select(".summary p").text();
                final var date = "no_date";
                result.add(new ParsedArticle(
                        title, description, Instant.parse(date), link, Set.of(), "", BLOG_LINK));
            }
        } catch (Exception e) {
            log.error("Error while parsing articles on page url {}", BLOG_LINK, e);
            throw new ParserFailedException(e);
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }

}
