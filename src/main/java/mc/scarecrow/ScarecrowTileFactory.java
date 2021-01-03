package mc.scarecrow;

import mc.scarecrow.common.block.ClientScarecrowTile;
import mc.scarecrow.common.block.ScarecrowBaseTile;
import mc.scarecrow.common.block.ServerScarecrowTile;
import mc.scarecrow.common.network.IProxy;

import java.util.function.Supplier;

public enum ScarecrowTileFactory {

    client(ClientScarecrowTile::new),
    server(ServerScarecrowTile::new);

    private Supplier<ScarecrowBaseTile> supplier;

    ScarecrowTileFactory(Supplier<ScarecrowBaseTile> supplier) {
        this.supplier = supplier;
    }

    public static ScarecrowBaseTile getInstance(IProxy proxy) {
        return proxy.isClient() ? client.supplier.get() : server.supplier.get();
    }
}
