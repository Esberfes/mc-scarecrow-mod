package mc.scarecrow.lib.tile;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface TileSidedTickHandler extends ITickableTileEntity {

    void onTickServer(ServerWorld world, long serverTicks);

    @OnlyIn(Dist.CLIENT)
    void onTickClient(ClientWorld world, PlayerEntity clientPlayerEntity, long clientTicks);
}
