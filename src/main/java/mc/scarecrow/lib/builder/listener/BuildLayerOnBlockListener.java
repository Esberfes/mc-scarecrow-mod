package mc.scarecrow.lib.builder.listener;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public interface BuildLayerOnBlockListener {
    void onBlock(BlockPos placedPosition, BlockState placedBlock);
}
