package mc.scarecrow.lib.block;


import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;
import net.minecraft.block.Block;

public abstract class LibBaseBlock extends Block {
    {
        ILibInstanceHandler.fire(this);
    }

    public LibBaseBlock(Properties properties) {
        super(properties);
    }
}
