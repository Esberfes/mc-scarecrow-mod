package mc.scarecrow.lib.core.libinitializer;

public class InjectionPoint {

    private Class<?> fromClass;

    public InjectionPoint(Class<?> fromClass) {
        this.fromClass = fromClass;
    }

    public Class<?> getFromClass() {
        return fromClass;
    }
}
