package to.charlie.spotifyplayhistory.job;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

import lombok.RequiredArgsConstructor;
import to.charlie.spotifyplayhistory.domain.service.SpotifyApiService;


@DisallowConcurrentExecution
@RequiredArgsConstructor
public class RefreshTokenJob extends QuartzJobBean
{
  private final SpotifyApiService spotifyApiService;

  @Override
  protected void executeInternal(JobExecutionContext context)
  {
    spotifyApiService.refreshAuthCode();
  }
}
