package s26901.pjatalks.Controller.API;

import jakarta.validation.Valid;
import org.bson.types.ObjectId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.General.FollowingDto;
import s26901.pjatalks.Entity.Following;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Exception.RecordExistsException;
import s26901.pjatalks.Service.FollowingService;

import java.util.List;

@RestController
@RequestMapping("/following")
public class FollowingController {
    private final FollowingService followingService;

    public FollowingController(FollowingService followingService) {
        this.followingService = followingService;
    }

    @GetMapping("/followers/{user_id}")
    public ResponseEntity<?> getListOfFollowersForUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            List<String> resultList = followingService.getListOfFollowersForUser(user_id);
            if (!resultList.isEmpty()) return ResponseEntity.ok(resultList);
            else return ResponseEntity.notFound().build();
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/following/{user_id}")
    public ResponseEntity<?> getListOfFollowingForUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            List<String> resultList = followingService.getListOfFollowingForUser(user_id);
            if (!resultList.isEmpty()) return ResponseEntity.ok(resultList);
            else return ResponseEntity.notFound().build();
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> insertFollower(@Valid @RequestBody FollowingDto followingDto){
        try {
            String resultId = followingService.addFollower(followingDto);
            if (resultId != null) return ResponseEntity.ok(resultId);
            else return ResponseEntity.status(500).body("Record could not be saved");
        } catch (IllegalArgumentException | RecordExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e){
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/{user_id}/{follower_id}")
    public ResponseEntity<?> deleteFollowerFromUser(@ObjectIdValidation @PathVariable String user_id,
                                                    @ObjectIdValidation @PathVariable String follower_id){
        try {
            if (followingService.deleteFollowerFromUser(follower_id, user_id))
                return ResponseEntity.ok().build();
            else return ResponseEntity.status(500).build();
        } catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }

    @DeleteMapping("/{user_id}")
    public ResponseEntity<?> deleteAllFollowersForUser(@ObjectIdValidation @PathVariable String user_id){
        try {
            if (followingService.deleteAllFollowersForUser(user_id))
                return ResponseEntity.ok().build();
            else return ResponseEntity.status(500).build();
        } catch (Exception e){
            return ResponseEntity.status(500).build();
        }
    }
}

