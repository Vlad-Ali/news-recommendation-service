package org.hsse.news.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.Collections.reverse;

@Slf4j
public class AlgomasterBlogParser {
    private static final String BLOG_LINK = "https://blog.algomaster.io/t/system-design";

    private List<ParsedArticle> parseLastArticles() {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(AlgomasterBlogParser.BLOG_LINK).get();
            final var gridContainer = doc.selectFirst("div[class=portable-archive-list]");
            if (gridContainer == null) {
                throw new RuntimeException("Grid container should exist");
            }

            final var posts = gridContainer.select("div._container_6i6j0_1");
            for (final Element post : posts) {
                final var title = post.selectFirst("a[data-testid=post-preview-title]").text();
                final var description = post.selectFirst("div:nth-of-type(2) a").text();
                final var link = post.selectFirst("a[data-testid=post-preview-title]").attr("href");
                final var date = post.selectFirst("time").attr("datetime");

                result.add(new ParsedArticle(
                        title, description, Instant.parse(date), link, Set.of(), "", BLOG_LINK));
            }
        } catch (Exception e) {
            log.error("Error while parsing articles on page url {}", AlgomasterBlogParser.BLOG_LINK, e);
            throw new ParserFailedException(e);
        }
        reverse(result);
        return result;
    }
}
