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
public class FingerprintBlogParser {
    private static final String BASE_LINK = "https://fingerprint.com/";
    private static final String BLOG_LINK = BASE_LINK + "/blog";

    public List<ParsedArticle> parseLastArticles() {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(BLOG_LINK).get();
            final var gridContainer = doc.selectFirst("div[class^=Grid-module--grid]");
            if (gridContainer != null) {
                final var posts = gridContainer.select("div[class^=Post-module--post]");
                for (final Element post : posts) {
                    final var title = post.select("h1[class^=Post-module--title]").text();
                    final var link = BASE_LINK + post.select("a").attr("href");
                    final var description = post.select("p[class^=Post-module--description]").text();
                    final var date = post.select("span[class^=Post-module--publishDate]").text();
                    result.add(new ParsedArticle(
                            title, description, Instant.parse(date), link, Set.of(), "", BLOG_LINK));
                }
            } else {
                throw new RuntimeException("Grid div is null");
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
