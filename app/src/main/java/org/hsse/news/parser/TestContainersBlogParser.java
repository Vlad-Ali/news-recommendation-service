package org.hsse.news.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.reverse;

@Slf4j
public class TestContainersBlogParser {
    private static final String BASE_LINK = "https://www.atomicjar.com";
    private static final String BLOG_LINK = BASE_LINK + "/category/testcontainers/";

    public List<ParsedArticle> parseLastArticles() {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(BLOG_LINK).get();
            final var posts = doc.select("article.masonry-blog-item");
            for (final Element post : posts) {
                final var linkElement = post.selectFirst("a.entire-meta-link");
                final var link = BASE_LINK + linkElement.attr("href");
                final var title = linkElement.attr("aria-label");
                final var description = post.selectFirst("div.excerpt").text();
                final var date = post.selectFirst("div.grav-wrap span").text();
                result.add(new ParsedArticle(
                        title, description, Instant.parse(date), link, Set.of(), "", BLOG_LINK));
            }
        } catch (IOException e) {
            log.error("Error while parsing articles on page url {}", BLOG_LINK, e);
            throw new ParserFailedException(e);
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }

}
