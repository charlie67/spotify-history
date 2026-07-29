package to.charlie.spotifyplayhistory.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import to.charlie.spotifyplayhistory.domain.entity.Token;


@Repository
public interface TokenRepository extends CrudRepository<Token, Integer>
{

}
