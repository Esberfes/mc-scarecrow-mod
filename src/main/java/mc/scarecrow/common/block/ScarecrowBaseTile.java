package mc.scarecrow.common.block;

import mc.scarecrow.common.init.RegistryHandler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicInteger;

public abstract class ScarecrowBaseTile extends TileEntity implements ITickableTileEntity {

    private AtomicInteger counter;

    public ScarecrowBaseTile() {
        super(RegistryHandler.scarecrowTileBlock.get());
        counter = new AtomicInteger();
    }

    @Override
    public void tick() {
        // limitar update
        int ticks = counter.incrementAndGet();
        if (ticks % 100 == 0) {
            if (!isClient())
                ((ServerWorld) world).getServer().deferTask(execute());
            else
                execute(ticks);
            counter.set(0);
        }
    }

    protected abstract boolean isClient();

    protected abstract void execute(int ticks);

    protected abstract Runnable execute();
}
