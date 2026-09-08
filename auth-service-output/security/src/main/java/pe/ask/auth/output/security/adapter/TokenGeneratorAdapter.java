package pe.ask.auth.output.security.adapter;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.ECDSASigner;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import pe.ask.auth.core.model.AuthTokens;
import pe.ask.auth.core.model.Role;
import pe.ask.auth.core.model.Session;
import pe.ask.auth.core.model.User;
import pe.ask.auth.core.model.constant.DomainEventEnum;
import pe.ask.auth.core.model.exception.DomainValidation;
import pe.ask.auth.core.model.exception.TokenInitializationException;
import pe.ask.auth.core.port.out.TokenGeneratorOutputPort;
import pe.ask.auth.output.security.model.MfaChallenge;
import pe.ask.auth.output.security.model.SecurityEnum;
import pe.ask.auth.output.security.model.SecurityIntEnum;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public final class TokenGeneratorAdapter implements TokenGeneratorOutputPort {

    private final SecureRandom secureRandom;
    private final ECKey ecJwk;
    private final ECDSASigner signer;
    private final ConcurrentHashMap<String, MfaChallenge> mfaChallenges = new ConcurrentHashMap<>();

    public TokenGeneratorAdapter(ECKey ecJwk) {
        try {
            this.secureRandom = new SecureRandom();
            this.ecJwk = ecJwk != null ? ecJwk : new ECKeyGenerator(Curve.P_256).keyID(SecurityEnum.KEY_ID.value()).generate();
            this.signer = new ECDSASigner(this.ecJwk);
        } catch (JOSEException e) {
            throw new TokenInitializationException(SecurityEnum.ERR_INIT_TOKEN_GENERATOR.value(), e);
        }
    }

    public TokenGeneratorAdapter() {
        this(null);
    }

    public ECKey getEcJwk() {
        return ecJwk;
    }

    @Override
    public Mono<String> generateSecureToken() {
        return Mono.fromCallable(() -> {
            byte[] bytes = new byte[SecurityIntEnum.REFRESH_TOKEN_BYTES.value()];
            secureRandom.nextBytes(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        }).subscribeOn(Schedulers.parallel());
    }

    @Override
    public Mono<String> hashToken(String rawToken) {
        DomainValidation.requireNonNull(rawToken, SecurityEnum.PARAM_TOKEN.value());
        return Mono.fromCallable(() -> {
            MessageDigest digest = MessageDigest.getInstance(SecurityEnum.HASH_ALGORITHM_SHA256.value());
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        }).subscribeOn(Schedulers.parallel());
    }

    @Override
    public Mono<AuthTokens> issueTokens(User user, Session session, UUID familyId) {
        DomainValidation.requireNonNull(user, SecurityEnum.PARAM_USER_ID.value());
        DomainValidation.requireNonNull(session, SecurityEnum.CLAIM_SID.value());

        return generateSecureToken()
                .flatMap(rawRefreshToken -> Mono.fromCallable(() -> {
                    Instant now = Instant.now();
                    Instant exp = now.plusSeconds(SecurityIntEnum.ACCESS_TOKEN_TTL_SECONDS.value());

                    List<String> roles = user.roles().stream()
                            .map(Role::name)
                            .collect(Collectors.toList());

                    JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.ES256)
                            .keyID(ecJwk.getKeyID())
                            .type(new JOSEObjectType(SecurityEnum.TOKEN_TYPE_JWT.value()))
                            .build();

                    JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                            .issuer(SecurityEnum.ISSUER.value())
                            .subject(user.id().toString())
                            .audience(SecurityEnum.AUDIENCE.value())
                            .issueTime(Date.from(now))
                            .notBeforeTime(Date.from(now))
                            .expirationTime(Date.from(exp))
                            .jwtID(generateSafeUuid())
                            .claim(SecurityEnum.CLAIM_SID.value(), session.id().toString())
                            .claim(SecurityEnum.CLAIM_ROLES.value(), roles)
                            .claim(SecurityEnum.CLAIM_SCOPE.value(), SecurityEnum.SCOPE_OPENID_PROFILE.value())
                            .claim(SecurityEnum.CLAIM_VER.value(), user.authVersion())
                            .claim(SecurityEnum.CLAIM_AMR.value(), List.of(user.mfaEnabled() ? DomainEventEnum.AMR_MFA.value() : DomainEventEnum.AMR_PASSWORD.value()))
                            .claim(SecurityEnum.CLAIM_AUTH_TIME.value(), now.getEpochSecond())
                            .build();

                    SignedJWT signedJWT = new SignedJWT(header, claimsSet);
                    signedJWT.sign(signer);

                    String jwtString = signedJWT.serialize();
                    return AuthTokens.ofBearer(jwtString, rawRefreshToken, (long) SecurityIntEnum.ACCESS_TOKEN_TTL_SECONDS.value());
                }).subscribeOn(Schedulers.parallel()));
    }

    @Override
    public Mono<String> createMfaChallengeToken(UUID userId, UUID deviceId) {
        DomainValidation.requireNonNull(userId, SecurityEnum.PARAM_USER_ID.value());
        DomainValidation.requireNonNull(deviceId, SecurityEnum.CLAIM_SID.value());

        return generateSecureToken()
                .doOnNext(token -> {
                    Instant exp = Instant.now().plusSeconds(SecurityIntEnum.MFA_CHALLENGE_TTL_SECONDS.value());
                    mfaChallenges.put(token, new MfaChallenge(userId, deviceId, exp));
                });
    }

    @Override
    public Mono<UUID> verifyMfaChallengeToken(String mfaChallengeToken) {
        return Mono.justOrEmpty(mfaChallengeToken)
                .mapNotNull(mfaChallenges::remove)
                .filter(challenge -> !challenge.expiresAt().isBefore(Instant.now()))
                .map(MfaChallenge::userId);
    }

    private static String generateSafeUuid() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return new UUID(random.nextLong(), random.nextLong()).toString();
    }
}
