package mc.scarecrow.common.block;

import mc.scarecrow.common.block.tile.ScarecrowTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.ToolType;

public class ScarecrowBlock extends Block {

    public ScarecrowBlock() {
        super(Properties
                .create(Material.IRON, MaterialColor.GRAY)
                .sound(SoundType.STONE)
                .hardnessAndResistance(1.5f, 6)
                .harvestLevel(1)
                .harvestTool(ToolType.PICKAXE));
    }

    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        // return ScarecrowTileFactory.getInstance(ScarecrowMod.PROXY);
        return new ScarecrowTile();
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }
}
