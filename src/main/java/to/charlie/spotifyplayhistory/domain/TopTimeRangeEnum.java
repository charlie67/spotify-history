package to.charlie.spotifyplayhistory.domain;

import lombok.Getter;


@Getter
public enum TopTimeRangeEnum
{
  SHORT_TERM("short_term"),
  MEDIUM_TERM("medium_term"),
  LONG_TERM("long_term");

  private final String value;

  TopTimeRangeEnum(String value)
  {
    this.value = value;
  }
}
