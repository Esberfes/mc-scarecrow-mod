package mc.scarecrow.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleRenderType;
import net.minecraft.client.particle.SpriteTexturedParticle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;

public class ScarecrowParticle extends SpriteTexturedParticle {

    protected ScarecrowParticle(World world, double x, double y, double z, double vx, double vy, double vz, IAnimatedSprite spriteSet) {
        super((ClientWorld) world, x, y, z);
        this.particleGravity = 1.2F;
        this.particleScale /= 3.0F;
        this.maxAge = 7;
        this.canCollide = false;
        this.motionX = vx * 1;
        this.motionY = vy * 1;
        this.motionZ = vz * 1;

        this.selectSpriteRandomly(spriteSet);
    }

    @Override
    public IParticleRenderType getRenderType() {
        return IParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
    }
}