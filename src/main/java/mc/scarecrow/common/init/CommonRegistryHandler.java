package mc.scarecrow.common.init;

import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.entity.ScarecrowPlayerEntity;
import mc.scarecrow.lib.register.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;

public class CommonRegistryHandler {

    @AutoRegisterTileEntity(id = "scarecrow_block", blockId = "scarecrow_block")
    public static TileEntity registerTileEntity() {
        return new ScarecrowTile();
    }

    @AutoRegisterItem(id = "scarecrow_block")
    public static Item registerItem() {
        return new BlockItem(AutoRegister.BLOCKS.get("scarecrow_block"), new Item.Properties()
                .group(ItemGroup.BUILDING_BLOCKS)
                .maxStackSize(10));
    }

    @AutoRegisterBlock(id = "scarecrow_block")
    public static Block registerBlock() {
        return new ScarecrowBlock();
    }

    @AutoRegisterEntity(id = "fake_player")
    public static EntityType.Builder<?> registerFakePlayer() {
        return EntityType.Builder.<ScarecrowPlayerEntity>create(EntityClassification.MISC)
                .disableSerialization()
                .disableSummoning()
                .size(0, 0);
    }
}
