package s26901.pjatalks.Mapper;

import org.springframework.stereotype.Service;
import s26901.pjatalks.DTO.General.NotificationDto;

import s26901.pjatalks.DTO.Output.NotificationOutputDto;
import s26901.pjatalks.Entity.Notification;

@Service
public class NotificationMapper {
    public Notification map(NotificationDto notificationDto){
        return new Notification(
                notificationDto.getContent(),
                notificationDto.getTimestamp(),
                notificationDto.getUser_id(),
                notificationDto.getCauser_id(),
                notificationDto.getType()
        );
    }

    public NotificationOutputDto map(Notification notification){
        return new NotificationOutputDto(
                notification.getId(), notification.getContent(),
                notification.getTimestamp(),
                notification.getUser_id(),
                notification.getCauser_id(),
                notification.getType()
        );
    }
}
