package org.hsse.news.util;

import lombok.experimental.UtilityClass;
import org.hsse.news.database.userrequest.exception.IncorrectURLException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@UtilityClass
public class UrlUtils {

    public static String normalizeUrl(final String urlString) throws URISyntaxException {
        final URI uri = new URI(urlString);

        final String scheme = uri.getScheme();
        if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https"))) {
            throw new IncorrectURLException(urlString+" URL должен использовать протокол HTTP или HTTPS");
        }

        final String host = uri.getHost();
        if (host == null) {
            throw new IncorrectURLException(urlString+" URL должен содержать хост");
        }

        int port = uri.getPort();
        if (("http".equalsIgnoreCase(scheme) && port == 80) ||
                ("https".equalsIgnoreCase(scheme) && port == 443)) {
                port = -1;
        }

        return new URI(scheme, null, host, port, null, null, null).toString();
    }

    public static boolean isUrlAccessible(final String urlString) {
        try {
            final URL url = new URL(urlString);
            final HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setInstanceFollowRedirects(true);

            connection.setRequestProperty("User-Agent", "UrlUtils/1.0");

            final int responseCode = connection.getResponseCode();
            return (responseCode >= 200 && responseCode < 400);
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean validateAndCheckUrl(final String urlString) {
        try {
            final String normalizedUrl = normalizeUrl(urlString);
            return isUrlAccessible(normalizedUrl);
        } catch (URISyntaxException e) {
            return false;
        }
    }
}

