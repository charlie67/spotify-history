package to.charlie.integrationTests.spotifyPlayHistory.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import to.charlie.integrationTests.spotifyPlayHistory.WireMockContainer;
import to.charlie.spotifyplayhistory.domain.service.SpotifyApiService;

public class Hooks {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SpotifyApiService spotifyApiService;

    @Before
    public void resetWireMock() {
        // Both the stubs and the request journal are per-scenario: a leftover stub would let a
        // scenario pass without setting up its own, and leftover requests would break the
        // "should have been called n times" assertions.
        WireMockContainer.client().resetRequests();
        WireMockContainer.client().resetMappings();
    }

    @After
    public void resetDatabase() {
        jdbcTemplate.execute(
                "TRUNCATE TABLE play, track, track_artists, album, artist_albums, artist, token, migration, genre RESTART IDENTITY CASCADE");
    }

    /**
     * The Spring context is shared by the whole run, and SpotifyApiService holds its SpotifyApi -
     * including any access token - in a field. Without this the token an authentication scenario
     * obtains would leak into every scenario that follows and quietly make them "logged in", and a
     * scenario that got its token rejected would leave the reauthorisation flag set behind it.
     */
    @After
    public void resetSpotifyApi() {
        spotifyApiService.forgetTokens();
    }
}
