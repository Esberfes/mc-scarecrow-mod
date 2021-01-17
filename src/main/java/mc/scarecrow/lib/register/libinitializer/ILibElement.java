package mc.scarecrow.lib.register.libinitializer;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public interface ILibElement {
    default void initialize(String modId, OnRegisterManuallyListener onRegisterManuallyListener, FMLJavaModLoadingContext loadingContext) {
    }

    ;
}
