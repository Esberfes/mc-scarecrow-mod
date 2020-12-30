package mc.scarecrow.init;

import mc.scarecrow.blocks.ScarecrowBlock;
import mc.scarecrow.blocks.ScarecrowContainer;
import mc.scarecrow.blocks.ScarecrowTile;
import mc.scarecrow.entity.FakePlayerEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

public class RegistryHandler {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_IDENTIFIER);
    public static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_IDENTIFIER);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_IDENTIFIER);
    public static final DeferredRegister<ContainerType<?>> CONTAINERS = DeferredRegister.create(ForgeRegistries.CONTAINERS, MOD_IDENTIFIER);

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITIES, MOD_IDENTIFIER);

    public static final RegistryObject<EntityType<FakePlayerEntity>> FAKE_PLAYER = ENTITIES.register( "fake_player", () ->
            EntityType.Builder.<FakePlayerEntity>create( EntityClassification.MISC )
                    .disableSerialization()
                    .disableSummoning()
                    .size( 0, 0 )
                    .build( MOD_IDENTIFIER+ ":fake_player" ) );

    public static final RegistryObject<Block> scarecrowBlock = BLOCKS.register("scarecrow_block", ScarecrowBlock::new);
    public static final RegistryObject<Item> scarecrowBlockBuild = ITEMS.register("scarecrow_block", () ->
            new BlockItem(scarecrowBlock.get(), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS))
    );
    public static final RegistryObject<TileEntityType<ScarecrowTile>> scarecrowTileBlock = TILES.register(
            "scarecrow_block",
            () -> TileEntityType.Builder.create(ScarecrowTile::new, scarecrowBlock.get()).build(null)
    );

    public static void init() {
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        CONTAINERS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    public static final RegistryObject<ContainerType<ScarecrowContainer>> scarecrowBlockContainer = CONTAINERS.register("scarecrow_block", () ->
            new ContainerType<ScarecrowContainer>(RegistryHandler::create)
    );

    private static ScarecrowContainer create(int windowId, PlayerInventory playerInventory) {
        return new ScarecrowContainer(windowId, playerInventory);
    }

}