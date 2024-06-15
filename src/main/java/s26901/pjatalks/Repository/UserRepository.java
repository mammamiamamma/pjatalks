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
import java.util.stream.Collectors;

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
        return collection.find(new Document("_id", new Document("$in", idList)))
                .map(this::documentToUser)
                .into(new ArrayList<>());
    }

    public Map<User, Integer> findUsers(Map<ObjectId, Integer> idList) {
        Map<User, Integer> userCountMap = new HashMap<>();

        List<ObjectId> ids = new ArrayList<>(idList.keySet());

        collection.find(new Document("_id", new Document("$in", ids)))
                .forEach((Document doc) -> {
                    User user = documentToUser(doc);
                    userCountMap.put(user, idList.get(new ObjectId(user.getId())));
                });

        return userCountMap;
    }

    public Map<User, Integer> findUsersByPosts(Map<Post, Integer> postList) {
        Map<User, Integer> userCountMap = new HashMap<>();
        List<ObjectId> userIds = postList.keySet().stream()
                .map(post -> new ObjectId(post.getUser_id()))
                .collect(Collectors.toList());

        List<User> users = collection.find(new Document("_id", new Document("$in", userIds)))
                .map(this::documentToUser)
                .into(new ArrayList<>());

        Map<ObjectId, User> userIdToUserMap = users.stream()
                .collect(Collectors.toMap(user -> new ObjectId(user.getId()), user -> user));

        for (Post post : postList.keySet()) {
            User user = userIdToUserMap.get(new ObjectId(post.getUser_id()));
            userCountMap.put(user, postList.get(post));
        }

        return userCountMap;
    }

    public List<User> findUsersByUsernameContaining(String query) {
        Document searchQuery = new Document("username", new Document("$regex", query).append("$options", "i"));
        return collection.find(searchQuery)
                .map(this::documentToUser)
                .into(new ArrayList<>());
    }

    public List<User> findUsersForSuggestionsByUsernameContaining(String query) {
        Document searchQuery = new Document("username", new Document("$regex", query)
                .append("$options", "i"));
        return collection.find(searchQuery).limit(3)
                .map(this::documentToUser)
                .into(new ArrayList<>());
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
        return roles.stream()
                .map(role -> new Document("name", role.getName()))
                .collect(Collectors.toList());
    }
}
