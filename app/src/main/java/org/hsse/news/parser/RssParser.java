package org.hsse.news.parser;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.Normalizer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public final class RssParser implements Parser {
  @Override
  public Optional<List<ParsedArticle>> parse(final URL url) {
    try {
      return Optional.of(parse(url, url.toExternalForm()));
    } catch (Exception e) {
      log.error("Error while parsing feed url {}", url);
      return Optional.empty();
    }
  }

  public static List<ParsedArticle> parse(final URL url, final String websiteUrl)
          throws IOException, FeedException {
    final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    setConnection(connection);
    try (XmlReader reader = new XmlReader(connection.getInputStream(), true, UTF_8.name())) {
      return parse(reader, websiteUrl);
    }
  }

  public static List<ParsedArticle> parse(final File file, final String websiteUrl)
          throws IOException, FeedException {
    try (XmlReader reader = new XmlReader(file)) {
      return parse(reader, websiteUrl);
    }
  }

  private static List<ParsedArticle> parse(final XmlReader reader, final String websiteUrl)
          throws FeedException {
    final List<ParsedArticle> articles = new ArrayList<>();
    final SyndFeedInput syndFeedInput = new SyndFeedInput();
    syndFeedInput.setAllowDoctypes(true);
    final SyndFeed feed = syndFeedInput.build(reader);
    for (final SyndEntry entry : feed.getEntries()) {
      final String name = entry.getTitle();
      final String description = getDescription(entry);
      final Instant date = toInstantDate(entry.getPublishedDate());
      final String link = entry.getLink();
      final String author = entry.getAuthor();
      articles.add(
              new ParsedArticle(
                      name,
                      description,
                      date,
                      link,
                      author,
                      websiteUrl));
    }
    return articles;
  }


  private static void setConnection(final HttpURLConnection connection) {
    connection.setRequestProperty("User-agent", "Mozilla/5.0");
  }

  private static Set<String> getTopics(final List<SyndCategory> categoryList) {
    final Set<String> categories = new HashSet<>();
    for (final SyndCategory category : categoryList) {
      final String categoryName = category.getName();
      final Document document = parseText(categoryName);

      categories.add(parseDocument(document));
    }
    return categories;
  }

  private static Instant toInstantDate(final Date date) {
    return date.toInstant();
  }

  private static String getDescription(final SyndEntry entry) {
    final SyndContent content = entry.getDescription();
    return parseDescription(content);
  }

  private static Document parseText(final String text) {
    return Jsoup.parse(text);
  }

  private static String parseDocument(final Document document) {
    return document.text();
  }

  private static String parseDescription(final SyndContent content) {
    if (content == null || content.getValue() == null || content.getValue().isEmpty()) {
      return "";
    }

    String text = Normalizer.normalize(content.getValue(), Normalizer.Form.NFKC)
            .replaceAll("\\p{C}", "")
            .replaceAll("\\s+", " ")
            .trim();

    final char[] problematicChars = {
            '\uFFFD',
            '\u2018', '\u2019',
            '\u201C', '\u201D',
            '\u00A0',
            '\u2013', '\u2014'
    };

    final char[] replacements = {
            '\'',
            '\'', '\'',
            '"', '"',
            ' ',
            '-', '-'
    };

    for (int i = 0; i < problematicChars.length; i++) {
      text = text.replace(problematicChars[i], replacements[i]);
    }

    text = Jsoup.parse(text).text()
            .replaceAll("\\n{3,}", "\n\n")
            .replaceAll("[\\p{Cf}\\p{Co}]", "");


    text = text.codePoints()
            .filter(cp ->
                    cp == '\n' ||
                            cp == '\t' ||
                            Character.isLetterOrDigit(cp) ||
                            Character.isWhitespace(cp) ||
                            ",.!?;:\"'()[]{}<>-–—/=+*%$#@".indexOf(cp) >= 0)
            .collect(StringBuilder::new,
                    StringBuilder::appendCodePoint,
                    StringBuilder::append)
            .toString();

    text = text.trim();
    final int maxLength = 300;
    if (text.length() > maxLength) {
      text = text.substring(0, maxLength);
      final int lastPunctuation = Math.max(
              text.lastIndexOf('.'),
              Math.max(
                      text.lastIndexOf('!'),
                      text.lastIndexOf('?')
              )
      );

      if (lastPunctuation > maxLength - 50 && lastPunctuation > 0) {
        text = text.substring(0, lastPunctuation + 1);
      } else {
        final int lastSpace = text.lastIndexOf(' ');
        if (lastSpace > maxLength - 30 && lastSpace > 0) {
          text = text.substring(0, lastSpace) + "...";
        }
      }
    }

    return text;
  }
}
