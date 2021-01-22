package mc.scarecrow.lib.producer;

import mc.scarecrow.lib.core.libinitializer.InjectionPoint;
import mc.scarecrow.lib.core.libinitializer.LibProducer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerProducer {

    @LibProducer
    public Logger produceLogger(InjectionPoint injectionPoint) {
        return LogManager.getLogger(injectionPoint.getFromClass());
    }
}
