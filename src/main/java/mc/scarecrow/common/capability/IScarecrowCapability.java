package mc.scarecrow.common.capability;

import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;

import java.util.Map;

public interface IScarecrowCapability<C, K, V, T extends Map<K, V>> extends Capability.IStorage<C> {
    void add(K key, V value);

    void remove(K key);

    void remove(K key, V value);

    V get(K key);

    T getAll();

    void removeAll();

    ServerWorld getWorld();
}
