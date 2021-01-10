package mc.scarecrow.lib.register;

import mc.scarecrow.lib.utils.LogUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

import static mc.scarecrow.constant.ScarecrowModConstants.MOD_IDENTIFIER;

@Mod.EventBusSubscriber(modid = MOD_IDENTIFIER, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AutoRegister {

    private static final Logger LOGGER = LogManager.getLogger();

    public static Map<String, Block> BLOCKS = new HashMap<>();
    public static Map<String, Item> ITEMS = new HashMap<>();
    public static Map<String, TileEntityType<?>> TILE_ENTITIES = new HashMap<>();
    public static Map<String, EntityType<?>> ENTITIES = new HashMap<>();

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        try {
            for (Class<?> clazz : getClassesFromModId(MOD_IDENTIFIER)) {
                Map<Method, AutoRegisterBlock> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterBlock.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterBlock.class)));

                for (Map.Entry<Method, AutoRegisterBlock> entry : methods.entrySet()) {
                    String id = entry.getValue().id();
                    Block block = (Block) entry.getKey().invoke(clazz);
                    block.setRegistryName(id);

                    event.getRegistry().register(block);
                    BLOCKS.put(id, block);

                    LOGGER.info("Registered Block with id: " + id + ", and type: " + (block.getRegistryName() != null ? block.getRegistryName().toString() : id) + " on side: " + FMLLoader.getDist());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        try {
            for (Class<?> clazz : getClassesFromModId(MOD_IDENTIFIER)) {
                Map<Method, AutoRegisterItem> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterItem.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterItem.class)));

                for (Map.Entry<Method, AutoRegisterItem> entry : methods.entrySet()) {
                    String id = entry.getValue().id();
                    Item item = (Item) entry.getKey().invoke(clazz);
                    item.setRegistryName(id);

                    event.getRegistry().register(item);
                    ITEMS.put(id, item);

                    LOGGER.info("Registered Item with id: " + id + ", and type: " + item.getName().getString() + " on side: " + FMLLoader.getDist());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @SubscribeEvent
    public static void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        try {
            for (Class<?> clazz : getClassesFromModId(MOD_IDENTIFIER)) {
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
                                    LogUtils.printError(LOGGER, e);
                                    return null;
                                }
                            }, AutoRegister.BLOCKS.get(blockId))
                            .build(null);

                    tileEntityType.setRegistryName(id);

                    event.getRegistry().register(tileEntityType);
                    TILE_ENTITIES.put(id, tileEntityType);

                    LOGGER.info("Registered TileEntity with id: " + id + ", and type: " + (tileEntityType.getRegistryName() != null ? tileEntityType.getRegistryName().toString() : id) + " on side: " + FMLLoader.getDist());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        try {
            for (Class<?> clazz : getClassesFromModId(MOD_IDENTIFIER)) {
                Map<Method, AutoRegisterEntity> methods = Arrays.stream(clazz.getDeclaredMethods())
                        .filter(m -> m.isAnnotationPresent(AutoRegisterEntity.class))
                        .collect(Collectors.toMap((a) -> a, (a) -> a.getAnnotation(AutoRegisterEntity.class)));

                for (Map.Entry<Method, AutoRegisterEntity> entry : methods.entrySet()) {
                    String id = entry.getValue().id();

                    EntityType.Builder<?> builder = (EntityType.Builder<?>) entry.getKey().invoke(clazz);
                    EntityType<?> entityType = builder.build(MOD_IDENTIFIER + ":" + id);
                    entityType.setRegistryName(id);

                    event.getRegistry().register(entityType);
                    ENTITIES.put(id, entityType);

                    LOGGER.info("Registered Entity with id: " + id + ", and type: " + entityType.getName().getString() + " on side: " + FMLLoader.getDist());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    private static List<Class<?>> getClassesFromModId(String modId) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            ModFileInfo modFileInfo;
            if ((modFileInfo = FMLLoader.getLoadingModList().getModFileById(modId)) != null) {
                ModFile file;
                if ((file = modFileInfo.getFile()) != null) {
                    ArrayList<ModFileScanData.ClassData> classDatas = new ArrayList<>(file.getScanResult().getClasses());
                    for (ModFileScanData.ClassData classData : classDatas) {
                        try {
                            Field fieldClazz = ModFileScanData.ClassData.class.getDeclaredField("clazz");
                            fieldClazz.setAccessible(true);
                            Type typeClazz = (Type) fieldClazz.get(classData);
                            Class<?> aClass = Class.forName(typeClazz.getClassName());
                            classes.add(aClass);
                        } catch (Throwable e) {
                            LogUtils.printError(LOGGER, e);
                        }
                    }
                }
            }

            return classes;

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            return classes;
        }
    }
}
