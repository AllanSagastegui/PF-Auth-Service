package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWK public key description")
public record JwksKeyResponse(
        @Schema(description = "Key type", example = "RSA")
        String kty,

        @Schema(description = "Cryptographic curve", example = "P-256")
        String crv,

        @Schema(description = "Key identifier", example = "ask-auth-key-2026-01")
        String kid,

        @Schema(description = "Intended key use", example = "sig")
        String use,

        @Schema(description = "Target algorithm", example = "RS256")
        String alg,

        @Schema(description = "Modulus or X-coordinate", example = "u1WguNzsJwgIqV0EA3XGMsV9WU...")
        String x,

        @Schema(description = "Exponent or Y-coordinate", example = "AQAB")
        String y
) {
}
