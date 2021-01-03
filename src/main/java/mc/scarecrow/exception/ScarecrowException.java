package mc.scarecrow.exception;

import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.common.network.ClientProxy;
import net.minecraft.util.text.StringTextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Method;
import java.util.function.Consumer;

public abstract class ScarecrowException extends Exception {

    private static final Logger LOGGER = LogManager.getLogger();

    private String message;

    public ScarecrowException() {

    }

    protected Consumer<String> onError(String message) {
        return new Consumer<String>() {
            @Override
            public void accept(String s) {
                Method enclosingMethod = this.getClass().getEnclosingMethod();
                if (ScarecrowMod.IS_DEV_MODE) {
                    LOGGER.error(message);
                    if (ScarecrowMod.PROXY instanceof ClientProxy)
                        ScarecrowMod.PROXY.getPlayerEntity().sendStatusMessage(new StringTextComponent(message), true);
                }
            }
        };
    }

    @Override
    public String getMessage() {
        return message;
    }
}
