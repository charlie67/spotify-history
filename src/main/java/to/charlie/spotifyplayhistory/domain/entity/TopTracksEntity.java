//package to.charlie.spotifyplayhistory.domain.entity;
//
//import java.sql.Timestamp;
//import java.util.Set;
//import java.util.UUID;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//
//@Entity(name = "top_tracks")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class TopTracksEntity
//{
//  @Id
//  @GeneratedValue(strategy = GenerationType.AUTO)
//  @Column(name = "id", nullable = false)
//  private UUID id;
//
//  @Column(name = "time")
//  Timestamp timestamp;
//
//  Set<TrackEntity> tracks;
//}
