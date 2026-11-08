package to.charlie.integrationTests.spotifyPlayHistory.steps;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import to.charlie.spotifyplayhistory.domain.service.SpotifyApiService;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Triggers the scheduled jobs by hand. The test properties push their first scheduled run an hour
 * out, so a job only ever runs when a scenario says so.
 */
public class SpotifyJobSteps {

	private static final JobKey REFRESH_TOKEN_JOB = JobKey.jobKey("refreshTokenJob", "spotify");

	@Autowired
	private SpotifyApiService spotifyApiService;

	@Autowired
	private Scheduler scheduler;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@When("the play history job runs")
	public void thePlayHistoryJobRuns() {
		spotifyApiService.getPlayHistory();
	}

	@When("the token refresh job runs")
	public void theTokenRefreshJobRuns() {
		spotifyApiService.refreshAuthCode();
	}

	@When("Quartz fires the token refresh job")
	public void quartzFiresTheTokenRefreshJob() throws SchedulerException {
		scheduler.triggerJob(REFRESH_TOKEN_JOB);
	}

	@Then("the token refresh job should be stored in the Quartz job store")
	public void theTokenRefreshJobShouldBeStored() {
		assertEquals("to.charlie.spotifyplayhistory.job.RefreshTokenJob", jdbcTemplate.queryForObject(
				"SELECT job_class_name FROM qrtz_job_details WHERE job_name = ? AND job_group = ?",
				String.class, REFRESH_TOKEN_JOB.getName(), REFRESH_TOKEN_JOB.getGroup()));
		assertEquals(1, jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM qrtz_simple_triggers WHERE trigger_name = 'refreshTokenTrigger'",
				Integer.class));
	}
}
