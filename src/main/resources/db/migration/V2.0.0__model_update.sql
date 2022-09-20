ALTER TABLE artist
    RENAME COLUMN artist_id TO id;

ALTER TABLE artist
    RENAME COLUMN artist_name TO name;

CREATE TABLE album
(
    id   VARCHAR(255) NOT NULL,
    type VARCHAR(255),
    name VARCHAR(255),
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
    name        VARCHAR(255),
    song_length BIGINT       NOT NULL,
    popularity  INTEGER      NOT NULL,
    album_id    VARCHAR(255),
    CONSTRAINT pk_track PRIMARY KEY (id)
);

ALTER TABLE track
    ADD CONSTRAINT FK_TRACK_ON_ALBUM FOREIGN KEY (album_id) REFERENCES album (id);

DROP TABLE play_artist;

CREATE TABLE track_artists
(
    artists_id VARCHAR(255) NOT NULL,
    track_id   VARCHAR(255) NOT NULL,
    CONSTRAINT pk_track_artists PRIMARY KEY (artists_id, track_id)
);

ALTER TABLE track_artists
    ADD CONSTRAINT fk_traart_on_artist FOREIGN KEY (artists_id) REFERENCES artist (id);

ALTER TABLE track_artists
    ADD CONSTRAINT fk_traart_on_track FOREIGN KEY (track_id) REFERENCES track (id);

CREATE TABLE public.migration
(
    id       VARCHAR(255) NOT NULL,
    complete BOOLEAN      NOT NULL,
    CONSTRAINT pk_migration PRIMARY KEY (id)
);

ALTER TABLE play
    DROP COLUMN track_name;

ALTER TABLE play
    DROP COLUMN song_length;