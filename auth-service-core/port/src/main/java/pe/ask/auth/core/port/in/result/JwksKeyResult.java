package pe.ask.auth.core.port.in.result;

public record JwksKeyResult(
        String kty,
        String crv,
        String kid,
        String use,
        String alg,
        String x,
        String y
) {
}
