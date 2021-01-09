package mc.scarecrow.common.init;

import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.common.entity.ScarecrowPlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

public class CommonRegistryHandler {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_IDENTIFIER);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_IDENTIFIER);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_IDENTIFIER);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_IDENTIFIER);

    public static final RegistryObject<Block> scarecrowBlock = BLOCKS.register("scarecrow_block", ScarecrowBlock::new);
    public static final RegistryObject<Item> scarecrowBlockItem = ITEMS.register(scarecrowBlock.getId().getPath(), () -> new BlockItem(scarecrowBlock.get(), new Item.Properties()
            .group(ItemGroup.BUILDING_BLOCKS)
            .maxStackSize(10))
    );

    @SuppressWarnings("ConstantConditions")
    public static final RegistryObject<TileEntityType<ScarecrowTile>> scarecrowTileBlock = TILES.register(
            "scarecrow_block",
            () -> TileEntityType.Builder.create(ScarecrowTile::new, scarecrowBlock.get()).build(null)
    );

    public static final RegistryObject<EntityType<ScarecrowPlayerEntity>> FAKE_PLAYER = ENTITIES.register("fake_player", () ->
            EntityType.Builder.<ScarecrowPlayerEntity>create(EntityClassification.MISC)
                    .disableSerialization()
                    .disableSummoning()
                    .size(0, 0)
                    .build(MOD_IDENTIFIER + ":fake_player"));

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}