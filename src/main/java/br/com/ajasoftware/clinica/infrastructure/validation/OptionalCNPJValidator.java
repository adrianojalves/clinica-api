package br.com.ajasoftware.clinica.infrastructure.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Validator implementation for OptionalCNPJ annotation.
 */
public class OptionalCNPJValidator implements ConstraintValidator<OptionalCNPJ, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // If the value is null, empty, or only whitespace, it is valid (not mandatory)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Validate mathematically if present
        String numbers = value.replaceAll("\\D", "");
        if (numbers.length() != 14 || numbers.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            int[] weight1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] weight2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += Character.getNumericValue(numbers.charAt(i)) * weight1[i];
            }
            int r = sum % 11;
            int digit1 = (r < 2) ? 0 : (11 - r);

            sum = 0;
            for (int i = 0; i < 13; i++) {
                sum += Character.getNumericValue(numbers.charAt(i)) * weight2[i];
            }
            r = sum % 11;
            int digit2 = (r < 2) ? 0 : (11 - r);

            return digit1 == Character.getNumericValue(numbers.charAt(12)) &&
                   digit2 == Character.getNumericValue(numbers.charAt(13));

        } catch (Exception e) {
            return false;
        }
    }
}
