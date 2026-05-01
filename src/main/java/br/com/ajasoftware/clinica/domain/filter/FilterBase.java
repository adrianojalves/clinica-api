package br.com.ajasoftware.clinica.domain.filter;

import lombok.Getter;
import lombok.Setter;

/**
 * Base class for all search filters in the application.
 * Uses standard classes instead of records to allow inheritance.
 */
@Getter
@Setter
public abstract class FilterBase {
    private Long id;
    private String name;
}