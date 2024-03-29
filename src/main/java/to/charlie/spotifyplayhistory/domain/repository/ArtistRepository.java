package to.charlie.spotifyplayhistory.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import to.charlie.spotifyplayhistory.domain.entity.ArtistEntity;


@Repository
public interface ArtistRepository extends CrudRepository<ArtistEntity, Integer>
{
  Optional<ArtistEntity> findById(String artistId);
}
