CREATE TABLE IF NOT EXISTS user_requests(
   request_id bigserial PRIMARY KEY,
   user_id UUID REFERENCES users (user_id) ON DELETE CASCADE,
   url text NOT NULL,
   request_time TIMESTAMP NOT NULL,
   CONSTRAINT User_id_Url UNIQUE (user_id, url)
);

CREATE index IF NOT EXISTS index_user_requests_user_id ON user_requests (user_id);

CREATE index IF NOT EXISTS index_user_requests_request_time ON user_requests (request_time DESC);

CREATE index IF NOT EXISTS index_user_requests_url ON user_requests (url);