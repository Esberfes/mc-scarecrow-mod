package mc.scarecrow.lib.proxy;

import mc.scarecrow.client.renderer.ScarecrowTileRenderer;
import mc.scarecrow.client.screen.ScarecrowScreen;
import mc.scarecrow.lib.register.LibAutoRegister;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.registry.ClientRegistry;

public class ClientProxy implements IProxy {

    @Override
    public void init() {
        // Register on client the screen for the container
        ScreenManager.registerFactory(LibAutoRegister.CONTAINERS.get("scarecrow"),
                (ScreenManager.IScreenFactory<Container, ContainerScreen<Container>>) ScarecrowScreen::new);

        // Register on client the scarecrow tile renderer
        ClientRegistry.bindTileEntityRenderer((TileEntityType<? extends TileEntity>) LibAutoRegister.TILE_ENTITIES.get("scarecrow"),
                o -> new ScarecrowTileRenderer(o, LibAutoRegister.BLOCKS.get("scarecrow")));
    }

    @Override
    public Minecraft getClient() {
        return Minecraft.getInstance();
    }

    @Override
    public World getPlayerWorld() {
        return Minecraft.getInstance().world;
    }

    @Override
    public ClientPlayerEntity getPlayerEntity() {
        return Minecraft.getInstance().player;
    }
}