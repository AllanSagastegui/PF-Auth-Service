package pe.ask.auth.input.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "JSON Web Key Set (JWKS) public keys response")
public record JwksResponse(
        @Schema(description = "List of public cryptographic JWK keys")
        List<JwksKeyResponse> keys
) {
}
