package mc.scarecrow.common.block;

import mc.scarecrow.common.block.tile.factory.ScarecrowTileFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

public class TestBlock extends Block {

    public TestBlock() {
        super(Block.Properties.create(Material.WOOD).sound(SoundType.CROP).hardnessAndResistance(2f, 10f));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return ScarecrowTileFactory.getInstance(world);
    }
}
