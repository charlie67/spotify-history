package to.charlie.integrationTests.spotifyPlayHistory;

import io.cucumber.java.BeforeAll;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import to.charlie.spotifyplayhistory.SpotifyPlayHistoryApplication;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.Ports;

@CucumberContextConfiguration
@SpringBootTest(classes = {SpotifyPlayHistoryApplication.class,
        TestConfig.class}, webEnvironment = WebEnvironment.DEFINED_PORT, properties = {
        "server.port=" + Ports.SPRING,
})
public class CucumberSpringConfiguration {

    @BeforeAll
    public static void beforeAll() {
        PostgresContainer.getInstance();
        WireMockContainer.getInstance();
    }
}
