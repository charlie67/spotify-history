package to.charlie.integrationTests.spotifyPlayHistory.utilities;

import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Context {

  private final HashMap<String, String> context = new HashMap<>();

  public String set(final String key, final String value) {
    return context.put(key, value);
  }

  public String get(final String key) {
    return context.get(key);
  }

  public Set<String> keys() {
    return context.keySet();
  }

  /**
   * Replaces {@code ${key:-default}} placeholders in the given content with the value stored in
   * the context under {@code key}, falling back to {@code default} when the key is not set.
   */
  public String replaceContent(String content) {
    final String regex = "\\$\\{([^}]+):-([^}]+)}";

    final Pattern pattern = Pattern.compile(regex);
    final Matcher matcher = pattern.matcher(content);

    while (matcher.find()) {

      final String fullMatch = matcher.group(0);
      final String keyName = matcher.group(1);

      String replaceValue = matcher.group(2);

      if (context.containsKey(keyName)) {
        replaceValue = context.get(keyName);
      }

      content = content.replace(fullMatch, replaceValue);
    }

    return content;
  }
}
