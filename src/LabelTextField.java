import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class LabelTextField extends HBox {
    private TextField field;
    public LabelTextField(String s) {
        field = new TextField();
        getChildren().addAll(new Text(s), new RigidBox(20, 20).get(), field);
    }

    public String getText() {
        return field.getText();
    }
}
