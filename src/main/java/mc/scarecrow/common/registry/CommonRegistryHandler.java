package mc.scarecrow.common.registry;

import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.container.ScarecrowContainer;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.entity.ScarecrowPlayerEntity;
import mc.scarecrow.lib.proxy.Proxy;
import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.register.annotation.*;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.extensions.IForgeContainerType;

public class CommonRegistryHandler {

    @AutoRegisterTileEntity(id = "scarecrow", blockId = "scarecrow")
    public static TileEntity registerTileEntity() {
        return new ScarecrowTile();
    }

    @AutoRegisterItem(id = "scarecrow")
    public static Item registerItem() {
        return new BlockItem(LibAutoRegister.BLOCKS.get("scarecrow"), new Item.Properties()
                .group(ItemGroup.BUILDING_BLOCKS)
                .maxStackSize(10));
    }

    @AutoRegisterBlock(id = "scarecrow")
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

    @AutoRegisterContainer(id = "scarecrow")
    public static ContainerType<?> registerScarecrowContainer() {
        return IForgeContainerType.create((windowId, inv, data) -> {
            BlockPos pos = data.readBlockPos();
            return new ScarecrowContainer(windowId, Proxy.PROXY.getPlayerWorld(), pos, inv);
        });
    }
}
