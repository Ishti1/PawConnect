CREATE TABLE IF NOT EXISTS vet_likes (vet_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (vet_id, user_id));
CREATE TABLE IF NOT EXISTS shop_likes (shop_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (shop_id, user_id));
CREATE TABLE IF NOT EXISTS shelter_likes (shelter_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (shelter_id, user_id));
CREATE TABLE IF NOT EXISTS meme_likes (meme_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (meme_id, user_id));
CREATE TABLE IF NOT EXISTS adoption_likes (adoption_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (adoption_id, user_id));
CREATE TABLE IF NOT EXISTS lostfound_likes (post_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (post_id, user_id));
CREATE TABLE IF NOT EXISTS moment_likes (moment_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (moment_id, user_id));
CREATE TABLE IF NOT EXISTS campaign_likes (campaign_id BIGINT NOT NULL, user_id BIGINT NOT NULL, PRIMARY KEY (campaign_id, user_id));
