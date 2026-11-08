package to.charlie.spotifyplayhistory.job;

import java.time.Instant;
import java.util.Date;

import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class QuartzConfig
{
  @Bean
  public JobDetail refreshTokenJobDetail()
  {
    return JobBuilder.newJob(RefreshTokenJob.class)
        .withIdentity("refreshTokenJob", "spotify")
        .withDescription("Refreshes the Spotify access token")
        .storeDurably()
        .build();
  }

  @Bean
  public Trigger refreshTokenTrigger(JobDetail refreshTokenJobDetail,
                                     @Value("${history.refreshTokenDelayMs:1000000}") long intervalMs,
                                     @Value("${history.refreshTokenInitialDelayMs:0}") long initialDelayMs)
  {
    return TriggerBuilder.newTrigger()
        .forJob(refreshTokenJobDetail)
        .withIdentity("refreshTokenTrigger", "spotify")
        .startAt(Date.from(Instant.now().plusMillis(initialDelayMs)))
        .withSchedule(SimpleScheduleBuilder.simpleSchedule()
            .withIntervalInMilliseconds(intervalMs)
            .repeatForever()
            .withMisfireHandlingInstructionNowWithExistingCount())
        .build();
  }
}
