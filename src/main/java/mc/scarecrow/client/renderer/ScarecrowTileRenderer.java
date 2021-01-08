package mc.scarecrow.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import mc.scarecrow.ScarecrowMod;
import mc.scarecrow.common.block.ScarecrowBlock;
import mc.scarecrow.common.block.tile.ScarecrowTile;
import mc.scarecrow.utils.LogUtils;
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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class ScarecrowTileRenderer extends TileEntityRenderer<ScarecrowTile> {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final int DELTA_ANGLE = 5;
    private final Block block;

    public ScarecrowTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn, Block block) {
        super(rendererDispatcherIn);
        this.block = block;
    }

    @Override
    public void render(ScarecrowTile tile, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer buffer,
                       int combinedLight, int combinedOverlay) {
        try {
            PlayerEntity player = ScarecrowMod.PROXY.getPlayerEntity();
            PlayerEntity target = player.world.getClosestPlayer((double) tile.getPos().getX() + 0.5D,
                    (double) tile.getPos().getY() + 0.5D,
                    (double) tile.getPos().getZ() + 0.5D,
                    20.0D, false);

            matrixStack.push();
            matrixStack.translate(0.5, 0.5, 0.5);

            BlockState tileState = player.world.getBlockState(tile.getPos());
            Direction tileFacingDirection = tileState.get(ScarecrowBlock.FACING);

            if (target != null && ForgeHooks.getBurnTime(target.getHeldItemMainhand()) > 0) {
                Vector3d tilePositionVec = new Vector3d(tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ());

                double targetYaw = lookAt(tilePositionVec, target);

                tile.setLastYaw(calcAngle(tileFacingDirection, targetYaw, tile.getLastYaw(), DELTA_ANGLE));

            } else {
                tile.setLastYaw(calcAngle(tileFacingDirection, tileFacingDirection.getHorizontalAngle(), tile.getLastYaw(), DELTA_ANGLE));
            }

            matrixStack.rotate(new Quaternion(0, (float) tile.getLastYaw(), 0, true));

        } catch (Throwable e) {
            LogUtils.printError(LOGGER, e);
            matrixStack.clear();
        } finally {
            matrixStack.translate(-0.5, -0.5, -0.5);
            for (RenderType type : RenderType.getBlockRenderTypes()) {
                if (RenderTypeLookup.canRenderInLayer(block.getDefaultState(), type)) {
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

    public double calcAngle(Direction direction, double target, double lastYaw, int delta) {
        double dirAngle;
        switch (direction) {
            case NORTH:
            case EAST:
                dirAngle = 180D;
                break;
            case SOUTH:
            case WEST:
                dirAngle = -180D;
                break;
            default:
                dirAngle = 0D;
        }

        double goalTarget = ((target + dirAngle) * -1);

        while (lastYaw - goalTarget < -180.0F)
            goalTarget -= 360.0F;

        while (lastYaw - goalTarget >= 180.0F)
            goalTarget += 360.0F;

        double targetAngle = goalTarget < 0D ? Math.max(goalTarget, -delta) : Math.min(goalTarget, delta);

        return targetAngle < 0D ? Math.max(targetAngle + lastYaw, goalTarget) : Math.min(targetAngle + lastYaw, goalTarget);
    }

    public double lookAt(Vector3d vector3d, Entity entity) {
        Vector3d target = entity.getPositionVec();
        double d0 = target.x - vector3d.x;
        double d2 = target.z - vector3d.z;

        return (MathHelper.wrapDegrees((MathHelper.atan2(d2, d0) * (180F / Math.PI)) - 90.0F));
    }
}
