import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.layout.Pane;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class UserInterfaceMenu extends Alert {

    public static final ButtonType SEE_CARDS = new ButtonType("See cards...");

    public static void main(String[] args) throws FileNotFoundException {
        Player p = new Player(1);
        p.setType(Player.Type.ENEMY);
        p.setName("John Doe");
        p.setClassType(Player.ClassType.TANK);
        p.setImage(new FileInputStream("res/2 of Clubs.jpg"));
        new Thread(() -> {
            try {
                new UserInterfaceMenu(p).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public UserInterfaceMenu(Player player) {
        super(AlertType.NONE,"", ButtonType.OK);
        if (player.isUser()) {
            getButtonTypes().add(SEE_CARDS);
        }

        setHeaderText("Here are the properties of " + player.toString());

        Pane root = new Pane();
        ListView<String> view = new ListView<>();
        ObservableList<String> list = FXCollections.observableArrayList(
                String.format("Health: %s / %s (%.1f%%)", player.getHealthString(),
                        player.getMaxHealthString(), player.getHealthBar().getProgress() * 100),
                "Attack: " + player.getAttackString(),
                "Defense: " + player.getDefenseString(),
                "Class: " + player.getClassType().toString(),
                String.format("Turn Percentage: %.1f%%", Math.min(player.getProgress() / 10d, 100d)),
                "Speed: " + player.getSpeed(),
                "Number of Cards: " + player.getCards().size(),
                "Skill Cooldown: " + player.getCooldown(),
                "Status Effects: None"
        );
        for (StatusEffect effect : player.getStatusEffects()) {
            list.set(8, "Status Effects:");
            list.add(effect.getName() + ": " + effect.getDuration() + " turn(s)");
        }
        view.setItems(list);

        root.getChildren().add(view);
        getDialogPane().setContent(root);
        setTitle("Properties of " + player.getName());
    }
}
