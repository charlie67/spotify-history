package to.charlie.integrationTests.spotifyPlayHistory.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Reads the tables the ingest writes to and compares them against a data table.
 *
 * <p>Every assertion polls rather than reading once: {@code getPlayHistory} hands the Spotify
 * response to a {@code CompletableFuture} callback, so the rows appear on a background thread some
 * time after the step that triggered the job returned. The "should be empty" step additionally
 * insists the table stays empty for a moment, otherwise it would pass simply by reading before the
 * background thread got there.
 */
public class SpotifyDataSteps {

	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final Duration STAYS_EMPTY_FOR = Duration.ofSeconds(2);

	private static final String SELECT_PLAYS = """
					SELECT p.id, p.track_id
					FROM play p
					ORDER BY p.id
					""";

	/**
	 * Collapses a track's artists into a comma separated, alphabetically sorted string so a track and
	 * its artists can be expressed as a single data table row.
	 */
	private static final String SELECT_TRACKS = """
					SELECT t.id,
					       t.name,
					       t.song_length,
					       t.popularity,
					       t.album_id,
					       COALESCE(STRING_AGG(ta.artists_id, ',' ORDER BY ta.artists_id), '') AS artists
					FROM track t
					         LEFT JOIN track_artists ta ON ta.track_id = t.id
					GROUP BY t.id, t.name, t.song_length, t.popularity, t.album_id
					ORDER BY t.id
					""";

	private static final String SELECT_ARTISTS = """
					SELECT a.id, a.name
					FROM artist a
					ORDER BY a.id
					""";

	private static final String SELECT_ALBUMS = """
					SELECT al.id,
					       al.type,
					       al.name,
					       COALESCE(STRING_AGG(aa.artist_id, ',' ORDER BY aa.artist_id), '') AS artists
					FROM album al
					         LEFT JOIN artist_albums aa ON aa.album_id = al.id
					GROUP BY al.id, al.type, al.name
					ORDER BY al.id
					""";

	private static final String SELECT_TOKENS = """
					SELECT t.id, t.refresh_token
					FROM token t
					ORDER BY t.id
					""";

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Given("the following plays exist:")
	public void theFollowingPlaysExist(final DataTable table) {
		for (final Map<String, String> row : table.asMaps()) {
			jdbcTemplate.update("INSERT INTO play (id, track_id) VALUES (?, ?)",
							Long.parseLong(row.get("id")), row.get("track_id"));
		}
	}

	@Then("the play table should contain the following plays:")
	public void thePlayTableShouldContainTheFollowingPlays(final DataTable table) {
		assertTableEventuallyMatches(SELECT_PLAYS, List.of("id", "track_id"), table);
	}

	@Then("the track table should contain the following tracks:")
	public void theTrackTableShouldContainTheFollowingTracks(final DataTable table) {
		assertTableEventuallyMatches(SELECT_TRACKS,
						List.of("id", "name", "song_length", "popularity", "album_id", "artists"), table);
	}

	@Then("the artist table should contain the following artists:")
	public void theArtistTableShouldContainTheFollowingArtists(final DataTable table) {
		assertTableEventuallyMatches(SELECT_ARTISTS, List.of("id", "name"), table);
	}

	@Then("the album table should contain the following albums:")
	public void theAlbumTableShouldContainTheFollowingAlbums(final DataTable table) {
		assertTableEventuallyMatches(SELECT_ALBUMS, List.of("id", "type", "name", "artists"), table);
	}

	@Then("the token table should contain the following tokens:")
	public void theTokenTableShouldContainTheFollowingTokens(final DataTable table) {
		assertTableEventuallyMatches(SELECT_TOKENS, List.of("id", "refresh_token"), table);
	}

	@Then("the {word} table should be empty")
	public void theTableShouldBeEmpty(final String tableName) {
		await().during(STAYS_EMPTY_FOR).atMost(TIMEOUT).untilAsserted(() -> assertEquals(0,
						(int) jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Integer.class),
						"Expected the " + tableName + " table to be empty"));
	}

	private void assertTableEventuallyMatches(final String query, final List<String> columns,
	                                          final DataTable expected) {
		await().atMost(TIMEOUT)
						.untilAsserted(() -> assertEquals(expected.asMaps(), read(query, columns)));
	}

	private List<Map<String, String>> read(final String query, final List<String> columns) {
		return jdbcTemplate.query(query, (rs, rowNum) -> {
			final Map<String, String> row = new LinkedHashMap<>();
			for (final String column : columns) {
				row.put(column, rs.getString(column));
			}

			return row;
		});
	}
}
