package to.charlie.spotifyplayhistory.domain.repository;

import java.util.Optional;
import java.util.Set;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import to.charlie.spotifyplayhistory.domain.entity.Play;


@Repository
public interface PlayRepository extends CrudRepository<Play, Integer>
{
  Optional<Play> findById(long id);

  @Query(value = "SELECT p FROM Play p WHERE p.id <= :id2 and p.id >= :id1")
  Set<Play> findAllBetweenTwoTimes(long id1,
                                   long id2);
}
