package mc.scarecrow.lib.core;

import mc.scarecrow.lib.core.libinitializer.*;
import mc.scarecrow.lib.register.LibAutoRegister;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.ReflectionUtils;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static mc.scarecrow.lib.utils.ReflectionUtils.*;


public class LibCore {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Class<? extends ILibElement>, ILibElement> libCdi = new HashMap<>();
    private static final Map<Class<?>, Method> cdi = new HashMap<>();
    private static final Map<Class<?>, Object> cdiProducers = new HashMap<>();
    public static List<Class<?>> classesCache;


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
                            initializeInjections(elementClass, libElement);
                            libElement.postConstruct(modId, fmlJavaModLoadingContext);
                        }

                        libElements.remove(i);
                    }
                }
                // if in the last loop the size is the same will force to resolve
                unresolvable = currentSize == libElements.size();
            }

            initializeProducers();

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        } finally {
            LOGGER.info("Lib initialization time: " + Duration.between(start, LocalDateTime.now()).toMillis() + "ms");
        }
    }

    public static void handleInstance(Object o) {
        initializeInjections(o.getClass(), o);
    }

    public static void initializeInjections(Class<?> clazz, Object o) {
        try {
            if (clazz.getSuperclass() != null)
                initializeInjections(clazz.getSuperclass(), o);

            List<Field> fieldsAnnotated = ReflectionUtils.getFieldsAnnotated(Collections.singletonList(clazz), LibInject.class);
            for (Field field : fieldsAnnotated) {
                field.setAccessible(true);
                if (libCdi.containsKey(field.getType())) {
                    ILibElement availableCdi = libCdi.get(field.getType());
                    field.set(o, availableCdi);
                } else if (cdi.containsKey(field.getType())) {
                    Method method = cdi.get(field.getType());
                    Object result;
                    if (method.getParameterCount() > 0) {
                        InjectionPoint injectionPoint = new InjectionPoint(clazz);
                        result = method.invoke(cdiProducers.get(method.getDeclaringClass()), injectionPoint);
                    } else {
                        result = method.invoke(cdiProducers.get(method.getDeclaringClass()));
                    }
                    if (result != null)
                        field.set(o, result);
                } else if (hasParameterlessConstructor(field.getType())) {
                    field.set(o, field.getType().newInstance());
                }
            }
        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        }
    }

    private static void initializeProducers() {
        for (Class<?> clazz : classesCache) {
            try {
                List<Method> methodsAnnotated = ReflectionUtils.getMethodsAnnotated(clazz, LibProducer.class);
                for (Method method : methodsAnnotated) {
                    Class<?> returnType = method.getReturnType();
                    if (returnType == Void.TYPE)
                        continue;
                    if (hasParameterlessConstructor(clazz) || method.getModifiers() == Modifier.STATIC) {
                        if (method.getParameterCount() == 0) {
                            method.setAccessible(true);
                            cdi.put(method.getReturnType(), method);
                            cdiProducers.put(clazz, method.getModifiers() == Modifier.STATIC ? null : clazz.newInstance());
                        } else if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(InjectionPoint.class)) {
                            cdi.put(method.getReturnType(), method);
                            cdiProducers.put(clazz, method.getModifiers() == Modifier.STATIC ? null : clazz.newInstance());
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Class error: " + clazz.getCanonicalName());
                LogUtils.printError(LOGGER, e);
            }
        }
    }
}
