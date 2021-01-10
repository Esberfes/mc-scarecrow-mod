package mc.scarecrow.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.lib.utils.LogUtils;
import mc.scarecrow.lib.utils.UIUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ScarecrowTileRenderer extends TileEntityRenderer<TileEntity> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DELTA_ANGLE = 5;
    private final Block block;

    public ScarecrowTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn, Block block) {
        super(rendererDispatcherIn);
        this.block = block;
    }

    @Override
    public void render(TileEntity tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                       int combinedLight, int combinedOverlay) {
        try {
            matrixStack.push();
            matrixStack.translate(0.5, 0.5, 0.5);

            if (!(tile instanceof ScarecrowTile))
                throw new Exception("Tile must be a instance of ScarecrowTile");

            PlayerEntity player = ScarecrowMod.PROXY.getPlayerEntity();
            PlayerEntity target = ((ScarecrowTile) tile).getClosestPlayer() != null ? player.world.getPlayerByUuid(((ScarecrowTile) tile).getClosestPlayer()) : null;
            BlockState tileState = player.world.getBlockState(tile.getPos());
            Direction tileFacingDirection = tileState.get(ScarecrowBlock.FACING);
            Vector3d tilePositionVec = new Vector3d(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());

            if (target != null && ForgeHooks.getBurnTime(target.getHeldItemMainhand()) > 0)
                ((ScarecrowTile) tile).setLastYaw(UIUtils.calcAngle(tileFacingDirection, UIUtils.lookAt(tilePositionVec, target), ((ScarecrowTile) tile).getLastYaw(), DELTA_ANGLE));
            else
                ((ScarecrowTile) tile).setLastYaw(UIUtils.calcAngle(tileFacingDirection, UIUtils.lookAt(tilePositionVec, tileFacingDirection.getDirectionVec()), ((ScarecrowTile) tile).getLastYaw(), DELTA_ANGLE));

            matrixStack.rotate(new Quaternion(0, (float) ((ScarecrowTile) tile).getLastYaw(), 0, true));

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
        } finally {
            matrixStack.translate(-0.5, -0.5, -0.5);
            for (RenderType type : RenderType.getBlockRenderTypes()) {
                if (RenderTypeLookup.canRenderInLayer(block.getDefaultState(), type) && tile.getWorld() != null) {
                    BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
                    IBakedModel model = blockRenderer.getModelForState(block.getDefaultState());
                    blockRenderer.getBlockModelRenderer().renderModel(tile.getWorld(), model, block.getDefaultState(),
                            tile.getPos(), matrixStack, buffer.getBuffer(type), false, new Random(), 0,
                            combinedOverlay, EmptyModelData.INSTANCE);
                }
            }
            matrixStack.pop();
        }
    }
}
