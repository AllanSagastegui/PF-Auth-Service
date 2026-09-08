package pe.ask.auth.output.security.adapter;

import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.TotpCryptoException;
import pe.ask.auth.core.port.out.TotpOutputPort;
import pe.ask.auth.output.security.model.SecurityEnum;
import pe.ask.auth.output.security.model.SecurityIntEnum;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

public final class TotpAdapter implements TotpOutputPort {

    private final byte[] encryptionKey;
    private final SecureRandom secureRandom;

    public TotpAdapter(byte[] encryptionKey) {
        this.encryptionKey = encryptionKey != null ? encryptionKey.clone() : new byte[32];
        this.secureRandom = new SecureRandom();
        if (encryptionKey == null) {
            this.secureRandom.nextBytes(this.encryptionKey);
        }
    }

    public TotpAdapter() {
        this(null);
    }

    @Override
    public Mono<String> generateSecret() {
        return Mono.fromCallable(() -> {
            byte[] bytes = new byte[20];
            secureRandom.nextBytes(bytes);
            return encodeBase32(bytes);
        }).subscribeOn(Schedulers.parallel());
    }

    @Override
    public Mono<String> encryptSecret(String rawSecret) {
        DomainValidation.requireNonNull(rawSecret, SecurityEnum.PARAM_SECRET.value());
        return Mono.fromCallable(() -> {
            try {
                byte[] iv = new byte[SecurityIntEnum.GCM_IV_LENGTH.value()];
                secureRandom.nextBytes(iv);

                Cipher cipher = Cipher.getInstance(SecurityEnum.CIPHER_AES_GCM_NO_PADDING.value());
                cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptionKey, SecurityEnum.ALGORITHM_AES.value()), new GCMParameterSpec(SecurityIntEnum.GCM_TAG_LENGTH.value(), iv));

                byte[] cipherText = cipher.doFinal(rawSecret.getBytes(StandardCharsets.UTF_8));
                byte[] combined = new byte[iv.length + cipherText.length];
                System.arraycopy(iv, 0, combined, 0, iv.length);
                System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

                return Base64.getEncoder().encodeToString(combined);
            } catch (GeneralSecurityException e) {
                throw new TotpCryptoException(SecurityEnum.ERR_ENCRYPT_TOTP.value(), e);
            }
        }).subscribeOn(Schedulers.parallel());
    }

    @Override
    public Mono<String> decryptSecret(String encryptedSecret) {
        DomainValidation.requireNonNull(encryptedSecret, SecurityEnum.PARAM_SECRET.value());
        return Mono.fromCallable(() -> {
            try {
                byte[] combined = Base64.getDecoder().decode(encryptedSecret);
                if (combined.length < SecurityIntEnum.GCM_IV_LENGTH.value()) {
                    throw new TotpCryptoException(SecurityEnum.ERR_INVALID_ENCRYPTED_PAYLOAD.value());
                }

                byte[] iv = new byte[SecurityIntEnum.GCM_IV_LENGTH.value()];
                System.arraycopy(combined, 0, iv, 0, SecurityIntEnum.GCM_IV_LENGTH.value());

                byte[] cipherText = new byte[combined.length - SecurityIntEnum.GCM_IV_LENGTH.value()];
                System.arraycopy(combined, SecurityIntEnum.GCM_IV_LENGTH.value(), cipherText, 0, cipherText.length);

                Cipher cipher = Cipher.getInstance(SecurityEnum.CIPHER_AES_GCM_NO_PADDING.value());
                cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(encryptionKey, SecurityEnum.ALGORITHM_AES.value()), new GCMParameterSpec(SecurityIntEnum.GCM_TAG_LENGTH.value(), iv));

                byte[] decrypted = cipher.doFinal(cipherText);
                return new String(decrypted, StandardCharsets.UTF_8);
            } catch (TotpCryptoException e) {
                throw e;
            } catch (GeneralSecurityException | IllegalArgumentException e) {
                throw new TotpCryptoException(SecurityEnum.ERR_DECRYPT_TOTP.value(), e);
            }
        }).subscribeOn(Schedulers.parallel());
    }

    @Override
    public Mono<Boolean> verifyCode(String rawSecret, String code) {
        return Mono.justOrEmpty(rawSecret)
                .filter(secret -> code != null && code.length() == SecurityIntEnum.TOTP_CODE_DIGITS.value())
                .flatMap(secret -> {
                    long currentWindow = Instant.now().getEpochSecond() / SecurityIntEnum.TOTP_TIME_STEP_SECONDS.value();
                    return Flux.range(-1, 3)
                            .map(offset -> currentWindow + offset)
                            .flatMap(timeIndex -> Mono.fromCallable(() -> generateOtpCode(secret, timeIndex))
                                    .subscribeOn(Schedulers.parallel()))
                            .any(code::equals);
                })
                .defaultIfEmpty(false);
    }

    @Override
    public Mono<String> generateTotpUri(String email, String secret) {
        return Mono.fromCallable(() -> {
            String issuer = URLEncoder.encode(SecurityEnum.OTP_ISSUER_NAME.value(), StandardCharsets.UTF_8);
            String account = URLEncoder.encode(email, StandardCharsets.UTF_8);
            return SecurityEnum.OTP_SCHEME_PREFIX.value() + issuer + ":" + account + "?secret=" + secret + "&issuer=" + issuer;
        });
    }

    private String generateOtpCode(String base32Secret, long timeIndex) throws GeneralSecurityException {
        byte[] key = decodeBase32(base32Secret);
        byte[] data = ByteBuffer.allocate(8).putLong(timeIndex).array();

        Mac mac = Mac.getInstance(SecurityEnum.ALGORITHM_HMAC_SHA1.value());
        mac.init(new SecretKeySpec(key, SecurityEnum.ALGORITHM_HMAC_SHA1.value()));
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, SecurityIntEnum.TOTP_CODE_DIGITS.value());
        return String.format("%06d", otp);
    }

    private static String encodeBase32(byte[] data) {
        StringBuilder sb = new StringBuilder();
        int buffer = 0;
        int bitsLeft = 0;
        for (byte b : data) {
            buffer = (buffer << 8) | (b & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                sb.append(SecurityEnum.BASE32_ALPHABET.value().charAt((buffer >> (bitsLeft - 5)) & 31));
                bitsLeft -= 5;
            }
        }
        if (bitsLeft > 0) {
            sb.append(SecurityEnum.BASE32_ALPHABET.value().charAt((buffer << (5 - bitsLeft)) & 31));
        }
        return sb.toString();
    }

    private static byte[] decodeBase32(String base32) {
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        ByteBuffer bytes = ByteBuffer.allocate(clean.length() * 5 / 8 + 1);
        int buffer = 0;
        int bitsLeft = 0;
        for (char c : clean.toCharArray()) {
            int val = SecurityEnum.BASE32_ALPHABET.value().indexOf(c);
            if (val < 0) continue;
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes.put((byte) ((buffer >> (bitsLeft - 8)) & 0xFF));
                bitsLeft -= 8;
            }
        }
        bytes.flip();
        byte[] result = new byte[bytes.remaining()];
        bytes.get(result);
        return result;
    }
}
