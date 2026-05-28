package com.mbc.mobileapp.utils.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(ElementType.TYPE)
@Retention(RUNTIME)
@Repeatable(PresentIfAnotherFieldHasValue.List.class)
@Constraint(validatedBy = PresentIfAnotherFieldHasValueValidator.class)
@Documented
public @interface PresentIfAnotherFieldHasValue {
    String fieldName();

    boolean isRequiredFieldValue() default false;

    String[] requiredValues() default "";

    boolean isDependFieldNotSame() default false;

    boolean isDependFieldHasValue() default false;

    String[] dependFieldName();

    String[] dependFieldValue();

    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target(ElementType.TYPE)
    @Retention(RUNTIME)
    @Documented
    @interface List {
        PresentIfAnotherFieldHasValue[] value();
    }
}
