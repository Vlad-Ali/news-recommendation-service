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
public class DecodableBlogParser {
    private static final String BASE_LINK = "https://www.decodable.co";
    private static final String BLOG_LINK = BASE_LINK + "/blog";

    public List<ParsedArticle> parseLastArticles() {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(BLOG_LINK).get();
            final var posts = doc.select(".blog-post-related_content");
            for (Element post : posts) {
                final var link = BASE_LINK + post.parent().attr("href");
                final var title = post.select("h3.heading-style-h5").text();
                final var description = post.select(".margin-bottom.margin-small .text-size-small").text();
                final var date = post.select(".blog-grid_meta-wrapper div").first().text();
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
