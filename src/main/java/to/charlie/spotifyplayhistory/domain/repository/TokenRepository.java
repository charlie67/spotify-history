package to.charlie.spotifyplayhistory.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import to.charlie.spotifyplayhistory.domain.entity.Token;


@RepositoryRestResource
public interface TokenRepository extends CrudRepository<Token, Integer>
{

}
