package mc.scarecrow.lib.core.libinitializer;

import mc.scarecrow.lib.core.LibCore;

public interface ILibInstanceHandler {

    default void handle(Object o) {
        LibCore.handleInstance(o);
    }

    static void fire(Object instance) {
        new ILibInstanceHandler() {
        }.handle(instance);
    }
}
