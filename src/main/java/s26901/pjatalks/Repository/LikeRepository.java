package s26901.pjatalks.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import s26901.pjatalks.Entity.Like;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@Repository
public class LikeRepository {
    private final MongoCollection<Document> collection;
    public LikeRepository(MongoTemplate mongoTemplate) {
        this.collection = mongoTemplate.getCollection("likes");
    }

    public long countLikesByPostId(ObjectId post_id) {
        return collection.countDocuments(new Document("post_id", post_id));
    }

//    public Map<ObjectId, Integer> findTop3LikingUsers() {
//        Map<ObjectId, Integer> idCountMap = new HashMap<>();
//        collection.aggregate(Arrays.asList(
//                Aggregates.group("$user_id", Accumulators.sum("likeCount", 1)),
//                Aggregates.sort(Sorts.descending("likeCount")),
//                Aggregates.limit(3)
//        )).forEach(doc -> {
//            idCountMap.put(doc.getObjectId("_id"), doc.getInteger("likeCount"));
//        });
//        return idCountMap;
//    }

    public Map<ObjectId, Integer> findTop3Posts() {
        Map<ObjectId, Integer> postIdLikeCountMap = new HashMap<>();
        collection.aggregate(Arrays.asList(
                Aggregates.group("$post_id", Accumulators.sum("likeCount", 1)),
                Aggregates.sort(Sorts.descending("likeCount")),
                Aggregates.limit(3)
        )).forEach(doc -> postIdLikeCountMap.put(doc.getObjectId("_id"), doc.getInteger("likeCount")));
        return postIdLikeCountMap;
    }

//    public List<Like> getLikesByUser(ObjectId user_id){
//        List<Like> likes = new ArrayList<>();
//        collection.find(new Document("user_id", user_id)).forEach((Document doc) -> {
//            likes.add(documentToLike(doc));
//        });
//        return likes;
//    }

    public boolean isAlreadyLiked(ObjectId userId, ObjectId postId) {
        Document query = new Document("user_id", userId).append("post_id", postId);
        return collection.countDocuments(query) > 0;
    }
    public void insertLikeToPost(Like like){
        ObjectId newId = new ObjectId();
        like.setId(newId.toHexString());
        collection.insertOne(likeToDocument(like));
//        return result.wasAcknowledged();
    }

    public void deleteLikeFromPostByUser(ObjectId userId, ObjectId postId) {
        Document query = new Document("user_id", userId)
                .append("post_id", postId);
        collection.deleteOne(query);
//        return result.getDeletedCount() != 0;
    }

    public boolean deleteAllLikesByUser(ObjectId user_id){
        DeleteResult result = collection.deleteMany(new Document("user_id", user_id));
        return result.wasAcknowledged();
    }

    public boolean deleteAllLikesByPost(ObjectId post_id){
        DeleteResult result = collection.deleteMany(new Document("post_id", post_id));
        return result.wasAcknowledged();
    }

    public boolean existsByPostIdAndUserId(ObjectId postId, ObjectId userId) {
        Document query = new Document("user_id", userId).append("post_id", postId);
        return collection.countDocuments(query) > 0;
    }

//    private Like documentToLike(Document doc) {
//        Like like = new Like();
//        like.setId(doc.getObjectId("_id").toHexString());
//        like.setUser_id(doc.getObjectId("user_id").toHexString());
//        like.setPost_id(doc.getObjectId("post_id").toHexString());
//        like.setTimestamp(doc.getDate("timestamp"));
//        return like;
//    }

    private Document likeToDocument(Like like) {
        Document doc = new Document();
        doc.put("_id", new ObjectId(like.getId()));
        doc.put("user_id", new ObjectId(like.getUser_id()));
        doc.put("post_id", new ObjectId(like.getPost_id()));
        doc.put("timestamp", like.getTimestamp());
        return doc;
    }
}
