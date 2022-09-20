package to.charlie.spotifyplayhistory.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import to.charlie.spotifyplayhistory.domain.entity.MigrationEntity;


@Repository
public interface MigrationRepository extends CrudRepository<MigrationEntity, String>
{
  boolean existsByIdAndCompleteTrue(String id);
}
