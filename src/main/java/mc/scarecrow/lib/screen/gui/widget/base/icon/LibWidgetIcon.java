package mc.scarecrow.lib.screen.gui.widget.base.icon;

import mc.scarecrow.lib.math.LibVectorBox;
import mc.scarecrow.lib.screen.gui.render.vertex.LibVertex;
import mc.scarecrow.lib.screen.gui.render.vertex.VertexDrawerBuilder;

public class LibWidgetIcon {

    private VertexDrawerBuilder vertexDrawerBuilder;

    private LibWidgetIcon(VertexDrawerBuilder vertexDrawerBuilder) {
        this.vertexDrawerBuilder = vertexDrawerBuilder;
    }

    public void render() {
        this.vertexDrawerBuilder.draw();
    }

    public static class Builder {

        private VertexDrawerBuilder vertexDrawerBuilder;
        private LibVectorBox parentDimension;

        public Builder(LibVectorBox parentDimension, int z) {
            this.parentDimension = parentDimension;
            this.vertexDrawerBuilder = VertexDrawerBuilder.builder(z);
        }

        public Builder vertex(LibVertex vertex) {
            vertexDrawerBuilder.vertex(vertex);

            return this;
        }

        public LibWidgetIcon build() {
            return new LibWidgetIcon(this.vertexDrawerBuilder);
        }
    }
}
