package com.mbc.mobileapp.utils.validator;

import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class ValidatorUtils {
    private static javax.validation.Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    public static <T> String validateBean(T obj) {
        Set<ConstraintViolation<T>> errors = VALIDATOR.validate(obj);
        if (errors != null && errors.size() != 0) {
            List<String> allErrors = new ArrayList(errors.size());
            Iterator var3 = errors.iterator();

            while (var3.hasNext()) {
                ConstraintViolation<T> err = (ConstraintViolation) var3.next();
                allErrors.add(err.getMessage());
            }

            return StringUtils.join(allErrors);
        } else {
            return null;
        }
    }
}
