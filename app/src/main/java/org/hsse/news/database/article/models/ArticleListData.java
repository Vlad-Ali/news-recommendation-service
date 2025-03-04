package org.hsse.news.database.article.models;

import java.util.List;

public record ArticleListData(
    List<ArticleData> articleDataList
) {}
