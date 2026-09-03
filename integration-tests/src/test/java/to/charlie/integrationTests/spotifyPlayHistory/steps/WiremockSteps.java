package to.charlie.integrationTests.spotifyPlayHistory.steps;

import com.github.tomakehurst.wiremock.client.WireMock;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import to.charlie.integrationTests.spotifyPlayHistory.WireMockContainer;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.DataLoader;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.awaitility.Awaitility.await;

import java.time.Duration;

@Slf4j
public class WiremockSteps {

	private static final Duration TIMEOUT = Duration.ofSeconds(10);
	private static final Duration NO_CALLS_FOR = Duration.ofSeconds(2);

	@Autowired
	public DataLoader loader;

	private final WireMock wireMockClient;

	public WiremockSteps() {
		this.wireMockClient = WireMockContainer.client();
	}

	@Given("the URL {string} is set to return the JSON from file {string}")
	public void urlIsSetToReturnTheJsonFromFile(final String url, final String bodyFile) {
		final String content = loader.loadData(bodyFile);

		wireMockClient.register(get(urlEqualTo(url))
						.willReturn(aResponse()
										.withStatus(200)
										.withHeader("Content-Type", "application/json")
										.withBody(content)));

		log.info("Stubbed URL: {} with data from {}", url, bodyFile);
	}

	@Given("the URL {string} is set to return status {int}")
	public void urlIsSetToReturnStatus(final String url, final int status) {
		wireMockClient.register(get(urlEqualTo(url))
						.willReturn(aResponse()
										.withStatus(status)
										.withHeader("Content-Type", "application/json")
										.withBody("{\"error\": {\"status\": " + status + ", \"message\": \"stubbed failure\"}}")));

		log.info("Stubbed URL: {} with status {}", url, status);
	}

	@Given("the POST URL {string} is set to return the JSON from file {string}")
	public void postUrlIsSetToReturnTheJsonFromFile(final String url, final String bodyFile) {
		final String content = loader.loadData(bodyFile);

		wireMockClient.register(post(urlEqualTo(url))
						.willReturn(aResponse()
										.withStatus(200)
										.withHeader("Content-Type", "application/json")
										.withBody(content)));

		log.info("Stubbed POST URL: {} with data from {}", url, bodyFile);
	}

	@Given("the POST URL {string} is set to return status {int} with the JSON from file {string}")
	public void postUrlIsSetToReturnStatusWithTheJsonFromFile(final String url, final int status,
					final String bodyFile) {
		final String content = loader.loadData(bodyFile);

		wireMockClient.register(post(urlEqualTo(url))
						.willReturn(aResponse()
										.withStatus(status)
										.withHeader("Content-Type", "application/json")
										.withBody(content)));

		log.info("Stubbed POST URL: {} with status {} and data from {}", url, status, bodyFile);
	}

	@Given("the POST URL {string} is set to return status {int}")
	public void postUrlIsSetToReturnStatus(final String url, final int status) {
		wireMockClient.register(post(urlEqualTo(url))
						.willReturn(aResponse()
										.withStatus(status)
										.withHeader("Content-Type", "application/json")
										.withBody("{\"error\": {\"status\": " + status + ", \"message\": \"stubbed failure\"}}")));

		log.info("Stubbed POST URL: {} with status {}", url, status);
	}

	@Then("the URL {string} should have been called {int} times")
	public void urlShouldHaveBeenCalledTimes(final String url, final int count) {
		awaitCallCount(count, () -> wireMockClient.verifyThat(count, getRequestedFor(urlEqualTo(url))));
	}

	@Then("the POST URL {string} should have been called {int} times")
	public void postUrlShouldHaveBeenCalledTimes(final String url, final int count) {
		awaitCallCount(count, () -> wireMockClient.verifyThat(count, postRequestedFor(urlEqualTo(url))));
	}

	@Then("the POST URL {string} should have been called with the JSON from file {string}")
	public void postUrlShouldHaveBeenCalledWithTheJsonFromFile(final String url, final String bodyFile) {
		final String content = loader.loadData(bodyFile);

		awaitCallCount(1,
						() -> wireMockClient.verifyThat(postRequestedFor(urlEqualTo(url)).withRequestBody(equalToJson(content))));
	}

	/**
	 * The token refresh and the play history ingest both call Spotify from a CompletableFuture
	 * callback, so a request can still be in flight when the assertion runs. Expecting calls waits
	 * for them to arrive; expecting none waits a moment to prove none turn up late.
	 */
	private void awaitCallCount(final int count, final Runnable verification) {
		if (count == 0) {
			await().during(NO_CALLS_FOR).atMost(TIMEOUT).untilAsserted(verification::run);
		} else {
			await().atMost(TIMEOUT).untilAsserted(verification::run);
		}
	}
}
