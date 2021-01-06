package mc.scarecrow.common.capability;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.server.ServerWorld;

import java.util.Map;

public interface IScarecrowCapability<K, V, T extends Map<K, V>> {
    void add(K key, V value);

    void remove(K key);

    void remove(K key, V value);

    V get(K key);

    T getAll();

    void removeAll();

    ServerWorld getWorld();

    CompoundNBT write();

    void read(CompoundNBT compound);
}
