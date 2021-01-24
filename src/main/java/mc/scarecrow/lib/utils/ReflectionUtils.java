package mc.scarecrow.lib.utils;

import mc.scarecrow.lib.core.libinitializer.ILibElement;
import mc.scarecrow.lib.core.libinitializer.LibElement;
import mc.scarecrow.lib.core.libinitializer.LibInject;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.moddiscovery.ModAnnotation;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.ModFileScanData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@SuppressWarnings("unused")
public abstract class ReflectionUtils {

    private static final Logger LOGGER = LogManager.getLogger();

    public static List<Class<?>> getClassesFromModId(String modId) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            String[] a = new String[0];
            ModFileInfo modFileInfo;
            if ((modFileInfo = FMLLoader.getLoadingModList().getModFileById(modId)) != null) {
                ModFile file;
                if ((file = modFileInfo.getFile()) != null) {
                    List<String> blackList = new ArrayList<>();

                    if (FMLLoader.getDist() == Dist.DEDICATED_SERVER) {
                        file.getScanResult().getAnnotations().iterator().forEachRemaining(annotationData -> {
                            if (annotationData.getAnnotationType().getClassName().equals(OnlyIn.class.getCanonicalName())
                                    && annotationData.getTargetType() == ElementType.TYPE
                                    && ((ModAnnotation.EnumHolder) annotationData.getAnnotationData().get("value")).getValue().equals(Dist.CLIENT.name())) {

                                blackList.add(annotationData.getClassType().getClassName());
                            }
                        });
                    }

                    ArrayList<ModFileScanData.ClassData> classDatas = new ArrayList<>(file.getScanResult().getClasses());
                    for (ModFileScanData.ClassData classData : classDatas) {
                        Field fieldClazz = null;
                        try {
                            fieldClazz = ModFileScanData.ClassData.class.getDeclaredField("clazz");
                            fieldClazz.setAccessible(true);
                            Type typeClazz = (Type) fieldClazz.get(classData);
                            if (!blackList.contains(typeClazz.getClassName())) {
                                Class<?> aClass = Class.forName(typeClazz.getClassName());
                                classes.add(aClass);
                            }
                        } catch (Throwable e) {
                            LOGGER.error("Error getting info for class: " + (fieldClazz != null ? fieldClazz.getName() : "unknown"));
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

    public static boolean hasParameterlessConstructor(Class<?> clazz) {
        return Stream.of(clazz.getConstructors())
                .anyMatch((c) -> c.getParameterCount() == 0);
    }

    public static List<Field> getFieldsAnnotated(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(m -> m.isAnnotationPresent(annotation))
                .collect(toList());
    }

    private static List<Class<?>> getClassesAnnotated(List<Class<?>> classes, Class<? extends Annotation> annotation) {
        return classes.stream().filter(c -> c.isAnnotationPresent(annotation)).collect(toList());
    }

    public static ILibElement classToLibElement(Class<?> clazz) {
        try {
            return (ILibElement) clazz.newInstance();
        } catch (Throwable e) {
            LOGGER.error("Unable to create lib element: " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends ILibElement> classToClassLibElement(Class<?> clazz) {
        try {
            if (isLibElement(clazz))
                return (Class<? extends ILibElement>) clazz;

            return null;

        } catch (Throwable e) {
            LOGGER.error("Unable to create lib element: " + e.getMessage());
            return null;
        }
    }

    public static boolean isLibElement(Class<?> clazz) {
        return clazz != null
                && clazz.isAnnotationPresent(LibElement.class)
                && ILibElement.class.isAssignableFrom(clazz)
                && hasParameterlessConstructor(clazz);
    }

    public static void warnCircularInjectionReference(List<Class<? extends ILibElement>> classes) {
        for (Class<? extends ILibElement> clazz : classes) {
            // cojo mis dependencias
            List<Class<?>> dependencies = new ArrayList<>(getFieldsAnnotatedTypes(clazz, LibInject.class));
            // Si en una dependecia se dpende de mi es cirucular
            for (Class<?> clazzOther : dependencies) {
                List<Class<?>> otherDependencies = new ArrayList<>(getFieldsAnnotatedTypes(clazzOther, LibInject.class));
                if (otherDependencies.contains(clazz))
                    LOGGER.warn("Circular dependency found: " + clazz.getSimpleName());
            }
        }
    }

    public static List<Method> getMethodsAnnotated(List<Class<?>> classes, Class<? extends Annotation> annotation) {
        return classes.stream().map(c -> getMethodsAnnotated(c, annotation)
        ).flatMap(List::stream).collect(toList());
    }

    public static List<Method> getMethodsAnnotated(Class<?> clazz, Class<? extends Annotation> annotation) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(annotation))
                .collect(toList());
    }

    public static Set<Class<?>> getFieldsAnnotatedTypes(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getFieldsAnnotated(clazz, annotation).stream().map(Field::getType).collect(Collectors.toSet());
    }

    public static List<Field> getFieldsAnnotated(List<Class<?>> classes, Class<? extends Annotation> annotation) {
        return classes.stream().map(c -> getFieldsAnnotated(c, annotation))
                .flatMap(List::stream)
                .collect(toList());
    }
}
