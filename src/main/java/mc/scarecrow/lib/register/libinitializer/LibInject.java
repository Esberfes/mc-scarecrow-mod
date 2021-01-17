package mc.scarecrow.lib.register.libinitializer;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.lang.annotation.*;

/**
 * This annotation is used to inject dependencies that are subscribed with {@link LibElement}.
 * The instance injected is always the same created at the beginning of Forge life cycle
 * using {@link FMLJavaModLoadingContext}
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface LibInject {
}
