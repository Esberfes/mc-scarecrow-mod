package mc.scarecrow.lib.proxy;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public abstract class Proxy {
    static {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PROXY = new ClientProxy());
        DistExecutor.unsafeRunWhenOn(Dist.DEDICATED_SERVER, () -> () -> PROXY = new ServerProxy());
    }

    public static IProxy PROXY;

    public static boolean isServerLogic() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }
}
