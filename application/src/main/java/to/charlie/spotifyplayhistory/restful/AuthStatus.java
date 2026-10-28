package to.charlie.spotifyplayhistory.restful;

import java.time.Instant;

public record AuthStatus(boolean loggedIn,
                         boolean reauthorisationRequired,
                         Instant refreshTokenIssuedAt,
                         Instant refreshTokenExpiresAt)
{
}
