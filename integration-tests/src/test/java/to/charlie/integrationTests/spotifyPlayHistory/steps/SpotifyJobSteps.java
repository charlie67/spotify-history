package to.charlie.integrationTests.spotifyPlayHistory.steps;

import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import to.charlie.spotifyplayhistory.domain.service.SpotifyApiService;

/**
 * Triggers the scheduled jobs by hand. The test properties push their first scheduled run an hour
 * out, so a job only ever runs when a scenario says so.
 */
public class SpotifyJobSteps {

	@Autowired
	private SpotifyApiService spotifyApiService;

	@When("the play history job runs")
	public void thePlayHistoryJobRuns() {
		spotifyApiService.getPlayHistory();
	}

	@When("the token refresh job runs")
	public void theTokenRefreshJobRuns() {
		spotifyApiService.refreshAuthCode();
	}
}
