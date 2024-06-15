package s26901.pjatalks.Service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import s26901.pjatalks.Entity.TrendingHashtags;
import s26901.pjatalks.Repository.PostRepository;
import s26901.pjatalks.Repository.TrendingHashtagRepository;
import s26901.pjatalks.SupportEntities.HashtagCount;

import java.util.Date;
import java.util.Set;

@Service
public class HashtagService {
    private final TrendingHashtagRepository trendingHashtagRepository;
    private final PostRepository postRepository;

    public HashtagService(TrendingHashtagRepository trendingHashtagRepository, PostRepository postRepository) {
        this.trendingHashtagRepository = trendingHashtagRepository;
        this.postRepository = postRepository;
    }

    @Scheduled(fixedRate = 60*60*1000) // every hour
    public void updateTrendingHashtags() {
        Set<HashtagCount> topHashtags = postRepository.getTopTrendingHashtags(3);

        TrendingHashtags trendingHashtags = new TrendingHashtags(topHashtags, new Date());
        trendingHashtags.setId("singleton"); // Ensuring only one record
        trendingHashtagRepository.save(trendingHashtags);
    }

    public TrendingHashtags getTrendingHashtags() {
        return trendingHashtagRepository.findById("singleton").orElse(null);
    }
}
