package mc.scarecrow.common.block.tile.strategy;

import mc.scarecrow.common.block.tile.base.ScarecrowBaseTile;

public class ServerScarecrowTile extends ScarecrowBaseTile {

    @Override
    protected boolean isClient() {
        return false;
    }

}
