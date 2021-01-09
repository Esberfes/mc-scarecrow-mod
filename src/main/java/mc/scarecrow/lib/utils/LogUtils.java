package mc.scarecrow.lib.utils;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;

public abstract class LogUtils {

    public static String methodName() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[2].getMethodName();
    }

    public static String methodNameAndLine() {
        final StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        return ste[2].getMethodName() + " (" + ste[2].getLineNumber() + ")";
    }

    public static void printNullPointerStackTrace(Logger logger, Throwable t) {
        if (t instanceof NullPointerException) {
            logger.error(ExceptionUtils.getStackTrace(t));
        }
    }

    public static void printError(Logger logger, Throwable e) {
        logger.error(LogUtils.methodNameAndLine() + ": " + e.getMessage());
        printNullPointerStackTrace(logger, e);
    }
}
