package mc.scarecrow.common.block.tile.strategy;

import mc.scarecrow.common.block.tile.base.ScarecrowBaseTile;

public class ClientScarecrowTile extends ScarecrowBaseTile {

    @Override
    protected boolean isClient() {
        return true;
    }

}
