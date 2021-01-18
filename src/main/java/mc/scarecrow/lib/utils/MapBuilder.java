package mc.scarecrow.lib.utils;

import mc.scarecrow.lib.core.libinitializer.ILibElement;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapBuilder<K, V> implements ILibElement {

    Map<K, V> result;

    public MapBuilder() {
        result = new LinkedHashMap<>();
    }

    public MapBuilder<K, V> entry(K key, V value) {
        result.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return result;
    }
}
