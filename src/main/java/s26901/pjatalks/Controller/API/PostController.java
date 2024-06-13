package s26901.pjatalks.Controller.API;

import com.mongodb.MongoWriteException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import s26901.pjatalks.Constraints.ObjectIdValidation;
import s26901.pjatalks.DTO.Input.PostInputDto;
import s26901.pjatalks.DTO.Output.PostOutputDto;
import s26901.pjatalks.DTO.View.PostViewDto;
import s26901.pjatalks.Entity.Post;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Service.PostService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("api/posts")
public class PostController {
    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }


//    @GetMapping
//    public ResponseEntity<?> getAllPosts(){ //shouldn't use this method
//        return ResponseEntity.ok(postService.getAllPosts());
//    }

    @GetMapping("/user/{user_id}")
    public ResponseEntity<?> getAllPostsByUser(@PathVariable @ObjectIdValidation String user_id){
        try {
            List<PostOutputDto> postList = postService.getPostsByUserId(user_id);
            return ResponseEntity.ok(postList);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable @ObjectIdValidation String id){ //shouldn't use this method
        try {
            PostOutputDto post = postService.getPostByIdApi(id);
            if (post == null) return ResponseEntity.notFound().header("reason", "wrong id").build();
            return ResponseEntity.ok(post);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> insertPost(@Valid @RequestBody PostInputDto post) {
        try {
            PostViewDto result = postService.insertPost(post);
            if (result == null) return ResponseEntity.status(500).body("Insert not acknowledged");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | MongoWriteException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable @ObjectIdValidation String id) {
        try {
            if (postService.deletePost(id)){
                return ResponseEntity.ok("no errors occured");
            } else {
                return ResponseEntity.badRequest().body("No record with id:"+id+" found");
            }
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NotAcknowledgedException e) {
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

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({ConstraintViolationException.class})
    Map<String, String> handle(ConstraintViolationException ex){
            return ex.getConstraintViolations()
                    .stream()
                    .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(),
                            ConstraintViolation::getMessage));
    }
}
