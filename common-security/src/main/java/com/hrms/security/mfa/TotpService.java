package com.hrms.security.mfa;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.recovery.RecoveryCodeGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TotpService {

    private final SecretGenerator secrets = new DefaultSecretGenerator();
    private final RecoveryCodeGenerator recoveryGen = new RecoveryCodeGenerator();
    private final TimeProvider time = new SystemTimeProvider();
    private final CodeGenerator codeGen = new DefaultCodeGenerator(HashingAlgorithm.SHA1, 6);
    private final CodeVerifier verifier = new DefaultCodeVerifier(codeGen, time);

    public String newSecret() { return secrets.generate(); }

    public String qrPngDataUri(String secret, String issuer, String userEmail) {
        QrData data = new QrData.Builder().label(userEmail).secret(secret).issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build();
        try {
            byte[] png = new ZxingPngQrGenerator().generate(data);
            return "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(png);
        } catch (Exception e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }

    public boolean verify(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }

    public String[] recoveryCodes(int count) { return recoveryGen.generateCodes(count); }
}
