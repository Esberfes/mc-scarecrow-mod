package mc.scarecrow.lib.proxy;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.thread.SidedThreadGroups;

public abstract class Proxy {

    public static final IProxy PROXY = DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static boolean isServerLogic() {
        return Thread.currentThread().getThreadGroup() == SidedThreadGroups.SERVER;
    }
}
