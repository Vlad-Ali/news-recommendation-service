package org.hsse.news.parser;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.reverse;

@Slf4j
public class KafkaBlogParser implements Parser {
    private static final String HOST_NAME = "kafka.apache.org";
    private static final String BLOG_LINK = "https://" + HOST_NAME + "/blog";

    @Override
    public Optional<List<ParsedArticle>> parse(final URL url) {
        if (HOST_NAME.equals(url.getHost())) {
            try {
                return Optional.of(doParse());
            } catch (Exception e) {
                log.error("Error while parsing articles on page url {}", BLOG_LINK, e);
            }
        }
        return Optional.empty();
    }

    private List<ParsedArticle> doParse() throws IOException {
        final List<ParsedArticle> result = new ArrayList<>();

        final Document doc = Jsoup.connect(BLOG_LINK).get();
        final var posts = doc.select("article");

        for (final Element post : posts) {
            final var linkElement = post.selectFirst("h2.bullet a[href]");
            final var link = BLOG_LINK + linkElement.attr("href");
            final var title = linkElement.text();
//            final var dateAndAuthor = post.select("h2.bullet").first().nextSibling().toString().trim();

//            final var date = dateAndAuthor.split(" - ")[0];
            final var paragraphs = post.select("p");
            final var description = paragraphs.get(0).text();
            result.add(new ParsedArticle(
                    title, description, Instant.now(), link, Set.of(), "", BLOG_LINK));
        }

        // очередность: от старого к свежему
        reverse(result);
        return result;
    }
}
