package mc.scarecrow.exception;

public class NotEntityOfTypeException extends ScarecrowException {

    public NotEntityOfTypeException(Class<?> type) {
        super.onError("No entity found with type: " + type.getSimpleName());
    }
}
