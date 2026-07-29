package to.charlie.integrationTests.spotifyPlayHistory.utilities;

import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class DataLoader {

  public final Context context;

  @SneakyThrows
  public String loadData(final String fileName) {
    final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
    try (final InputStream is = classloader.getResourceAsStream("data/" + fileName)) {
      if (is == null) {
        throw new IOException("Body file not found: " + fileName);
      }

      final String content = new String(is.readAllBytes());
      return context.replaceContent(content);
    }
  }
}
