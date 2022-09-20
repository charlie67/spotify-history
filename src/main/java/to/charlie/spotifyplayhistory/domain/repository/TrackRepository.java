package to.charlie.spotifyplayhistory.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import to.charlie.spotifyplayhistory.domain.entity.TrackEntity;


@Repository
public interface TrackRepository extends CrudRepository<TrackEntity, String>
{
}
