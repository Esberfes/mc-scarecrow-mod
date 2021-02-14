package mc.scarecrow.lib.producer;

import com.google.gson.*;
import mc.scarecrow.lib.core.libinitializer.InjectionPoint;
import mc.scarecrow.lib.core.libinitializer.LibProducer;

import java.lang.reflect.Type;

public class GsonProducer {

    @LibProducer
    public Gson produceGson(InjectionPoint injectionPoint) {
        return new GsonBuilder().registerTypeAdapter(Double.class, new DoubleSerializer()).setPrettyPrinting().create();
    }

    private static class DoubleSerializer implements JsonSerializer<Double> {
        @Override
        public JsonElement serialize(Double src, Type typeOfSrc, JsonSerializationContext context) {
            return src == src.longValue() ? new JsonPrimitive(src.longValue()) : new JsonPrimitive(src);
        }
    }
}
