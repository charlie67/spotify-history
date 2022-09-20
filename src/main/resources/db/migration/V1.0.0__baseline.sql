create table artist
(
    artist_id   varchar(255) not null
        primary key,
    artist_name varchar(255)
);

alter table artist
    owner to spotify;

create table play
(
    id          bigint not null
        primary key,
    song_length bigint not null,
    track_id    varchar(255),
    track_name  varchar(255)
);

alter table play
    owner to spotify;

create table play_artist
(
    play_id   bigint       not null
        constraint fkent2id0ksk44x51w3rmq8txjg
            references play,
    artist_id varchar(255) not null
        constraint fk87dhabhnjrr3c29bw6x1j946v
            references artist,
    primary key (play_id, artist_id)
);

alter table play_artist
    owner to spotify;

create table token
(
    id            integer not null
        primary key,
    refresh_token varchar(255)
);

alter table token
    owner to spotify;

