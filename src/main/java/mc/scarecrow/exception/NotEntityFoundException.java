package mc.scarecrow.exception;

import net.minecraft.util.math.BlockPos;

public class NotEntityFoundException extends ScarecrowException {

    public NotEntityFoundException(Class<?> type, BlockPos pos) {
        super.onError("No entity found with type:" + type.getSimpleName() + " in position: " + pos.getCoordinatesAsString());
    }
}
