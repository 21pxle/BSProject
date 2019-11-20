import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class AttackMenu extends Alert {
    private Player player;
    private PlayerList playerList;
    private ToggleGroup group;
    private Player enemy = null;
    public static void main(String[] args) {
        PlayerList list = new PlayerList(Stream.generate(Player::new).limit(6).toArray(Player[]::new));
        list.forEach(p -> {
            int index = list.indexOf(p);
            p.setName("Player " + (index + 1));
            switch (index % 3) {
                case 0: p.setClassType(Player.ClassType.ARCHER); break;
                case 1: p.setClassType(Player.ClassType.WARRIOR); break;
                case 2: p.setClassType(Player.ClassType.SUPPORT); break;
            }
            if (index < 3) p.setType(Player.Type.ENEMY);
            else p.setType(Player.Type.USER);
        });
        new Thread(() -> {
            new JFXPanel();
            Platform.runLater(() -> {
                Player player = list.getUserPlayers().get(1);
                AttackMenu menu = new AttackMenu(list, player);
                Optional<ButtonType> type = menu.showAndWait();
                if (type.isPresent()) {
                    if (type.get() == ButtonType.OK) {
                        menu.attackPlayer();
                    } else {
                        System.exit(0);
                    }
                }
            });
        }).run();
    }

    public AttackMenu(PlayerList list, Player player) {
        super(AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL);
        setTitle("Who will you attack, " + player + "?");
        playerList = list;
        this.player = player;
        Pane root = new Pane();
        getDialogPane().setContent(root);
        List<Player> enemies = list.getEnemyPlayers();
        List<Player> users = list.getUserPlayers();
        RadioButton[] buttons = new RadioButton[enemies.size()];
        group = new ToggleGroup();

        for (int i = 0; i < enemies.size(); i++) {
            buttons[i] = new RadioButton(enemies.get(i).getName());
            buttons[i].setToggleGroup(group);
        }
        HBox box = new HBox(10);
        box.getChildren().addAll(buttons);
        root.getChildren().add(box);
    }

    public boolean attackPlayer() {
        RadioButton button = (RadioButton) group.getSelectedToggle();
        if (button == null) {
            new Alert(AlertType.WARNING, "Please select a player to attack.").showAndWait();
            return false;
        }
        String name = button.getText();

        for (Player p : playerList.getEnemyPlayers()) {
            if (p.getName().equals(name)) {
                enemy = p;
                break;
            }
        }
        try {
            player.setCardsPut(1);
            player.attack(enemy);
            player.setCardsPut(0);
            System.out.println(new DecimalFormat("0.0%").format(enemy.getHealthBar().getProgress()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public Player getPlayer() {
        if (player.getClassType() != Player.ClassType.SUPPORT)
            return enemy;
        return player;
    }
}
