package mc.scarecrow.lib.core;

import mc.scarecrow.lib.core.libinitializer.ILibElement;
import mc.scarecrow.lib.core.libinitializer.LibElement;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import mc.scarecrow.lib.core.libinitializer.OnRegisterManuallyListener;
import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.ReflectionUtils;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static mc.scarecrow.lib.utils.ReflectionUtils.classToLibElement;
import static mc.scarecrow.lib.utils.ReflectionUtils.warnCircularInjectionReference;


public class LibCore {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Class<? extends ILibElement>, ILibElement> libCdi = new HashMap<>();
    public static List<Class<?>> classesCache;

    private static final OnRegisterManuallyListener onRegisterManuallyListener = libElement -> libCdi.put(libElement.getClass(), libElement);

    public static void initialize(FMLJavaModLoadingContext fmlJavaModLoadingContext, String modId) {
        classesCache = ReflectionUtils.getClassesFromModId(modId);
        initializeLib(modId, fmlJavaModLoadingContext);
        fmlJavaModLoadingContext.getModEventBus().register(new LibAutoRegister(modId));
    }

    private static void initializeLib(String modId, FMLJavaModLoadingContext fmlJavaModLoadingContext) {
        LocalDateTime start = LocalDateTime.now();
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

            // warning on log about circular reference injection
            warnCircularInjectionReference(libElements);

            // initialize internal dependency injections
            boolean unresolvable = false;
            while (libElements.size() > 0) {
                int currentSize = libElements.size();

                for (int i = currentSize - 1; i >= 0; i--) {
                    Class<? extends ILibElement> elementClass = libElements.get(i);
                    LibElement annotation = elementClass.getAnnotation(LibElement.class);

                    if (annotation.after().length == 0
                            || Arrays.stream(annotation.after()).allMatch(libCdi::containsKey)
                            || unresolvable) {

                        ILibElement libElement;
                        if ((libElement = classToLibElement(elementClass)) != null) {
                            libCdi.put(libElement.getClass(), libElement);
                            initializeInjections(elementClass);
                            libElement.postConstruct(modId, onRegisterManuallyListener, fmlJavaModLoadingContext);
                        }

                        libElements.remove(i);
                    }
                }
                // if in the last loop the size is the same will force to resolve
                unresolvable = currentSize == libElements.size();
            }

            // resolve external dependencies
            classesCache.stream().filter(c -> !libCdi.containsKey(c)).collect(Collectors.toSet()).forEach(LibCore::initializeInjections);
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        } finally {
            LOGGER.info("Lib initialization time: " + Duration.between(start, LocalDateTime.now()).toMillis() + "ms");
        }
    }

    public static void initializeInjections(Class<?> clazz) {
        try {
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
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }
}
