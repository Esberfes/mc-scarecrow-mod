package mc.scarecrow.lib.network.executor;

import java.util.Map;

public class NetworkCommand {

    private final Map<String, Object> payload;
    private String id;

    public NetworkCommand(Map<String, Object> payload, String id) {
        this.payload = payload;
        this.id = id;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
