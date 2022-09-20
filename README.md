# spotify-history

Records Spotify play history and stores it in an Influx Database and a Postgres Database so that it can be viewed and
explored later using a Grafana dashboard.

To restore DB dump:\
psql -U spotify spotify < /tmp/spotify_dump.sql

ALTER TABLE play ADD CONSTRAINT FK_PLAYENTITY_ON_TRACK FOREIGN KEY (track_id) REFERENCES track (id);