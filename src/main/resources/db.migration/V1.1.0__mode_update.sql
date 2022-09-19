ALTER TABLE artist
    RENAME COLUMN artist_id TO id;

ALTER TABLE artist
    RENAME COLUMN artist_name TO name;

CREATE TABLE album
(
    id   VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    name VARCHAR(255),
    json VARCHAR(255),
    CONSTRAINT pk_album PRIMARY KEY (id)
);

CREATE TABLE artist_albums
(
    album_id  VARCHAR(255) NOT NULL,
    artist_id VARCHAR(255) NOT NULL,
    CONSTRAINT pk_artist_albums PRIMARY KEY (album_id, artist_id)
);

ALTER TABLE artist_albums
    ADD CONSTRAINT fk_artalb_on_album FOREIGN KEY (album_id) REFERENCES album (id);

ALTER TABLE artist_albums
    ADD CONSTRAINT fk_artalb_on_artist FOREIGN KEY (artist_id) REFERENCES artist (id);

CREATE TABLE genre
(
    id    UUID NOT NULL,
    genre VARCHAR(255),
    CONSTRAINT pk_genre PRIMARY KEY (id)
);

CREATE TABLE track
(
    id          VARCHAR(255) NOT NULL,
    track_name  VARCHAR(255),
    song_length BIGINT       NOT NULL,
    popularity  INTEGER      NOT NULL,
    album_id    VARCHAR(255),
    json        VARCHAR(255),
    CONSTRAINT pk_track PRIMARY KEY (id)
);

ALTER TABLE track
    ADD CONSTRAINT FK_TRACK_ON_ALBUM_ENTITY FOREIGN KEY (album_id) REFERENCES album (id);

DROP TABLE play_artist;