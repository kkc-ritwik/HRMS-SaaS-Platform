package com.hrms.security.crypto;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Bridge between Spring-managed {@link PiiEncryptor} and the Hibernate-instantiated
 * {@link PiiEncryptedConverter}. Spring calls afterPropertiesSet() at boot and we
 * stash the encryptor in a static reference the converter can read without DI.
 */
@Component
@RequiredArgsConstructor
public class PiiEncryptorHolder implements InitializingBean {

    private static volatile PiiEncryptor ENCRYPTOR;

    private final PiiEncryptor encryptor;

    @Override
    public void afterPropertiesSet() { ENCRYPTOR = encryptor; }

    public static PiiEncryptor get() { return ENCRYPTOR; }
}
