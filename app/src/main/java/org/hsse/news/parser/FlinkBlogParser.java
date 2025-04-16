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
public class FlinkBlogParser  {
    private static final String BASE_LINK = "https://flink.apache.org";
    private static final String BLOG_LINK = BASE_LINK + "/posts/";
    private static final String PAGE_BLOG_LINK = BASE_LINK + "/posts/page/%s/";
    private static final int MAX_PAGES = 0;

    public List<ParsedArticle> parseLastArticles() {
        // first page
        final List<ParsedArticle> result = new ArrayList<>(parseArticlesOnPage(BLOG_LINK));
        // other pages
        for (int pageNumber = 2; pageNumber <= MAX_PAGES; pageNumber++) {
            try {
                result.addAll(parseArticlesOnPage(PAGE_BLOG_LINK.formatted(pageNumber)));
            } catch (Exception e) {
                log.error("Error while parsing articles for page {}", pageNumber, e);
            }
        }
        // очередность: от старого к свежему
        reverse(result);
        return result;
    }

    private List<ParsedArticle> parseArticlesOnPage(String pageUrl) {
        final List<ParsedArticle> result = new ArrayList<>();
        try {
            final Document doc = Jsoup.connect(pageUrl).get();
            final var posts = doc.select("article.markdown.book-post");
            for (Element post : posts) {
                final var titleElement = post.selectFirst("h3 > a");
                final var title = titleElement.text();
                final var link = BASE_LINK + titleElement.attr("href");
                final var date = post.ownText().split(" - ")[0];
                final var description = post.selectFirst("p").text();
                result.add(new ParsedArticle(
                        title, description, Instant.parse(date), link, Set.of(), "", BLOG_LINK));
            }
        } catch (Exception e) {
            log.error("Error while parsing articles on page url {}", pageUrl, e);
            throw new ParserFailedException(e);
        }
        return result;
    }
}
