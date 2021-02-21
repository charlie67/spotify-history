package to.charlie.spotifyplayhistory.postgres;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface ArtistRepository extends CrudRepository<Artist, Integer>
{
  Optional<Artist> findByArtistId(String artistId);
}
