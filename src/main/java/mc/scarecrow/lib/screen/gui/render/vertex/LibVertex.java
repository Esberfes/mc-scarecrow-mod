package mc.scarecrow.lib.screen.gui.render.vertex;

import mc.scarecrow.lib.math.LibRGBA;
import mc.scarecrow.lib.math.LibVector3D;

public class LibVertex {
    private LibVector3D vector3D;
    private LibRGBA color;

    public LibVertex(float x, float y, float z, float red, float green, float blue, float alpha) {
        this.vector3D = new LibVector3D(x, y, z);
        this.color = new LibRGBA(red, green, blue, alpha);
    }

    public LibVertex(float x, float y, float z, LibRGBA color) {
        this.vector3D = new LibVector3D(x, y, z);
        this.color = color;
    }

    public LibVertex(LibVector3D vector3D, LibRGBA color) {
        this.vector3D = vector3D;
        this.color = color;
    }

    public LibVector3D getVector3D() {
        return vector3D;
    }

    public LibRGBA getColor() {
        return color;
    }
}
