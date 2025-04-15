package org.hsse.news.util;

import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.URISyntaxException;

@UtilityClass
public class ParserMainPartUrl {
    public static String extractBaseUrlFromRss(final String rssUrl) throws URISyntaxException {
        try {
            final URI uri = new URI(rssUrl);

            if (uri.getScheme() == null || uri.getHost() == null) {
                return rssUrl;
            }

            final StringBuilder baseUrl = new StringBuilder(uri.getScheme() + "://" + uri.getHost());

            if (uri.getPort() != -1 && uri.getPort() != 80 && uri.getPort() != 443) {
                baseUrl.append(':').append(uri.getPort());
            }

            if (!baseUrl.toString().endsWith("/")) {
                baseUrl.append('/');
            }

            return baseUrl.toString();
        } catch (URISyntaxException e) {
            return rssUrl;
        }
    }
}
