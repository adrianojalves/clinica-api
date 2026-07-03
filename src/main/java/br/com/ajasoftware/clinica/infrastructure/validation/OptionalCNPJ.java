package br.com.ajasoftware.clinica.infrastructure.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for validating optional CNPJ values.
 * Allows null, empty or blank strings, but validates mathematically if populated.
 */
@Documented
@Constraint(validatedBy = OptionalCNPJValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface OptionalCNPJ {
    String message() default "O CNPJ informado é inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
