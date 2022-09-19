//package to.charlie.spotifyplayhistory.restful;
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.Comparator;
//import java.util.LinkedHashMap;
//import java.util.Map;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import to.charlie.spotifyplayhistory.domain.entity.PlayEntity;
//import to.charlie.spotifyplayhistory.domain.repository.PlayRepository;
//import to.charlie.spotifyplayhistory.domain.service.SpotifyApiService;
//
//
//@RestController
//@RequiredArgsConstructor
//@Slf4j
//@RequestMapping(path = "/playlist", produces = "application/json")
//public class PlayListCreationController
//{
//  private final PlayRepository playRepository;
//
//  private final SpotifyApiService spotifyApiService;
//
//  @PostMapping("/create/{datesmall}/{datelarge}/{amount}")
//  public ResponseEntity<String> createPlaylist(@PathVariable(value = "datesmall") String dateSmall,
//                                               @PathVariable(value = "datelarge") String dateLarge,
//                                               @PathVariable(value = "amount") int amount,
//                                               @RequestParam(value = "name") String name) throws ParseException
//  {
//    log.info("Received playlist creation request");
//    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
//    long dateSmallMillis = dateFormat.parse(String.format("%s 00:00:00", dateSmall)).getTime();
//    long dateLargeMillis = dateFormat.parse(String.format("%s 23:59:59", dateLarge)).getTime();
//
//    Set<PlayEntity> playEntities = playRepository.findAllBetweenTwoTimes(dateSmallMillis, dateLargeMillis);
//
//    Map<String, Long> countMap = playEntities.stream()
//        .map(PlayEntity::getTrackId)
//        .toList()
//        .stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
//
//    Map<String, Long> top =
//        countMap.entrySet().stream()
//            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
//            .limit(amount)
//            .collect(Collectors.toMap(
//                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
//
//    Set<String> trackIds = top.keySet();
//
//    if (trackIds.isEmpty())
//    {
//      return ResponseEntity.notFound().build();
//    }
//
//    try
//    {
//      spotifyApiService.createPlaylistWithNameAndTracks(name, trackIds);
//    }
//    catch (Exception e)
//    {
//      log.error("Error during playlist creation ", e);
//    }
//
//    return ResponseEntity.ok("Done");
//  }
//}
