package to.charlie.integrationTests.spotifyPlayHistory.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextSteps {

	@Autowired
	private Context context;

	@Given("{string} is set to {string}")
	public void setContextValue(final String key, final String value) {
		context.set(key, value);
	}

	@Then("{string} should be {string}")
	public void shouldBe(final String key, final String value) {
		assertEquals(value, context.get(key));
	}
}
