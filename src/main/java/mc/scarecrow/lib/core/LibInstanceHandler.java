package mc.scarecrow.lib.core;

import mc.scarecrow.lib.core.libinitializer.ILibInstanceHandler;

public abstract class LibInstanceHandler {
    {
        ILibInstanceHandler.fire(this);
    }
}
