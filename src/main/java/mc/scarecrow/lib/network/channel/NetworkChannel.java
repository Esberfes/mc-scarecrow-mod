package mc.scarecrow.lib.network.channel;


import mc.scarecrow.lib.core.libinitializer.ILibElement;
import mc.scarecrow.lib.core.libinitializer.LibElement;
import mc.scarecrow.lib.network.executor.NetworkCommandExecutor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

@LibElement
public class NetworkChannel implements ILibElement {

    private SimpleChannel channel;

    @Override
    public void postConstruct(String modId, FMLJavaModLoadingContext loadingContext) {
        channel = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(modId, NetworkCommandExecutor.class.getSimpleName().toLowerCase())
                , () -> "1.0", s -> true, s -> true);
    }

    public SimpleChannel getChannel() {
        return channel;
    }
}
