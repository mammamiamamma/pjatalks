package s26901.pjatalks.Repository;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;
import s26901.pjatalks.Entity.Notification;

import java.time.Instant;
import java.util.*;

@Repository
public class NotificationRepository {
    private final MongoCollection<Document> collection;

    public NotificationRepository(MongoTemplate mongoTemplate) {
        this.collection = mongoTemplate.getCollection("notifications");
    }

    public void deleteOutdatedNotifications() {
        Instant cutoffTime = Instant.now().minusSeconds(86400); // 86400 seconds in a day
        Date cutoffDate = Date.from(cutoffTime);
        Document query = new Document("timestamp", new Document("$lt", cutoffDate));
        DeleteResult result = collection.deleteMany(query);
        System.out.println("Deleted " + result.getDeletedCount() + " outdated notifications.");
    }

    public Optional<Notification> findById(ObjectId id){
        return Optional.ofNullable(
                collection.find(
                        new Document("_id", id)
                        )
                        .first()
                )
                .map(this::documentToNotification);
    }

    public List<Notification> getNotificationsForUser(ObjectId user_Id){
        List<Notification> notifications = new ArrayList<>();
        collection.find(new Document("user_id", user_Id))
                .sort(Sorts.descending("timestamp"))
                .forEach(
                        document -> {
                            notifications.add(documentToNotification(document));
                        }
                        );
        return notifications;
    }

    public String insertNotification(Notification notification){
        ObjectId newId = new ObjectId();
        notification.setId(newId.toHexString());
        InsertOneResult result = collection.insertOne(notificationToDocument(notification));
        if (result.wasAcknowledged())
            return Objects.requireNonNull(
                    result.getInsertedId())
                    .asObjectId()
                    .getValue()
                    .toHexString();
        else return null;
    }

    public boolean deleteNotificationsOfUser(ObjectId user_id){
        DeleteResult result = collection.deleteMany(new Document("user_id", user_id));
        return result.wasAcknowledged();
    }

    public boolean deleteNotificationsByUser(ObjectId user_id){
        DeleteResult result = collection.deleteMany(new Document("causer_id", user_id));
        return result.wasAcknowledged();
    }

    public boolean deleteNotification(ObjectId id){
        DeleteResult result = collection.deleteOne(new Document("_id", id));
        return result.getDeletedCount() != 0;
    }

    public boolean existsByUserIdAndTimestampAfter(ObjectId userId, Date timestamp) {
        Document query = new Document("user_id", userId)
                .append("timestamp", new Document("$gt", timestamp));

        // Log the query to check
        System.out.println("Query: " + query.toJson());

        long count = collection.countDocuments(query);
        System.out.println("Count: " + count);
        return count > 0;
    }
    public boolean existsByUserIdCauserIdAndType(ObjectId user_id, ObjectId causer_id, String type){
        Document query = new Document("user_id", user_id)
                .append("causer_id", causer_id)
                .append("type", type);
        return collection.countDocuments(query)>0;
    }
    public Notification findByUserIdCauserIdAndType(ObjectId user_id, ObjectId causer_id, String type){
        Document query = new Document("user_id", user_id)
                .append("causer_id", causer_id)
                .append("type", type);
        Document doc = collection.find(query).first();
        if (doc != null){
            return documentToNotification(doc);
        }
        return null;
    }

    private Notification documentToNotification(Document document){
        return new Notification(
                document.getObjectId("_id").toHexString(),
                document.getString("content"),
                document.getDate("timestamp"),
                document.getObjectId("causer_id").toHexString(),
                document.getObjectId("user_id").toHexString(),
                document.getString("type")
        );
    }

    private Document notificationToDocument(Notification notification){
        Document doc = new Document();
        doc.put("_id", new ObjectId(notification.getId()));
        doc.put("content", notification.getContent());
        doc.put("timestamp", notification.getTimestamp());
        doc.put("user_id", new ObjectId(notification.getUser_id()));
        doc.put("causer_id", new ObjectId(notification.getCauser_id()));
        doc.put("type", notification.getType());
        return doc;
    }

}
