package to.charlie.integrationTests.spotifyPlayHistory.steps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.SneakyThrows;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.Context;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.DataLoader;
import to.charlie.integrationTests.spotifyPlayHistory.utilities.Ports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class HttpSteps {

	@Autowired
	public DataLoader loader;

	@Autowired
	public Context context;

	@Value("${WIREMOCK_BASE_URL}")
	private String wiremockBaseUrl;

	private final CloseableHttpClient client = HttpClientBuilder
					.create()
					.setDefaultHeaders(getDefaultHeaders())
					.build();

	private List<BasicHeader> getDefaultHeaders() {
		final List<BasicHeader> headers = new ArrayList<>();
		headers.add(new BasicHeader("Content-Type", "application/json"));

		return headers;
	}

	@SneakyThrows
	@When("I send an HTTP {} request to {string}")
	public void sendHttpRequest(final String method, final String url) {
		sendHttpRequest(method, url, null);
	}

	@SneakyThrows
	@When("I send an HTTP {} request to {string} with the body from file: {string}")
	public void sendHttpRequest(final String method, final String url, final String bodyFile) {
		final String content = bodyFile == null ? "" : loader.loadData(bodyFile);

		final String modifiedUrl = replaceUrlVariables(url);

		final String builtUrl = "http://localhost:" + Ports.SPRING + modifiedUrl;

		sendRequest(method, builtUrl, content);
	}

	/**
	 * Replaces {@code {wiremock-url}} with the WireMock base URL and any {@code {KEY}} placeholder
	 * with the matching value stored in the scenario context (e.g. {@code {TAG_ID}}).
	 */
	private String replaceUrlVariables(final String url) {
		String modifiedUrl = url;
		if (url.contains("{wiremock-url}")) {
			modifiedUrl = modifiedUrl.replace("{wiremock-url}", wiremockBaseUrl);
		}
		for (final String key : context.keys()) {
			final String placeholder = "{" + key + "}";
			if (modifiedUrl.contains(placeholder)) {
				modifiedUrl = modifiedUrl.replace(placeholder, context.get(key));
			}
		}
		return modifiedUrl;
	}

	private void sendRequest(final String method, final String url, final String body) {
		if (method.equalsIgnoreCase("POST")) {
			sendGenericHttpRequest(new HttpPost(url), body);
		} else if (method.equalsIgnoreCase("PATCH")) {
			sendGenericHttpRequest(new HttpPatch(url), body);
		} else if (method.equalsIgnoreCase("DELETE")) {
			sendHttpDeleteRequest(url);
		} else if (method.equalsIgnoreCase("GET")) {
			sendHttpGetRequest(url);
		} else {
			throw new UnsupportedOperationException("HTTP method not supported: " + method);
		}
	}

	@SneakyThrows
	private void sendHttpGetRequest(final String url) {
		final HttpGet request = new HttpGet(url);

		final CloseableHttpResponse response = client.execute(request);

		context.set("RESPONSE_STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
		context.set("RESPONSE_BODY", EntityUtils.toString(response.getEntity()));
	}

	@SneakyThrows
	private void sendGenericHttpRequest(final HttpEntityEnclosingRequestBase request, final String body) {
		request.setEntity(new StringEntity(body));
		final CloseableHttpResponse response = client.execute(request);

		context.set("RESPONSE_STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
		context.set("RESPONSE_BODY", EntityUtils.toString(response.getEntity()));
	}

	@SneakyThrows
	private void sendHttpDeleteRequest(final String url) {
		final HttpDelete request = new HttpDelete(url);

		final CloseableHttpResponse response = client.execute(request);

		context.set("RESPONSE_STATUS", String.valueOf(response.getStatusLine().getStatusCode()));
		context.set("RESPONSE_BODY", "");
	}

	@Then("the response body should contain the following fields:")
	public void theResponseBodyShouldContainTheFollowingFields(final DataTable table) {
		assertJsonFields(context.get("RESPONSE_BODY"), table);
	}

	/**
	 * Asserts that the given JSON document contains every field listed in the data table, where each
	 * row is a JSON path and its expected value. The special value {@code <valid_uuid>} asserts the
	 * field is a parseable UUID rather than checking an exact value.
	 */
	static void assertJsonFields(final String body, final DataTable table) {
		final DocumentContext document = JsonPath.parse(body);

		for (final List<String> row : table.asLists(String.class)) {
			final String jsonPath = row.get(0);
			final String expectedValue = row.get(1);

			final String actualValue = document.read(jsonPath, String.class);
			if (actualValue == null) {
				throw new AssertionError(
								"Expected value for JSON path '" + jsonPath + "' not found in JSON body.");
			}

			if (expectedValue.equals("<valid_uuid>")) {
				try {
					UUID.fromString(actualValue);
				} catch (final IllegalArgumentException e) {
					throw new AssertionError("Expected a valid UUID for JSON path '" + jsonPath
									+ "', but got '" + actualValue + "'.");
				}
			} else if (!actualValue.equals(expectedValue)) {
				throw new AssertionError("Expected value '" + expectedValue + "' for JSON path '" + jsonPath
								+ "', but got '" + actualValue + "'.");
			}
		}
	}

	@Then("the response body should match the file: {string}")
	@SneakyThrows
	public void theResponseBodyShouldMatchTheFile(final String expectedFile) {
		final String body = context.get("RESPONSE_BODY");

		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode actual = mapper.readTree(body);
		final JsonNode expected = mapper.readTree(loader.loadData(expectedFile));

		assertJsonMatches("$", expected, actual);
	}

	private void assertJsonMatches(final String path, final JsonNode expected, final JsonNode actual) {
		if (expected.isTextual() && expected.asText().equals("<valid_uuid>")) {
			try {
				UUID.fromString(actual.asText());
			} catch (final IllegalArgumentException e) {
				throw new AssertionError("Expected a valid UUID at '" + path
								+ "', but got '" + actual.asText() + "'.");
			}
			return;
		}

		if (expected.isObject()) {
			if (!actual.isObject()) {
				throw new AssertionError("Expected an object at '" + path + "', but got: " + actual);
			}
			final ObjectNode expectedObject = (ObjectNode) expected;
			final ObjectNode actualObject = (ObjectNode) actual;
			expectedObject.fieldNames().forEachRemaining(field -> {
				if (!actualObject.has(field)) {
					throw new AssertionError("Expected field '" + field + "' at '" + path + "' was missing.");
				}
				assertJsonMatches(path + "." + field, expectedObject.get(field), actualObject.get(field));
			});
			if (actualObject.size() != expectedObject.size()) {
				throw new AssertionError("Object at '" + path + "' has unexpected fields. Expected "
								+ expectedObject.size() + " fields but got " + actualObject.size() + ".");
			}
			return;
		}

		if (expected.isArray()) {
			if (!actual.isArray()) {
				throw new AssertionError("Expected an array at '" + path + "', but got: " + actual);
			}
			final ArrayNode expectedArray = (ArrayNode) expected;
			final ArrayNode actualArray = (ArrayNode) actual;
			if (expectedArray.size() != actualArray.size()) {
				throw new AssertionError("Expected array of size " + expectedArray.size() + " at '" + path
								+ "', but got size " + actualArray.size() + ".");
			}
			for (int i = 0; i < expectedArray.size(); i++) {
				assertJsonMatches(path + "[" + i + "]", expectedArray.get(i), actualArray.get(i));
			}
			return;
		}

		if (!expected.equals(actual)) {
			throw new AssertionError("Expected '" + expected + "' at '" + path + "', but got '" + actual + "'.");
		}
	}

	@Then("I store the value of {string} from the HTTP response as {string}")
	@SneakyThrows
	public void iStoreTheValueOfFromTheResponseAs(final String jsonPath, final String contextKey) {
		final String responseBody = context.get("RESPONSE_BODY");

		final ObjectMapper mapper = new ObjectMapper();
		final Map<String, Object> json = mapper.readValue(responseBody, new TypeReference<>() {
		});
		final Object value = getValueByPath(json, jsonPath);

		context.set(contextKey, value.toString());
	}

	@SuppressWarnings("unchecked")
	public static Object getValueByPath(final Map<String, Object> obj, final String path) {
		final String[] keys = path.split("\\.");
		Object current = obj;

		for (final String key : keys) {
			if (current instanceof Map) {
				current = ((Map<String, Object>) current).get(key);
			} else {
				return null;
			}
		}
		return current;
	}
}
