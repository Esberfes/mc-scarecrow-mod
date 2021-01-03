package mc.scarecrow.common.network.packet;

import mc.scarecrow.common.block.ScarecrowTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public abstract class ScarecrowPacket {

    protected BlockPos pos;

    public ScarecrowPacket(BlockPos pos) {
        this.pos = pos;
    }

    public ScarecrowPacket(PacketBuffer buffer) {
        this.decodeBuffer(buffer);
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeBlockPos(this.pos);
    }

    protected void decodeBuffer(PacketBuffer buffer) {
        this.pos = buffer.readBlockPos();
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().setPacketHandled(true);

        PlayerEntity player = contextSupplier.get().getSender();
        if (player == null || player.getPosition().distanceSq(this.pos) > 32 * 32)
            return;
        World world = player.world;
        if (world == null)
            return;
        TileEntity tile = world.getTileEntity(this.pos);
        if (tile instanceof ScarecrowTile)
            contextSupplier.get().enqueueWork(() -> this.handle(player, world, (ScarecrowTile) tile));
    }

    protected abstract void handle(PlayerEntity player, World world, ScarecrowTile tile);

}
