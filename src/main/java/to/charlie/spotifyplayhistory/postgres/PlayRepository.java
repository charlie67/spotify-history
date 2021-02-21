package to.charlie.spotifyplayhistory.postgres;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;


@RepositoryRestResource
public interface PlayRepository extends CrudRepository<Play, Integer>
{
  Optional<Play> findById(long id);
}
