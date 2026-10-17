package dev.rlcraftceaddons.sdtemperaturehud.integration.xaero;

public final class RegistrationGate {
    private Object attached;
    private boolean failed;
    public boolean needsRegistration(Object handler) {return !failed && handler!=null && handler!=attached;}
    public void attached(Object handler) {attached=handler;}
    public void fail() {failed=true;}
    public boolean isFailed() {return failed;}
}
