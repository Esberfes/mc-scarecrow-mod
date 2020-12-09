package mc.scarecrow.mod.client;


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientHandler {

    public static void setup() {
        // RenderingRegistry.registerEntityRenderingHandler(ModEntities.GOBLIN_TRADER, GoblinTraderRenderer::new);
        // RenderingRegistry.registerEntityRenderingHandler(ModEntities.VEIN_GOBLIN_TRADER, GoblinTraderRenderer::new);
    }
}