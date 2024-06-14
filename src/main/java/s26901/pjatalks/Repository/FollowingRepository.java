package s26901.pjatalks.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import s26901.pjatalks.Entity.Following;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Entity.UserRole;

import java.util.*;

@Repository
public class FollowingRepository {
    private final MongoCollection<Document> collection;

    public FollowingRepository(MongoTemplate mongoTemplate) {
        this.collection = mongoTemplate.getCollection("following");
    }

    public boolean doesUserFollowUser(ObjectId user_id, ObjectId target_id){
        return collection.countDocuments(new Document("F_follower_id", user_id).append("F_user_id", target_id))>0;
    }


    public List<String> getListOfFollowersIds(ObjectId user_id){
        List<String> followerList = new ArrayList<>();
        collection.find(new Document("F_user_id", user_id))
                .forEach(doc -> {
                    followerList.add(doc.getObjectId("F_follower_id").toHexString());
                });
        return followerList;
    }

    public List<String> getListOfFollowingIds(ObjectId user_id){
        List<String> followerList = new ArrayList<>();
        collection.find(new Document("F_follower_id", user_id))
                .forEach(doc -> {
                    followerList.add(doc.getObjectId("F_user_id").toHexString());
                });
        return followerList;
    }

    //true if record like this exists
    public boolean isAlreadyFollowing(Following following){
        return collection.find(
                new Document("F_user_id", following.getF_user_id())
                        .append("F_follower_id", following.getF_follower_id())
                ).first() != null;
    }

    public long getFollowerCountForUser(ObjectId user_id){
        return collection.countDocuments(new Document("F_user_id", user_id));
    }

    public long getFollowingCountForUser(ObjectId user_id){
        return collection.countDocuments(new Document("F_follower_id", user_id));
    }

    public String addFollower(Following following){
        following.setId(new ObjectId().toHexString());
        InsertOneResult result = collection.insertOne(followingToDocument(following));
        if (result.wasAcknowledged()){
            return Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue().toHexString();
        } else return null;
    }

    public boolean addFollower(String user_id, String target_id){
        Following following = new Following(user_id, target_id);
        InsertOneResult result = collection.insertOne(followingToDocument(following));
        return result.wasAcknowledged();
    }

    //can be used to delete both the follower and the 'followee' (person who is followed)
    public boolean deleteFollowerFromUser(ObjectId follower_id, ObjectId user_id){
        DeleteResult result = collection.deleteOne(new Document("F_user_id", user_id).append("F_follower_id", follower_id));
        return result.getDeletedCount() != 0;
    }

    public boolean deleteAllFollowersForUser(ObjectId user_id){
        DeleteResult result = collection.deleteMany(new Document("F_user_id", user_id));
        return result.wasAcknowledged();
    }

    public boolean deleteAllFollowingForUser(ObjectId user_id){
        DeleteResult result = collection.deleteMany(new Document("F_follower_id", user_id));
        return result.wasAcknowledged();
    }

    private User documentToUser(Document doc) {
        User user = new User();
        user.setId(doc.getObjectId("_id").toHexString());
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setEmailAddress(doc.getString("emailAddress"));
        user.setShortBio(doc.getString("shortBio"));
        user.setRoles(documentToRoles(doc.getList("roles", Document.class)));
        return user;
    }
    private Set<UserRole> documentToRoles(List<Document> docs) {
        if (docs == null) {
            return new HashSet<>();
        }
        Set<UserRole> roles = new HashSet<>();
        for (Document doc : docs) {
            roles.add(new UserRole(doc.getString("name")));
        }
        return roles;
    }


    private Document followingToDocument(Following following) {
        Document doc = new Document();
        doc.put("_id", new ObjectId(following.getId()));
        doc.put("F_follower_id", new ObjectId(following.getF_follower_id()));
        doc.put("F_user_id", new ObjectId(following.getF_user_id()));
        return doc;
    }
}
