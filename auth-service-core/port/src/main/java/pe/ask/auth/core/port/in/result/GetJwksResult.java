package pe.ask.auth.core.port.in.result;

import java.util.Collections;
import java.util.List;

public record GetJwksResult(List<JwksKeyResult> keys) {
    public GetJwksResult {
        keys = keys != null ? Collections.unmodifiableList(keys) : Collections.emptyList();
    }
}
