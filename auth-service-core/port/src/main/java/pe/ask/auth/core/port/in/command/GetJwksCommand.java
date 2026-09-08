package pe.ask.auth.core.port.in.command;

@SuppressWarnings("java:S2094")
public record GetJwksCommand() {

    public static GetJwksCommand create() {
        return new GetJwksCommand();
    }
}
