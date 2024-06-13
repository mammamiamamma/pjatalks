package s26901.pjatalks.Constraints;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = ObjectIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ObjectIdValidation {
    String message() default "Value is not ObjectId-compatible";
    Class[] groups() default {};
    Class[] payload() default {};

    String domain() default "pl";
}
