package mc.scarecrow.common.network.packet;

import mc.scarecrow.common.block.ScarecrowTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScarecrowTogglePacket extends ScarecrowPacket {

    public ScarecrowTogglePacket(BlockPos pos) {
        super(pos);
    }

    public ScarecrowTogglePacket(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
    }

    @Override
    protected void decodeBuffer(PacketBuffer buffer) {
        super.decodeBuffer(buffer);
    }

    public static ScarecrowTogglePacket decode(PacketBuffer buffer) {
        return new ScarecrowTogglePacket(buffer);
    }

    @Override
    protected void handle(PlayerEntity player, World world, ScarecrowTile tile) {
        tile.toggle();
    }
}
