package mc.scarecrow.lib.register;

import mc.scarecrow.lib.core.LibInstanceHandler;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.proxy.Proxy;
import mc.scarecrow.lib.register.annotation.*;
import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static mc.scarecrow.lib.core.LibCore.classesCache;

public class LibAutoRegister extends LibInstanceHandler {

    public static Map<String, Block> BLOCKS = new HashMap<>();
    public static Map<String, Item> ITEMS = new HashMap<>();
    public static Map<String, TileEntityType<?>> TILE_ENTITIES = new HashMap<>();
    public static Map<String, ContainerType<?>> CONTAINERS = new HashMap<>();
    public static Map<String, EntityType<?>> ENTITIES = new HashMap<>();

    @LibInject
    private Logger logger;

    private String MOD_ID;

    public LibAutoRegister(String MOD_ID) {
        this.MOD_ID = MOD_ID;
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        try {
            for (Class<?> clazz : classesCache) {
                Map<Method, AutoRegisterBlock> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterBlock.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterBlock.class)));

                for (Map.Entry<Method, AutoRegisterBlock> entry : methods.entrySet()) {
                    String id = entry.getValue().id();
                    Block block = (Block) entry.getKey().invoke(clazz);
                    block.setRegistryName(id);

                    event.getRegistry().register(block);
                    LibAutoRegister.BLOCKS.put(id, block);

                    logger.info("Registered Block with id: " + id + ", and type: "
                            + (block.getRegistryName() != null ? block.getRegistryName().toString() : id)
                            + " on side: " + Proxy.PROXY.getSide());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        try {
            for (Class<?> clazz : classesCache) {
                Map<Method, AutoRegisterItem> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterItem.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterItem.class)));

                for (Map.Entry<Method, AutoRegisterItem> entry : methods.entrySet()) {
                    String id = entry.getValue().id();
                    Item item = (Item) entry.getKey().invoke(clazz);
                    item.setRegistryName(id);

                    event.getRegistry().register(item);
                    LibAutoRegister.ITEMS.put(id, item);

                    logger.info("Registered Item with id: " + id + ", and type: " + item.getName().getString() + " on side: " + Proxy.PROXY.getSide());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    @SubscribeEvent
    public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        try {
            for (Class<?> clazz : classesCache) {
                Map<Method, AutoRegisterTileEntity> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterTileEntity.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterTileEntity.class)));

                for (Map.Entry<Method, AutoRegisterTileEntity> entry : methods.entrySet()) {
                    String id = entry.getValue().id();
                    String blockId = entry.getValue().blockId();

                    TileEntityType<?> tileEntityType = TileEntityType.Builder
                            .create(() -> {
                                try {
                                    return (TileEntity) entry.getKey().invoke(clazz);
                                } catch (Throwable e) {
                                    LogUtils.printError(logger, e);
                                    return null;
                                }
                            }, LibAutoRegister.BLOCKS.get(blockId))
                            .build(null);

                    tileEntityType.setRegistryName(id);

                    event.getRegistry().register(tileEntityType);
                    LibAutoRegister.TILE_ENTITIES.put(id, tileEntityType);

                    logger.info("Registered TileEntity with id: " + id + ", and type: " + (tileEntityType.getRegistryName() != null ? tileEntityType.getRegistryName().toString() : id) + " on side: " + Proxy.PROXY.getSide());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    @SubscribeEvent
    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        try {
            for (Class<?> clazz : classesCache) {
                Map<Method, AutoRegisterEntity> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterEntity.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterEntity.class)));

                for (Map.Entry<Method, AutoRegisterEntity> entry : methods.entrySet()) {
                    String id = entry.getValue().id();

                    EntityType.Builder<?> builder = (EntityType.Builder<?>) entry.getKey().invoke(clazz);
                    EntityType<?> entityType = builder.build(MOD_ID + ":" + id);
                    entityType.setRegistryName(id);

                    event.getRegistry().register(entityType);
                    LibAutoRegister.ENTITIES.put(id, entityType);

                    logger.info("Registered Entity with id: " + id + ", and type: " + entityType.getName().getString() + " on side: " + Proxy.PROXY.getSide());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }

    @SubscribeEvent
    public void registerContainer(RegistryEvent.Register<ContainerType<?>> event) {
        try {
            for (Class<?> clazz : classesCache) {
                Map<Method, AutoRegisterContainer> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterContainer.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterContainer.class)));

                for (Map.Entry<Method, AutoRegisterContainer> entry : methods.entrySet()) {
                    String id = entry.getValue().id();
                    ContainerType<?> containerType = (ContainerType<?>) entry.getKey().invoke(clazz);
                    containerType.setRegistryName(id);

                    event.getRegistry().register(containerType);
                    LibAutoRegister.CONTAINERS.put(id, containerType);

                    logger.info("Registered Container with id: " + id + ", and type: " + containerType.getRegistryName() + " on side: " + Proxy.PROXY.getSide());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(logger, e);
        }
    }
}
