package mc.scarecrow.common.network.packet;

import mc.scarecrow.common.block.ScarecrowTile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ScarecrowTogglePacket extends ScarecrowPacket {
    private int xOffset, zOffset;

    public ScarecrowTogglePacket(BlockPos pos, int xOffset, int zOffset) {
        super(pos);
        this.xOffset = xOffset;
        this.zOffset = zOffset;
    }

    public ScarecrowTogglePacket(PacketBuffer buffer) {
        super(buffer);
    }

    @Override
    public void encode(PacketBuffer buffer) {
        super.encode(buffer);
        buffer.writeInt(this.xOffset);
        buffer.writeInt(this.zOffset);
    }

    @Override
    protected void decodeBuffer(PacketBuffer buffer) {
        super.decodeBuffer(buffer);
        this.xOffset = buffer.readInt();
        this.zOffset = buffer.readInt();
    }

    public static ScarecrowTogglePacket decode(PacketBuffer buffer) {
        return new ScarecrowTogglePacket(buffer);
    }

    @Override
    protected void handle(PlayerEntity player, World world, ScarecrowTile tile) {
        tile.toggle(this.xOffset, this.zOffset);
    }
}
