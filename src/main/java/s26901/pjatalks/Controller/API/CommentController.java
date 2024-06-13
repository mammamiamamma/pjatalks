package s26901.pjatalks.Controller.API;

import com.mongodb.MongoWriteException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import s26901.pjatalks.DTO.General.CommentDto;
import s26901.pjatalks.DTO.View.CommentViewDto;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Service.CommentService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getCommentsByUser(@PathVariable String user_id) {
        try {
            List<CommentDto> comments = commentService.findAllByUser(user_id);
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/post/{post_id}")
    public ResponseEntity<?> getCommentByPost(@PathVariable String post_id) {
        try {
            List<CommentDto> comments = commentService.findAllByPostApi(post_id);
            return ResponseEntity.ok(comments);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> insertUser(@Valid @RequestBody CommentDto commentDto) {
        try {
            CommentViewDto result = commentService.insertCommentToPost(commentDto);
            if (result == null) return ResponseEntity.status(500).body("Insert not acknowledged");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | MongoWriteException | NotAcknowledgedException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/post/{post_id}/user/{user_id}")
    public ResponseEntity<?> deleteCommentFromPost(@PathVariable String post_id, @PathVariable String user_id) {
        try {
            if (commentService.deleteCommentFromPost(post_id, user_id)){
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("No record with id found");
            }
        } catch (IllegalArgumentException e ){
            return ResponseEntity.badRequest().body(e.getMessage());
        } //implement NotAcknowledged
    }

    @DeleteMapping("/user/{user_id}")
    public ResponseEntity<?> deleteCommentsByUser(@PathVariable String user_id) {
        try {
            if (commentService.deleteCommentsByUser(user_id)){
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("No record with id:"+user_id+" found");
            }
        } catch (IllegalArgumentException e ){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/post/{post_id}")
    public ResponseEntity<?> deleteCommentsOfPost(@PathVariable String post_id) {
        try {
            if (commentService.deleteCommentsOfPost(post_id)){
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.badRequest().body("No record with id:"+post_id+" found");
            }
        } catch (IllegalArgumentException e ){
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
