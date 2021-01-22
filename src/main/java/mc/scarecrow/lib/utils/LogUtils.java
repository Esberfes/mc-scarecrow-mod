package mc.scarecrow.lib.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;

public abstract class LogUtils {

    public static void printError(Logger logger, Throwable e) {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        logger.error(ste[2].getMethodName() + " (" + ste[2].getLineNumber() + ")" + ": " + e.getMessage());
        if (e instanceof NullPointerException)
            logger.error(ExceptionUtils.getStackTrace(e));
    }
}
