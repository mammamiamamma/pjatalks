package s26901.pjatalks.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Entity.UserRole;

import java.util.*;

@Repository
public class UserRepository {
    private final MongoCollection<Document> collection;
//    @Value("${spring.data.mongodb.atlas.search.index}")
//    private String index;

    public UserRepository(MongoTemplate mongoTemplate) {
        this.collection = mongoTemplate.getCollection("users");
    }

    public long count() {
        return collection.countDocuments();
    }

    public long countActiveUsers(Date sinceDate) {
        Document filter = new Document("lastCheckedNotifications", new Document("$gte", sinceDate));
        return collection.countDocuments(filter);
    }

    public List<User> findUsers(List<ObjectId> idList) {
        List<User> userList = new ArrayList<>();
        for (ObjectId id : idList){
            User user = collection
                    .find(new Document("_id", id))
                    .map(this::documentToUser)
                    .first();
            userList.add(user);
        }
        return userList;
    }

    public Map<User, Integer> findUsers(Map<ObjectId, Integer> idList) {
        Map<User, Integer> userCountMap = new HashMap<>();
        for (ObjectId id : idList.keySet()){
            User user = collection
                    .find(new Document("_id", id))
                    .map(this::documentToUser)
                    .first();
            userCountMap.put(user, idList.get(id));
        }
        return userCountMap;
    }

    public Map<User, Integer> findUsersByPosts(Map<Post, Integer> postList) {
        Map<User, Integer> userCountMap = new HashMap<>();
        for (Post post : postList.keySet()){
            User user = collection
                    .find(new Document("_id", new ObjectId(post.getUser_id())))
                    .map(this::documentToUser)
                    .first();
            userCountMap.put(user, postList.get(post));
        }
        return userCountMap;
    }


//    public boolean updateUsersLastVisitedTime(ObjectId user_id, Date date){
//        Document doc = collection.find(new Document("_id", user_id)).first();
//        if (doc != null){
//            User user = documentToUser(doc);
//            user.setLastCheckedNotifications(date);
//            return true;
//        }
//        return false;
//    }
    public List<User> findUsersByUsernameContaining(String query) {
        Document searchQuery = new Document("username", new Document("$regex", query).append("$options", "i"));
        List<User> users = new ArrayList<>();
        collection.find(searchQuery).forEach((Document doc) -> users.add(documentToUser(doc)));
        return users;
    }

    public List<User> findUsersForSuggestionsByUsernameContaining(String query) {
        Document searchQuery = new Document("username", new Document("$regex", query)
                .append("$options", "i"));
        List<User> users = new ArrayList<>();
        collection.find(searchQuery).limit(3)
                .forEach((Document doc) -> users.add(documentToUser(doc)));
        return users;
    }

    public Optional<User> findById(ObjectId id) {
        Document doc = collection.find(new Document("_id", id)).first();
        return Optional.ofNullable(doc).map(this::documentToUser);
    }

    public Optional<User> findUserByEmail(String email) {
        Document doc = collection.find(new Document("emailAddress", email)).first();
        return Optional.ofNullable(doc).map(this::documentToUser);
    }

    public Optional<User> findUserByUsername(String username) {
        Document doc = collection.find(new Document("username", username)).first();
        return Optional.ofNullable(doc).map(this::documentToUser);
    }

    public String insertUser(User user) {
        InsertOneResult result = collection.insertOne(userToDocument(user));
        if (result.wasAcknowledged()) {
            return Objects.requireNonNull(result.getInsertedId()).asObjectId().getValue().toHexString();
        }
        //probably change to exception such that if result was not acknowledged we throw
        return null;
    }

    public boolean updateUser(ObjectId id, User user) {
        Document updatedDoc = userToDocument(user);
        UpdateResult result = collection.replaceOne(new Document("_id", id), updatedDoc);
        return result.getMatchedCount() != 0;
    }

    public boolean deleteUser(ObjectId id) {
        DeleteResult result = collection.deleteOne(new Document("_id", id));
        return result.getDeletedCount() != 0;
    }

    private User documentToUser(Document doc) {
        User user = new User();
        user.setId(doc.getObjectId("_id").toHexString());
        user.setUsername(doc.getString("username"));
        user.setPassword(doc.getString("password"));
        user.setEmailAddress(doc.getString("emailAddress"));
        user.setShortBio(doc.getString("shortBio"));
        user.setRoles(documentToRoles(doc.getList("roles", Document.class)));
        user.setLastCheckedNotifications(doc.getDate("lastCheckedNotifications"));
        return user;
    }

    private Document userToDocument(User user) {
        Document doc = new Document();
        doc.put("_id", new ObjectId(user.getId()));
        doc.put("username", user.getUsername());
        doc.put("password", user.getPassword());
        doc.put("emailAddress", user.getEmailAddress());
        doc.put("shortBio", user.getShortBio());
        doc.put("roles", rolesToDocument(user.getRoles()));
        doc.put("lastCheckedNotifications", user.getLastCheckedNotifications());
        return doc;
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

    private List<Document> rolesToDocument(Set<UserRole> roles) {
        List<Document> docs = new ArrayList<>();
        for (UserRole role : roles) {
            Document doc = new Document();
            doc.put("name", role.getName());
            docs.add(doc);
        }
        return docs;
    }
}
