package s26901.pjatalks.Controller.API;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.General.LikeDto;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.Exception.AlreadyLikedException;
import s26901.pjatalks.Service.LikeService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/likes")
public class LikeController {
    private final LikeService likeService;

    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getAllLikesByUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            List<PostOutputDto> likedByUser = likeService.getLikesByUser(user_id);
            return ResponseEntity.ok(likedByUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/user/{user_id}")
    public ResponseEntity<?> deleteLikesByUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            if (likeService.deleteAllLikesByUser(user_id)){
                return ResponseEntity.ok("no errors occured");
            }
            return ResponseEntity.status(500).body("Deletion not acknowledged");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/post/{post_id}")
    public ResponseEntity<?> getLikeAmountOnPost(@PathVariable @ObjectIdValidation String post_id){
        try {
            long likeAmount = likeService.getLikeCountForPost(post_id);
            return ResponseEntity.ok(likeAmount+" likes on post with id="+post_id);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/post/{post_id}")
    public ResponseEntity<?> deleteLikesByPost(@PathVariable @ObjectIdValidation String post_id){
        try {
            if (likeService.deleteAllLikesByPost(post_id)){
                return ResponseEntity.ok("no errors occured");
            }
            return ResponseEntity.status(500).body("Deletion not acknowledged");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> addLike(@Valid @RequestBody LikeDto like){
        try {
//            if (returnId == null) return ResponseEntity.status(500).body("Insert not acknowledged");
            return ResponseEntity.ok(likeService.toggleLike(like.getPost_id(), like.getUser_id(), like.getTimestamp()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{post_id}/{user_id}")
    public ResponseEntity<?> deleteLikeFromPostByUser(@PathVariable @ObjectIdValidation String post_id, @PathVariable @ObjectIdValidation String user_id){
        try {
            if (likeService.deleteLikeFromPost(user_id, post_id)){
                return ResponseEntity.ok("no errors occured");
            }
            return ResponseEntity.status(500).body("Deletion not acknowledged");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
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
