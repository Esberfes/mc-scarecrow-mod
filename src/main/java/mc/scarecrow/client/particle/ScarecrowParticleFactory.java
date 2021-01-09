package mc.scarecrow.client.particle;

import net.minecraft.client.particle.IAnimatedSprite;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particles.BasicParticleType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScarecrowParticleFactory implements IParticleFactory<BasicParticleType> {

    private final IAnimatedSprite spriteSet;

    public ScarecrowParticleFactory(IAnimatedSprite spriteSet) {
        this.spriteSet = spriteSet;
    }

    @Override
    public Particle makeParticle(BasicParticleType typeIn, ClientWorld worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        return new ScarecrowParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, spriteSet);
    }
}