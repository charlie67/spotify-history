//package to.charlie.spotifyplayhistory.domain.entity;
//
//import java.sql.Timestamp;
//import java.util.LinkedHashSet;
//import java.util.Set;
//import java.util.UUID;
//
//import javax.persistence.CollectionTable;
//import javax.persistence.Column;
//import javax.persistence.ElementCollection;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.JoinColumn;
//
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//
//@Entity(name = "top_artists")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class TopArtistsEntity
//{
//  @Id
//  @GeneratedValue(strategy = GenerationType.AUTO)
//  @Column(name = "id", nullable = false)
//  private UUID id;
//
//  @Column(name = "time")
//  Timestamp timestamp;
//
//  @ElementCollection
//  @Column(name = "artist_entity")
//  @CollectionTable(name = "top_artists_artist_entity", joinColumns = @JoinColumn(name = "owner_id"))
//  private Set<ArtistEntity> artists = new LinkedHashSet<>();
//
//}
