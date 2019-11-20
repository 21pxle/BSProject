import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.util.Optional;

public class UserInterface extends VBox {
    private final Player player;
    private Image image;
    private ProgressBar bar;
    private Rectangle rectangle;

    private Text text;
    private Button button;
    public static void main(String[] args) {
        new JFXPanel();
        Application application = new Application() {
            @Override
            public void start(Stage primaryStage) throws Exception {
                Player p = new Player(111);
                Deck deck = new Deck();
                p.addCards(deck.draw(8));
                p.setType(Player.Type.USER);
                p.setName("John Doe");
                p.setImage(new FileInputStream("res/2 of Clubs.jpg"));
                p.setClassType(Player.ClassType.ARCHER);
                Pane root = new UserInterface(p);
                Scene scene = new Scene(root, 200, 200);
                primaryStage.setScene(scene);
                primaryStage.show();
            }
        };
        new Thread(() -> Platform.runLater(() -> {
            try {
                application.start(new Stage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        })).run();
    }

    public UserInterface(Player player) {
        this.player = player;
        setAlignment(Pos.CENTER);
        button = new Button("See more properties...");
        button.setOnMouseClicked(event ->
                new Thread(() -> Platform.runLater(() -> {
                    try {
                        Optional<ButtonType> type = new UserInterfaceMenu(player).showAndWait();
                        if (type.isPresent() && type.get() == UserInterfaceMenu.SEE_CARDS) {
                            ScrollBar scrollBar = new ScrollBar();
                            ScrollPane pane = new ScrollPane(scrollBar);
                            pane.setPrefWidth(300);
                            pane.setPrefHeight(150);
                            HBox box = new HBox();
                            for (Card card : player.getCards())
                                box.getChildren().add(new ImageView(card.getImage()));
                            pane.setContent(box);
                            Alert alert = new Alert(Alert.AlertType.NONE,
                                    "These are the cards that " + player + " has:", ButtonType.OK);
                            alert.setTitle("These are the cards that " + player + " has:");
                            alert.getDialogPane().setContent(pane);
                            alert.showAndWait();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })).start());

        if (player.isEnemy()) {
            text = new Text(player.getName() + (player.isDead() ? " (Dead)" : ""));
            text.setStroke(Color.RED);
            getChildren().addAll(button, text);

            image = player.getImage();
            if (image != null) {
                ImageView view = new ImageView(image);
                getChildren().add(view);
            } else {
                rectangle = new Rectangle(100, 100);
                if (player.isUser()) {
                    rectangle.setFill(Color.BLUE);
                }
                else if (player.isEnemy()) {
                    rectangle.setFill(Color.RED);
                }
                else {
                    rectangle.setFill(Color.BLACK);
                }
                getChildren().add(rectangle);
            }

            bar = player.getHealthBar();
            bar.setStyle("-fx-accent: red;");
            getChildren().add(bar);
        } else if (player.isUser()) {
            bar = player.getHealthBar();
            bar.setStyle("-fx-accent: blue;");
            getChildren().add(bar);

            image = player.getImage();
            if (image != null) {
                ImageView view = new ImageView(image);
                getChildren().add(view);
            } else {
                rectangle = new Rectangle(100, 100);
                if (player.isUser()) {
                    rectangle.setFill(Color.BLUE);
                }
                else if (player.isEnemy()) {
                    rectangle.setFill(Color.RED);
                }
                else {
                    rectangle.setFill(Color.BLACK);
                }
                getChildren().add(rectangle);
            }

            text = new Text(player.getName() + (player.isDead() ? " (Dead)" : ""));
            text.setStroke(Color.BLUE);
            getChildren().addAll(text, button);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UserInterface) {
            return ((UserInterface) obj).getPlayer().equals(player);
        }
        return false;
    }

    public Player getPlayer() {
        return player;
    }

    public double getX() {
        return getBoundsInLocal().getMinX();
    }

    public double getY() {
        return getBoundsInLocal().getMinY();
    }

    public void update() {
        if (player.isDead()) {
            setVisible(false);
        }
        setAlignment(Pos.CENTER);
        button = new Button("See more properties...");
        button.setOnMouseClicked(event ->
                new Thread(() -> Platform.runLater(() -> {
                    try {
                        Optional<ButtonType> type = new UserInterfaceMenu(player).showAndWait();
                        if (type.isPresent() && type.get() == UserInterfaceMenu.SEE_CARDS) {
                            ScrollBar scrollBar = new ScrollBar();
                            ScrollPane pane = new ScrollPane(scrollBar);
                            pane.setPrefWidth(300);
                            pane.setPrefHeight(150);
                            HBox box = new HBox();
                            for (Card card : player.getCards())
                                box.getChildren().add(new ImageView(card.getImage()));
                            pane.setContent(box);
                            Alert alert = new Alert(Alert.AlertType.NONE,
                                    "These are the cards that " + player + " has:", ButtonType.OK);
                            alert.setTitle("These are the cards that " + player + " has:");
                            alert.getDialogPane().setContent(pane);
                            alert.showAndWait();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                })).start());
        Timeline timeline = new Timeline();
        if (player.isEnemy()) {
            text = new Text(player.getName() + (player.isDead() ? " (Dead)" : "") +
                    (player.isStunned() && !player.isDead() ? " (Stunned)" : ""));
            text.setStroke(Color.RED);

            image = player.getImage();
            if (image == null) {
                Rectangle rectangle = new Rectangle(100, 100);
                if (player.isUser()) {
                    rectangle.setFill(Color.BLUE);
                }
                else if (player.isEnemy()) {
                    rectangle.setFill(Color.RED);
                }
                else {
                    rectangle.setFill(Color.BLACK);
                }
            }
            bar.setStyle("-fx-accent: red;");
        } else if (player.isUser()) {


            bar.setStyle("-fx-accent: blue;");

            image = player.getImage();
            if (image == null) {
                rectangle = new Rectangle(100, 100);
                if (player.isUser()) {
                    rectangle.setFill(Color.BLUE);
                }
                else if (player.isEnemy()) {
                    rectangle.setFill(Color.RED);
                }
                else {
                    rectangle.setFill(Color.BLACK);
                }
            }

            text = new Text(player.getName() + (player.isDead() ? " (Dead)" : "") +
                    (player.isStunned() && !player.isDead() ? " (Stunned)" : ""));
            text.setStroke(Color.BLUE);
        }
        timeline.play();
    }

    public ProgressBar getHPBar() {
        return bar;
    }
}
