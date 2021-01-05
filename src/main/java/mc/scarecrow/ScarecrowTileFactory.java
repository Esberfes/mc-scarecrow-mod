package mc.scarecrow;

import mc.scarecrow.common.block.tile.base.ScarecrowBaseTile;
import mc.scarecrow.common.block.tile.strategy.ClientScarecrowTile;
import mc.scarecrow.common.block.tile.strategy.ServerScarecrowTile;
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
