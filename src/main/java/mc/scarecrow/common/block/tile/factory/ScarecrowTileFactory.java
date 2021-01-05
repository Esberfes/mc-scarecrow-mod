package mc.scarecrow.common.block.tile.factory;

import mc.scarecrow.common.block.tile.base.ScarecrowBaseTile;
import mc.scarecrow.common.block.tile.strategy.ClientScarecrowTile;
import mc.scarecrow.common.block.tile.strategy.ServerScarecrowTile;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.IBlockReader;

public abstract class ScarecrowTileFactory {

    public static ScarecrowBaseTile getInstance(IBlockReader world) {
        return world instanceof ClientWorld ? new ClientScarecrowTile() : new ServerScarecrowTile();
    }
}
