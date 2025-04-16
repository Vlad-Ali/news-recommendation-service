package org.hsse.news.parser;

import org.hsse.news.database.article.models.Article;
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
public class VladMihalceaBlogParser {
    private static final String BLOG_LINK = "https://vladmihalcea.com/blog/";

    public List<ParsedArticle> parseLastArticles() {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(BLOG_LINK).get();
            final var posts = doc.select("div.blog-holder");
            for (final Element post : posts) {
                final var linkElement = post.selectFirst("h2.headline a");
                final var link = linkElement.attr("href");
                final var title = linkElement.attr("title");
                final var description = post.selectFirst("div.article > p").text();
                final var date = post.selectFirst("span.post-date-entry").text()
                    .replace("Posted on ", "");
                result.add(new ParsedArticle(
                        title, description, Instant.parse(date), link, Set.of(), "", date));
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
