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
import s26901.pjatalks.DTO.Input.UserInputDto;
import s26901.pjatalks.DTO.Output.UserOutputDto;
import s26901.pjatalks.Entity.User;
import s26901.pjatalks.Exception.NotAcknowledgedException;
import s26901.pjatalks.Service.UserService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

//    @GetMapping
//    public ResponseEntity<?> getAllUsers() {
//        return ResponseEntity.ok(userService.getAllUsers());
//    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable @ObjectIdValidation String id) {
        try {
            UserOutputDto user = userService.getUserById(id);
            if (user == null) return ResponseEntity.notFound().header("reason", "wrong id").build();
            else return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (ConstraintViolationException e){
            return ResponseEntity.badRequest().body(e.getConstraintViolations()
                    .stream()
                    .collect(Collectors.toMap(violation -> violation.getPropertyPath().toString(),
                            ConstraintViolation::getMessage)));
        }
    }

    @PostMapping
    public ResponseEntity<?> insertUser(@Valid @RequestBody UserInputDto userInputDto) {
        try {
            String result = userService.insertUser(userInputDto);
            if (result == null) return ResponseEntity.status(500).body("Insert not acknowledged");
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException | MongoWriteException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable @ObjectIdValidation String id, @Valid @RequestBody User updatedUser) {
        try {
            if (!userService.updateUser(id, updatedUser)) {
                return ResponseEntity.status(404).body("User not found");
            }
            return ResponseEntity.ok("User updated successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (MongoWriteException e) {
            return ResponseEntity.badRequest().body("The provided email or username is already taken");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable @ObjectIdValidation String id) {
        try {
            if (userService.deleteUser(id)){
                return ResponseEntity.ok("no errors occured");
            } else {
                return ResponseEntity.badRequest().body("No record with id:"+id+" found");
            }
        } catch (IllegalArgumentException e ){
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
}
