package s26901.pjatalks.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import s26901.pjatalks.Entity.TrendingHashtags;

public interface TrendingHashtagRepository extends MongoRepository<TrendingHashtags, String> {
}
