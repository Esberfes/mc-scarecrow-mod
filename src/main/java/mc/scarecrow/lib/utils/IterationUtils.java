package mc.scarecrow.lib.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public abstract class IterationUtils {

    public static <T> void forEachIndexed(Collection<T> collection, BiConsumer<Integer, T> consumer, Predicate<T> predicate) {
        if (collection == null)
            return;

        LinkedList<T> copy = new LinkedList<>(collection);
        for (int i = 0; i < copy.size(); i++) {
            T t = copy.get(i);

            if (predicate.test(t))
                consumer.accept(i, t);
        }
    }

    public static <T> void forEachIndexed(Collection<T> collection, BiConsumer<Integer, T> consumer) {
        forEachIndexed(collection, consumer, t -> true);
    }
}
