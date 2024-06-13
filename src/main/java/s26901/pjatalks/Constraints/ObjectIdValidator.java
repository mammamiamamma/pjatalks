package s26901.pjatalks.Constraints;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.bson.types.ObjectId;

public class ObjectIdValidator implements ConstraintValidator<ObjectIdValidation, String> {
    @Override
    public void initialize(ObjectIdValidation constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return ObjectId.isValid(value);
    }
}
