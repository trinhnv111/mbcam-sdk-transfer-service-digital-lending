package com.mbc.mobileapp.utils.validator;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.*;

public class PresentIfAnotherFieldHasValueValidator implements ConstraintValidator<PresentIfAnotherFieldHasValue, Object> {

    private String fieldName;
    private String[] requiredValues;
    private boolean isDependFieldNotSame;
    private boolean isDependFieldHasValue;
    private String[] dependFieldNames;
    private String[] dependFieldValues;
    private boolean isRequiredFieldValue;

    @Override
    public void initialize(PresentIfAnotherFieldHasValue constraintAnnotation) {
        fieldName = constraintAnnotation.fieldName();
        requiredValues = constraintAnnotation.requiredValues();
        isDependFieldNotSame = constraintAnnotation.isDependFieldNotSame();
        isDependFieldHasValue = constraintAnnotation.isDependFieldHasValue();
        dependFieldNames = constraintAnnotation.dependFieldName();
        dependFieldValues = constraintAnnotation.dependFieldValue();
        isRequiredFieldValue = constraintAnnotation.isRequiredFieldValue();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (Objects.isNull(value))
            return true;

        try {
            BeanWrapper beanWrapper = new BeanWrapperImpl(value);
            Object propertyValue = beanWrapper.getPropertyValue(fieldName);

            if (isDependFieldHasValue) {
                Object dependFieldValueInput = beanWrapper.getPropertyValue(dependFieldNames[0]);
                return !(Objects.isNull(propertyValue) && Objects.nonNull(dependFieldValueInput));
            }

            String fieldValueInput = (String) propertyValue;

            Map<String, String> DEPEND_FIELD_KV = new HashMap<>();
            if (isDependFieldNotSame) {
                for (int i = 0; i < dependFieldNames.length; i++) {
                    DEPEND_FIELD_KV.put(dependFieldNames[i], (String) beanWrapper.getPropertyValue(dependFieldNames[i]));
                }
                return !(StringUtils.isEmpty(fieldValueInput) && new HashSet<>(DEPEND_FIELD_KV.values()).size() == dependFieldNames.length);
            }
            String dependFieldValueInput = (String) beanWrapper.getPropertyValue(dependFieldNames[0]);
            if(isRequiredFieldValue){
                if(Arrays.asList(requiredValues).contains("OTHER_FT_NOT_REQUIRED") && Arrays.asList(dependFieldValues).contains(dependFieldValueInput))
                    return Objects.isNull(propertyValue);
                if(Objects.isNull(propertyValue))
                    return Arrays.asList(dependFieldValues).contains(dependFieldValueInput);
                if("REFUND".equals(dependFieldValueInput))
                    return Arrays.asList(dependFieldValues).contains(dependFieldValueInput);
                return !( !Arrays.asList(requiredValues).contains(fieldValueInput) && Arrays.asList(dependFieldValues).contains(dependFieldValueInput));
            }
            return !(StringUtils.isEmpty(fieldValueInput) && Arrays.asList(dependFieldValues).contains(dependFieldValueInput));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
