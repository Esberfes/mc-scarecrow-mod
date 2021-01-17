package mc.scarecrow.lib.network.executor;

import mc.scarecrow.lib.utils.MapBuilder;

public class NetworkCommandBuilder {

    private MapBuilder<String, Object> payload;
    private final String id;

    NetworkCommandBuilder(String id) {
        this.id = id;
        this.payload = new MapBuilder<>();
    }

    public NetworkCommandBuilder payload(MapBuilder<String, Object> payload) {
        this.payload = payload;
        return this;
    }

    public NetworkCommand build() {
        return new NetworkCommand(payload.build(), id);
    }
}
