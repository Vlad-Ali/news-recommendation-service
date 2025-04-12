CREATE TABLE IF NOT EXISTS article_topics
(
    article_id uuid NOT NULL REFERENCES articles (article_id) ON DELETE CASCADE,
    topic_id  bigserial   NOT NULL REFERENCES topics (topic_id) ON DELETE CASCADE,
    PRIMARY KEY (article_id, topic_id)
);

CREATE INDEX IF NOT EXISTS index_article_topics_article_id ON article_topics (article_id);