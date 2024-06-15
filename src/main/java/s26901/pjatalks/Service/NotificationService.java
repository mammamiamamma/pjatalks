package s26901.pjatalks.Service;

import org.bson.types.ObjectId;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.NotificationDto;
import s26901.pjatalks.DTO.Output.NotificationOutputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.DTO.View.NotificationViewDto;
import s26901.pjatalks.Entity.Notification;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Mapper.NotificationMapper;
import s26901.pjatalks.Mapper.UserMapper;
import s26901.pjatalks.Repository.NotificationRepository;
import s26901.pjatalks.Repository.UserRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final UserMapper userMapper;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationMapper notificationMapper, UserMapper userMapper, NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationMapper = notificationMapper;
        this.userMapper = userMapper;
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(fixedRate = 60*60*1000) // every hour, maybe do every day
    public void updateTrendingHashtags() {
        notificationRepository.deleteOutdatedNotifications();
    }

    public List<Notification> findAllByUser(String user_id){
        ObjectId transId = new ObjectId(user_id);
        if (userRepository.findById(transId).isEmpty())
            throw new IllegalArgumentException("No user with such id found");
        return notificationRepository.getNotificationsForUser(transId);
    }

    public List<NotificationViewDto> getNotificationsForView(String user_id) {
        ObjectId transId = new ObjectId(user_id);
        userRepository.findById(transId).orElseThrow(() ->
                new IllegalArgumentException("No user with such id found"));

        List<Notification> notifications = notificationRepository.getNotificationsForUser(transId);

        List<ObjectId> causerIds = notifications.stream()
                .map(notif -> new ObjectId(notif.getCauser_id()))
                .collect(Collectors.toList());

        List<User> causers = userRepository.findUsers(causerIds);
        Map<String, UserOutputDto> userMap = causers.stream()
                .collect(Collectors.toMap(User::getId, userMapper::map));

        return notifications.stream()
                .map(notif -> {
                    NotificationViewDto notifViewDto = new NotificationViewDto();
                    notifViewDto.setNotification(notificationMapper.map(notif));
                    notifViewDto.setIcon(getIconFromType(notif.getType()));
                    UserOutputDto causerDto = userMap.get(notif.getCauser_id());
                    notifViewDto.setCauser(causerDto != null ? causerDto : generateDeletedUserDetails());
                    return notifViewDto;
                })
                .collect(Collectors.toList());
    }

    //for safety
    private UserOutputDto generateDeletedUserDetails(){
        UserOutputDto userOutputDto = new UserOutputDto();
        userOutputDto.setUsername("DeletedUser");
        return userOutputDto;
    }

    private String getIconFromType(String type){
        switch(type){
            case "SUB" -> {
                return "people.png";
            }
            case "LIKE" -> {
                return "heart.png";
            }
            case "COM" -> {
                return "chat-bubble.png";
            }
            default -> {
                return null;
            }
        }
    }

//    //maybe don't need it
//    public NotificationOutputDto findById(String id){
//        return notificationMapper.map(Objects.requireNonNull(notificationRepository.findById(new ObjectId(id)).orElse(null)));
//    }

    public void addUserNotification(NotificationDto notificationDto){
        if (!notificationRepository.existsByUserIdCauserIdAndType(
                new ObjectId(notificationDto.getUser_id()),
                new ObjectId(notificationDto.getCauser_id()),
                notificationDto.getType()))
            notificationRepository.insertNotification(notificationMapper.map(notificationDto));
    }

//    public void addPostNotification(NotificationDto notificationDto, String post_id){
//        if (!notificationRepository.existsByUserIdPostIdAndType(
//                new ObjectId(notificationDto.getUser_id()),
//                new ObjectId(post_id),
//                notificationDto.getType()))
//            notificationRepository.insertNotification(notificationMapper.map(notificationDto));
//    }

//    public void deletePostNotification(NotificationDto notificationDto, String post_id){
//        if (notificationRepository.existsByUserIdPostIdAndType(
//                new ObjectId(notificationDto.getUser_id()),
//                new ObjectId(post_id),
//                notificationDto.getType())) {
//            Notification notif = notificationRepository.findByUserIdPostIdAndType(
//                    new ObjectId(notificationDto.getUser_id()),
//                    new ObjectId(post_id),
//                    notificationDto.getType());
//            notificationRepository.deleteNotification(new ObjectId(notif.getId()));
//        }
//    }

//    public void deleteUserNotification(NotificationDto notificationDto){
//        if (notificationRepository.existsByUserIdCauserIdAndType(
//                new ObjectId(notificationDto.getUser_id()),
//                new ObjectId(notificationDto.getCauser_id()),
//                notificationDto.getType())) {
//            Notification notif = notificationRepository.findByUserIdCauserIdAndType(
//                    new ObjectId(notificationDto.getUser_id()),
//                    new ObjectId(notificationDto.getCauser_id()),
//                    notificationDto.getType());
//            notificationRepository.deleteNotification(new ObjectId(notif.getId()));
//        }
//    }

    public boolean deleteNotificationsOfUser(String user_id){
        return notificationRepository.deleteNotificationsOfUser(new ObjectId(user_id));
    }

    public boolean deleteNotification(String id){
        return notificationRepository.deleteNotification(new ObjectId(id));
    }
}
