package com.hrms.security.crypto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Converter;

/**
 * JPA {@link AttributeConverter} that transparently encrypts/decrypts a String column
 * using {@link PiiEncryptor}. Apply via {@code @Convert(converter = PiiEncryptedConverter.class)}
 * on any column you want at-rest encrypted (PAN, Aadhaar, bank account, SSN, etc.).
 *
 * Hibernate instantiates JPA converters itself (not via Spring), so we use a static
 * {@link PiiEncryptorHolder} populated at Spring-context bootstrap. If the holder is not
 * yet initialised (e.g. very early app startup), the converter passes data through
 * unchanged so reads don't lose existing plaintext.
 */
@Converter
public class PiiEncryptedConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        PiiEncryptor enc = PiiEncryptorHolder.get();
        return enc == null ? attribute : enc.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        PiiEncryptor enc = PiiEncryptorHolder.get();
        return enc == null ? dbData : enc.decrypt(dbData);
    }

    /** Marker annotation alias for clarity: {@code @PiiColumn String panNumber;} */
    @java.lang.annotation.Target(java.lang.annotation.ElementType.FIELD)
    @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
    @Convert(converter = PiiEncryptedConverter.class)
    public @interface PiiColumn {}
}
