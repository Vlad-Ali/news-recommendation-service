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
public class KafkaBlogParser  {
    private static final String BLOG_LINK = "https://kafka.apache.org/blog";

    public List<ParsedArticle> parseLastArticles() {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(BLOG_LINK).get();
            final var posts = doc.select("article");
            for (Element post : posts) {
                final var linkElement = post.selectFirst("h2.bullet a[href]");
                final var link = BLOG_LINK + linkElement.attr("href");
                final var title = linkElement.text();
                final var dateAndAuthor = post.select("h2.bullet").first().nextSibling().toString().trim();;
                final var date = dateAndAuthor.split(" - ")[0];
                final var paragraphs = post.select("p");
                final var description = paragraphs.get(0).text();
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
