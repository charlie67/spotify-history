package to.charlie.spotifyplayhistory.domain.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import to.charlie.spotifyplayhistory.domain.entity.AlbumEntity;


@Repository
public interface AlbumRepository extends CrudRepository<AlbumEntity, Integer>
{
  Optional<AlbumEntity> findById(String id);
}
