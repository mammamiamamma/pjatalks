package s26901.pjatalks.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import s26901.pjatalks.Entity.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Repository
public class CommentRepository {
    private final MongoCollection<Document> collection;

    public CommentRepository(MongoTemplate mongoTemplate) {
        this.collection = mongoTemplate.getCollection("comments");
    }

    public long count(){
        return collection.countDocuments();
    }
    public List<Comment> findAllByPost(ObjectId post_id) {
        List<Comment> comments = new ArrayList<>();
        collection.find(new Document("post_id", post_id)).forEach((Document doc) -> {
            comments.add(documentToComment(doc));
        });
        return comments;
    }

    public List<Comment> findAllByUser(ObjectId user_id) {
        List<Comment> comments = new ArrayList<>();
        collection.find(new Document("user_id", user_id)).forEach((Document doc) -> {
            comments.add(documentToComment(doc));
        });
        return comments;
    }

    public long countCommentsByPost(ObjectId post_id){
        return collection.countDocuments(new Document("post_id", post_id));
    }
    public boolean deleteCommentFromPost(ObjectId user_id, ObjectId post_id){
        DeleteResult result = collection.deleteOne(new Document("user_id", user_id).append("post_id", post_id));
        return result.wasAcknowledged();
    }

    public boolean deleteCommentsByUser(ObjectId user_id){
        DeleteResult result = collection.deleteMany(new Document("user_id", user_id));
        return result.wasAcknowledged();
    }

    public boolean deleteCommentsOfPost(ObjectId post_id){
        DeleteResult result = collection.deleteMany(new Document("post_id", post_id));
        return result.wasAcknowledged();
    }

    public String insertCommentToPost(Comment comment){
        ObjectId newId = new ObjectId();
        comment.setId(newId.toHexString());
        InsertOneResult result = collection.insertOne(commentToDocument(comment));
        if (result.wasAcknowledged()){
            return Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue().toHexString();
        }
        return null;
    }

    private Comment documentToComment(Document doc) {
        Comment comment = new Comment();
        comment.setId(doc.getObjectId("_id").toHexString());
        comment.setPost_id(doc.getObjectId("post_id").toHexString());
        comment.setUser_id(doc.getObjectId("user_id").toHexString());
        comment.setText(doc.getString("text"));
        comment.setTimestamp(doc.getDate("timestamp"));
        return comment;
    }

    private Document commentToDocument(Comment comment) {
        Document doc = new Document();
        doc.put("_id", new ObjectId(comment.getId()));
        doc.put("post_id", new ObjectId(comment.getPost_id()));
        doc.put("user_id", new ObjectId(comment.getUser_id()));
        doc.put("text", comment.getText());
        doc.put("timestamp", comment.getTimestamp());
        return doc;
    }
}
