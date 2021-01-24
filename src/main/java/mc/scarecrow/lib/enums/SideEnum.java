package mc.scarecrow.lib.enums;

public enum SideEnum {
    client(false), serverOnClient(true), dedicated(true), server(true);

    private final boolean isServerLogic;

    SideEnum(boolean isServerLogic) {
        this.isServerLogic = isServerLogic;
    }

    public boolean isServerLogic() {
        return isServerLogic;
    }

    public SideEnum server() {
        return SideEnum.server;
    }

    public SideEnum client() {
        return SideEnum.client;
    }
}
