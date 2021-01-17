package mc.scarecrow.lib.core;

import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.register.libinitializer.ILibElement;
import mc.scarecrow.lib.register.libinitializer.LibElement;
import mc.scarecrow.lib.register.libinitializer.LibInject;
import mc.scarecrow.lib.register.libinitializer.OnRegisterManuallyListener;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.ReflectionUtils;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static mc.scarecrow.lib.utils.ReflectionUtils.classToLibElement;
import static mc.scarecrow.lib.utils.ReflectionUtils.isCircularInjectionReference;


public class LibCore {

    private static String MOD_ID;
    private static final Logger LOGGER = LogManager.getLogger();
    private static Map<Class<? extends ILibElement>, ILibElement> libCdi = new HashMap<>();
    public static List<Class<?>> classesCache;

    private static OnRegisterManuallyListener onRegisterManuallyListener = libElement -> libCdi.put(libElement.getClass(), libElement);

    public static void initialize(FMLJavaModLoadingContext fmlJavaModLoadingContext, String modId) {
        MOD_ID = modId;
        classesCache = ReflectionUtils.getClassesFromModId(MOD_ID);
        initializeLib(MOD_ID, fmlJavaModLoadingContext);
        fmlJavaModLoadingContext.getModEventBus().register(new LibAutoRegister(modId));
    }

    private static void initializeLib(String modId, FMLJavaModLoadingContext fmlJavaModLoadingContext) {
        try {
            List<Class<? extends ILibElement>> libElements = classesCache
                    .stream()
                    .map(ReflectionUtils::classToClassLibElement)
                    .filter(Objects::nonNull)
                    .sorted((o1, o2) -> {
                        LibElement annotation = o1.getAnnotation(LibElement.class);
                        LibElement annotationOther = o2.getAnnotation(LibElement.class);
                        return Integer.compare(annotation.after().length, annotationOther.after().length) * -1;
                    })
                    .collect(toList());

            Class<?> clazzError;
            if ((clazzError = isCircularInjectionReference(libElements)) != null)
                throw new RuntimeException("LibCore initializing fatal error, circular reference fond on class: " + clazzError.getSimpleName());

            while (libElements.size() > 0) {
                for (int i = libElements.size() - 1; i >= 0; i--) {
                    Class<? extends ILibElement> elementClass = libElements.get(i);
                    LibElement annotation = elementClass.getAnnotation(LibElement.class);
                    ILibElement libElement = null;
                    if (annotation.after().length == 0) {
                        libElement = classToLibElement(elementClass);
                        libElements.remove(i);
                    } else if (Arrays.stream(annotation.after()).allMatch(d -> libCdi.containsKey(d))) {
                        libElement = classToLibElement(elementClass);
                        libElements.remove(i);
                    }

                    if (libElement != null) {
                        libCdi.put(libElement.getClass(), libElement);
                        initializeInjections(elementClass);
                        libElement.initialize(modId, onRegisterManuallyListener, fmlJavaModLoadingContext);
                    }
                }
            }

            for (Class<?> clazz : classesCache.stream().filter(c -> !libCdi.containsKey(c)).collect(Collectors.toSet())) {
                initializeInjections(clazz);
            }

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    public static void initializeInjections(Class<?> clazz) throws IllegalAccessException {
        List<Field> fieldsAnnotated = ReflectionUtils.getFieldsAnnotated(Collections.singletonList(clazz), LibInject.class);
        for (Field field : fieldsAnnotated) {
            if (libCdi.containsKey(field.getType())) {
                field.setAccessible(true);
                if (libCdi.containsKey(clazz)) {
                    ILibElement availableCdi = libCdi.get(field.getType());
                    field.set(libCdi.get(clazz), availableCdi);
                } else if (Modifier.isStatic(field.getModifiers())) {
                    ILibElement availableCdi = libCdi.get(field.getType());
                    field.set(clazz, field.getType().cast(availableCdi));
                }
            }
        }
    }
}
