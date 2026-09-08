package pe.ask.auth.core.port.in.result;

import java.util.Collections;
import java.util.List;

public record GetSessionsResult(List<SessionSummary> sessions) {
    public GetSessionsResult {
        sessions = sessions != null ? Collections.unmodifiableList(sessions) : Collections.emptyList();
    }
}
