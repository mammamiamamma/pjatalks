package s26901.pjatalks.Controller.API;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.General.NotificationDto;
import s26901.pjatalks.DTO.Output.NotificationOutputDto;
import s26901.pjatalks.Entity.Notification;
import s26901.pjatalks.Service.NotificationService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/notifications")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getNotificationsForUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            List<Notification> resultList = notificationService.findAllByUser(user_id);
            if (!resultList.isEmpty()) return ResponseEntity.ok(resultList);
            else return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e){
            return ResponseEntity.notFound().header("reason", "no user with id="+user_id+" found").build();
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<?> getNotification(@PathVariable @ObjectIdValidation String id){
//        try {
//            NotificationOutputDto result = notificationService.findById(id);
//            if (result != null) return ResponseEntity.ok(result);
//            else return ResponseEntity.notFound().build();
//        } catch (Exception e){
//            return ResponseEntity.status(500).body(e.getMessage());
//        }
//    }

    @PostMapping
    public ResponseEntity<?> addUser(@Valid @RequestBody NotificationDto notificationDto){
        try {
//            String resultId = notificationService.addNotification(notificationDto);
//            if (resultId != null) return ResponseEntity.ok(resultId);
//            else return ResponseEntity.notFound().build();
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e){
            return ResponseEntity.notFound().header("reason", "no user with id="+notificationDto.getUser_id()+" found").build();
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{user_id}")
    public ResponseEntity<?> deleteNotificationsOfUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            if (notificationService.deleteNotificationsOfUser(user_id))
                return ResponseEntity.ok().build();
            else return ResponseEntity.status(500).body("Request not acknowledged by Database");
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNotification(@PathVariable @ObjectIdValidation String id){
        try {
            if (notificationService.deleteNotification(id))
                return ResponseEntity.ok().build();
            else return ResponseEntity.status(500).body("No record with id="+id+" found");
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({MethodArgumentNotValidException.class})
    Map<String, String> handle(MethodArgumentNotValidException ex){
        return ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({HandlerMethodValidationException.class})
    Map<String, String> handle(HandlerMethodValidationException ex){
        Map<String, String> result = new HashMap<>();
        ex.getAllValidationResults().forEach(entry ->{
            List<String> errors = new ArrayList<>();
            entry.getResolvableErrors().forEach(ent -> {
                errors.add(ent.getDefaultMessage());
            });
            result.put(entry.getMethodParameter().getParameterName(), errors.toString());
        });
        return result;
    }
}
