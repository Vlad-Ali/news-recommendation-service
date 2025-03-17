CREATE TABLE IF NOT EXISTS user_articles
(
    id BIGSERIAL PRIMARY KEY NOT NULL,
    user_id  uuid NOT NULL REFERENCES users(user_id),
    article_id uuid NOT NULL REFERENCES articles(article_id),
    mark int NOT NULL DEFAULT 0,
    CONSTRAINT unique_factor UNIQUE (user_id, article_id)
);