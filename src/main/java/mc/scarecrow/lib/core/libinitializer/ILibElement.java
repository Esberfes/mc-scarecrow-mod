package mc.scarecrow.lib.core.libinitializer;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public interface ILibElement {
    /**
     * Called after all injections has been resolved, this make easy ensure that a injection is there when this method
     * is invoked
     *
     * @param modId
     * @param loadingContext
     */
    default void postConstruct(String modId, FMLJavaModLoadingContext loadingContext) {
    }

    ;
}
