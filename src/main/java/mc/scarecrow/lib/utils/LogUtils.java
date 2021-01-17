package mc.scarecrow.lib.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;

public abstract class LogUtils {
    private static Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();

    public static void debug(Logger logger, Object e) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        logger.info(ste[2].getMethodName() + " (" + ste[2].getLineNumber() + ")" + ": " + (e != null ? gson.toJson(e) : ""));

    }

    public static void printError(Logger logger, Throwable e) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        logger.error(ste[2].getMethodName() + " (" + ste[2].getLineNumber() + ")" + ": " + e.getMessage());
        if (e instanceof NullPointerException)
            logger.error(ExceptionUtils.getStackTrace(e));
    }
}
