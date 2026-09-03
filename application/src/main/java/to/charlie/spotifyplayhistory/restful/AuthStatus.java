package to.charlie.spotifyplayhistory.restful;

import java.time.Instant;


/**
 * What the UI needs to decide between "sign in" and "sign in again".
 *
 * @param loggedIn                 whether the application currently holds an access token
 * @param reauthorisationRequired  whether Spotify rejected the stored refresh token, which it does
 *                                 six months after the token was issued
 * @param refreshTokenIssuedAt     when the stored refresh token was issued, null when none is
 *                                 stored or its age is unknown
 * @param refreshTokenExpiresAt    when the stored refresh token stops working, null on the same
 *                                 terms
 */
public record AuthStatus(boolean loggedIn,
                         boolean reauthorisationRequired,
                         Instant refreshTokenIssuedAt,
                         Instant refreshTokenExpiresAt)
{
}
