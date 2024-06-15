package s26901.pjatalks.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.SupportEntities.HashtagCount;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public class PostRepository {
    private final MongoCollection<Document> collection;
    private final MongoTemplate mongoTemplate;
//    @Value("${spring.data.mongodb.atlas.search.index}")
//    private String index;

    public PostRepository(MongoTemplate mongoTemplate) {
        this.collection = mongoTemplate.getCollection("posts");
        this.mongoTemplate = mongoTemplate;
    }
    public Page<Post> findAll(PageRequest pageRequest) {
        List<Post> posts = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find()
                .sort(Sorts.descending("timestamp"))
                .skip(0)
                .limit(pageRequest.getPageSize())
                .iterator()) {
            while (cursor.hasNext()) {
                posts.add(documentToPost(cursor.next()));
            }
        }
        return new PageImpl<>(posts, pageRequest, collection.countDocuments());
    }

    public long count(){
        return collection.countDocuments();
    }

    public List<Post> findPostsByTextContaining(String query) {
        Document searchQuery = new Document("text", new Document("$regex", query).append("$options", "i"));
        List<Post> posts = new ArrayList<>();
        collection.find(searchQuery).forEach((Document doc) -> posts.add(documentToPost(doc)));
        return posts;
    }

    public Set<HashtagCount> getTopTrendingHashtags(int limit) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -1);
        Date oneDayAgo = calendar.getTime();

        MatchOperation matchOperation = Aggregation.match(new Criteria("timestamp").gte(oneDayAgo));
        UnwindOperation unwindOperation = Aggregation.unwind("hashtags");
        GroupOperation groupOperation = Aggregation.group("hashtags").count().as("count");
        SortOperation sortOperation = Aggregation.sort(Sort.by(Sort.Direction.DESC, "count"));
        LimitOperation limitOperation = Aggregation.limit(limit);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                unwindOperation,
                groupOperation,
                sortOperation,
                limitOperation
        );

        AggregationResults<HashtagCount> results = mongoTemplate.aggregate(aggregation, "posts", HashtagCount.class);
        System.out.println(results.getMappedResults());
        return new HashSet<>(results.getMappedResults());
    }

    public List<Post> findByUserId(ObjectId id){
        List<Post> posts = new ArrayList<>();
        collection.find(new Document("user_id", id)).forEach((Document doc) -> posts.add(documentToPost(doc)));
        return posts;
    }

    public Page<Post> findByUserIds(List<ObjectId> userIds, PageRequest request) {
        List<Post> resultList = new ArrayList<>();
        long totalDocuments;
        int skip = request.getPageNumber() * request.getPageSize();
        Document query = new Document("user_id", new Document("$in", userIds));
        totalDocuments = collection.countDocuments(query);
        try (MongoCursor<Document> cursor = collection
                .find(query)
                .sort(Sorts.descending("timestamp"))
                .skip(skip)
                .limit(request.getPageSize())
                .iterator()) {
            while (cursor.hasNext()) {
                resultList.add(documentToPost(cursor.next()));
            }
        }
        // Create and return a Page object with the results
        return new PageImpl<>(resultList, request, totalDocuments);
    }

    public Page<Post> findByUserIdPaged(ObjectId id, PageRequest pageRequest) {
        List<Post> posts = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(new Document("user_id", id))
                .sort(Sorts.descending("timestamp"))
                .skip((int) pageRequest.getOffset())
                .limit(pageRequest.getPageSize())
                .iterator()) {
            while (cursor.hasNext()) {
                posts.add(documentToPost(cursor.next()));
            }
        }
        long totalPosts = collection.countDocuments(new Document("user_id", id));
        return new PageImpl<>(posts, pageRequest, totalPosts);
    }

    public String insertPost(Post post){
        ObjectId newId = new ObjectId();
        post.setId(newId.toHexString());

        InsertOneResult result = collection.insertOne(postToDocument(post));
        if (result.wasAcknowledged()){
            return Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue().toHexString();
        } else return null;
    }

    public Optional<Post> findById(ObjectId id) {
        Document doc = collection.find(new Document("_id", id)).first();
        return Optional.ofNullable(doc).map(this::documentToPost);
    }

    public boolean deletePost(ObjectId id){
        DeleteResult result = collection.deleteOne(new Document("_id", id));
        return result.getDeletedCount() != 0;
    }
    public Map<ObjectId, Integer> findTopUsersByPosts() {
        Map<ObjectId, Integer> idCountMap = new HashMap<>();
        collection.aggregate(Arrays.asList(
                Aggregates.group("$user_id", Accumulators.sum("postCount", 1)),
                Aggregates.sort(Sorts.descending("postCount")),
                Aggregates.limit(3)
        )).forEach(doc -> idCountMap.put(doc.getObjectId("_id"), doc.getInteger("postCount")));
        return idCountMap;
    }

    public Map<Post, Integer> findPostsByIds(Map<ObjectId, Integer> idList) {
        Map<Post, Integer> postCountMap = new HashMap<>();
        List<ObjectId> ids = new ArrayList<>(idList.keySet());
        Document query = new Document("_id", new Document("$in", ids));

        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                Post post = documentToPost(doc);
                ObjectId postId = doc.getObjectId("_id");
                Integer count = idList.get(postId);
                postCountMap.put(post, count);
            }
        }

        return postCountMap;
    }
    private Post documentToPost(Document doc) {
        Post post = new Post();
        post.setId(doc.getObjectId("_id").toHexString());
        post.setText(doc.getString("text"));
        post.setTimestamp(doc.getDate("timestamp"));
        post.setUser_id(doc.getObjectId("user_id").toHexString());
        List<String> hashtags = doc.getList("hashtags", String.class);
        post.setHashtags(hashtags != null ? new HashSet<>(hashtags) : new HashSet<>());
        return post;
    }
    private Document postToDocument(Post post) {
        Document doc = new Document();
        doc.put("_id", new ObjectId(post.getId()));
        doc.put("user_id", new ObjectId(post.getUser_id()));
        doc.put("text", post.getText());
        doc.put("timestamp", post.getTimestamp());
        doc.put("hashtags", post.getHashtags());
        return doc;
    }
}
