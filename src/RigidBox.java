import javafx.scene.layout.HBox;

public class RigidBox {
    private double width, height;

    public RigidBox(double width, double height) {
        this.width = width;
        this.height = height;
    }

    public HBox get() {
        HBox box = new HBox();
        box.setMinSize(width, height);
        return box;
    }
}
