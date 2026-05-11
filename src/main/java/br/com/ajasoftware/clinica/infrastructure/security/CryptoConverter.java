package br.com.ajasoftware.clinica.infrastructure.security;

import br.com.ajasoftware.clinica.infrastructure.CryptoUtils;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * JPA Converter to automatically encrypt and decrypt entity fields.
 * Applies the SRP (Single Responsibility Principle) by decoupling
 * security logic from domain entities.
 */
@Converter
@Component
public class CryptoConverter implements AttributeConverter<Object, String> {

    private static CryptoUtils cryptoUtils;

    @Autowired
    public void setCryptoUtils(CryptoUtils utils) {
        CryptoConverter.cryptoUtils = utils;
    }

    /**
     * Encrypts the attribute before saving to the database.
     */
    @Override
    public String convertToDatabaseColumn(Object attribute) {
        if (attribute == null) return null;
        return cryptoUtils.encrypt(attribute.toString());
    }

    /**
     * Decrypts the database column when reading the entity.
     */
    @Override
    public Object convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return null;
        return cryptoUtils.decrypt(dbData);
    }
}