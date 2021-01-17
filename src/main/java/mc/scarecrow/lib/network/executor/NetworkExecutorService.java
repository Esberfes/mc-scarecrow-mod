package mc.scarecrow.lib.network.executor;

import mc.scarecrow.lib.network.channel.NetworkChannel;
import mc.scarecrow.lib.register.libinitializer.ILibElement;
import mc.scarecrow.lib.register.libinitializer.LibElement;
import mc.scarecrow.lib.register.libinitializer.LibInject;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

@LibElement(after = {NetworkChannel.class})
public class NetworkExecutorService extends ThreadPoolExecutor implements ILibElement {

    @LibInject
    private NetworkChannel networkChannel;

    public NetworkExecutorService() {
        super(10, 10, 0, NANOSECONDS, new LinkedBlockingQueue<>());
    }

    public void executeCommand(NetworkCommand command) {
        this.execute(() -> networkChannel.getChannel().sendToServer(command));
    }
}

