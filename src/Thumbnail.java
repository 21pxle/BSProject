import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;

public class Thumbnail extends VBox {
    private String ordinal;
    private Text text = new Text();

    public Thumbnail(Player player) {
        setStyle("-fx-border-color: black;\n" +
                "-fx-border-insets: 5;\n" +
                "-fx-border-width: 3;\n" +
                "-fx-border-style: solid;\n");
        getChildren().add(text);
        Rectangle square = null;
        if (player.getImage() != null)
            getChildren().add(player.getImageView());
        else if (player.isUser()) {
            square = new Rectangle(100, 100, Color.BLUE);
        } else if (player.isEnemy()) {
            square = new Rectangle(100, 100, Color.RED);
        } else {
            square = new Rectangle(100, 100, Color.BLACK);
        }

        if (square != null) {
            getChildren().add(square);
        }
        Text text2 = new Text(player.toString());
        getChildren().add(text2);
    }

    public void setOrdinalNumber(int i) {
        int index = (i + 1) % 100;
        if (index % 10 == 1 && index != 11) {
            ordinal = (i + 1) + "st";
        } else if (index % 10 == 2 && index != 12) {
            ordinal = (i + 1) + "nd";
        } else if (index % 10 == 3 && index != 13) {
            ordinal = (i + 1) + "rd";
        } else {
            ordinal = (i + 1) + "th";
        }
        text.setText(ordinal);
    }

    public int getOrdinalNumber() {
        String s = ordinal;
        try {
            return Integer.parseInt(s.substring(0, s.length() - 2));
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
