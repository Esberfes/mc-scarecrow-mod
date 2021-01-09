package mc.scarecrow.lib.tile;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.server.ServerWorld;

public interface TileSidedTickHandler extends ITickableTileEntity {

    void onTickServer(ServerWorld world, long serverTicks);

    void onTickClient(ClientWorld world, long clientTicks);
}
