import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class GUITurn extends Alert {
    private PlayerList playerList;
    private Player player;
    private List<Integer> selectedIndices = new ArrayList<>();
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private List<RadioButton> buttons = new ArrayList<>(), skillButtons = new ArrayList<>();
    private ToggleGroup skillGroup = new ToggleGroup(), group = new ToggleGroup();

    public GUITurn(Player player, PlayerList playerList) { //Use the player's cards
        super(AlertType.NONE, "", ButtonType.OK, ButtonType.CANCEL);
        this.playerList = playerList;
        this.player = player;
        setTitle("Your Turn - " + player.getName() + " - Requested Card: "
                + new Card(player.getRequestedCard(), 1).getRankName());
        ScrollBar scrollBar = new ScrollBar();
        scrollBar.setOrientation(Orientation.HORIZONTAL);
        ScrollPane pane = new ScrollPane(scrollBar);
        pane.setPrefWidth(300);
        pane.setPrefHeight(150);
        VBox box = new VBox(new Text("Select the player to attack:"));
        HBox box2 = new HBox();
        for (Player p : playerList.getEnemyPlayers()) {
            RadioButton button = new RadioButton("" + p);
            button.setToggleGroup(group);
            buttons.add(button);
        }
        box.getChildren().addAll(buttons);

        for (Card card : player.getCards()) {
            try {
                CheckBox checkBox = new CheckBox();
                VBox vBox = new VBox(10, new ImageView(card.getImage()), checkBox);
                vBox.setAlignment(Pos.CENTER);
                box2.getChildren().add(vBox);
                checkBoxes.add(checkBox);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        pane.setContent(box2);
        getDialogPane().setContent(new VBox(10, box, pane));

        List<Player> enemies = playerList.getEnemyPlayers();
        List<Player> users = playerList.getUserPlayers();
        if (player.getClassType() == Player.ClassType.SUPPORT) {
            for (Player p : users) {
                int i = users.indexOf(p);
                skillButtons.add(new RadioButton(p.getName()));
                skillButtons.get(i).setToggleGroup(skillGroup);
            }
            skillButtons.add(new RadioButton("All"));
            skillButtons.get(users.size()).setToggleGroup(skillGroup);
            skillButtons.add(new RadioButton("None"));
            skillButtons.get(users.size() + 1).setToggleGroup(skillGroup);
            skillGroup.selectToggle(skillButtons.get(users.size() + 1));
        } else {
            for (Player p : enemies) {
                int i = enemies.indexOf(p);
                skillButtons.add(new RadioButton(p.getName()));
                skillButtons.get(i).setToggleGroup(skillGroup);
            }
            skillButtons.add(new RadioButton("All"));
            skillButtons.get(enemies.size()).setToggleGroup(skillGroup);
            skillButtons.add(new RadioButton("None"));
            skillButtons.get(enemies.size() + 1).setToggleGroup(skillGroup);
            skillGroup.selectToggle(skillButtons.get(enemies.size() + 1));
        }

        if (player.getFocusPlayer() != null) {
            for (int i = 0; i < enemies.size(); i++) {
                if (!skillButtons.get(i).getText().equals(player.getFocusPlayer().toString()))
                    skillButtons.get(i).setDisable(true);
                if (!buttons.get(i).getText().equals(player.getFocusPlayer().toString()))
                    buttons.get(i).setDisable(true);
            }
        }

        if (player.getCooldown() == 0) {
            if (player.getClassType() == Player.ClassType.TANK) {
                for (int i = 0; i < enemies.size(); i++) {
                    skillButtons.get(i).setDisable(true);
                }
            } else {
                skillButtons.get(enemies.size()).setDisable(true);
            }
        }  else if (player.getClassType() == Player.ClassType.SUPPORT) {
            for (int i = 0; i < users.size(); i++) {
                skillButtons.get(i).setDisable(true);
            }
        } else {
            for (int i = 0; i <= enemies.size(); i++) {
                skillButtons.get(i).setDisable(true);
            }
        }

        //Add Skills
        //Get selected buttons

        Text text = new Text("On which player(s) do you want to use your skill, " + player.getSkillName() + "?");
        box.getChildren().add(text);

        Text text2 = new Text("Nevermind... you cannot use your skill for " + player.getCooldown() + " turns");
        if (player.getCooldown() != 0) {
            box.getChildren().add(text2);
        }

        VBox vBox = new VBox();
        vBox.getChildren().addAll(skillButtons);
        box.getChildren().add(vBox);
    }

    public List<Integer> getSelectedIndices() {
        selectedIndices.clear();
        for (CheckBox box : checkBoxes) {
            if (box.isSelected()) selectedIndices.add(checkBoxes.indexOf(box));
        }
        return selectedIndices;
    }

    public Player getEnemy() throws Exception {
        RadioButton button = (RadioButton) group.getSelectedToggle();
        int index = buttons.indexOf(button);
        Player enemy = playerList.getEnemyPlayers().get(index);
        if (buttons.get(index).getText().equals(enemy.getName()))
            return enemy;
        throw new Exception("Player does not equal the button name!");
    }

    public void applySkill() {
        String playerName = ((RadioButton) skillGroup.getSelectedToggle()).getText();
        Player user = null;
        Player enemy = null;
        try {
            RadioButton button = (RadioButton) skillGroup.getSelectedToggle();
            String text = button.getText();
            for (Player p : playerList.getEnemyPlayers()) {
                if (p.getName().equals(text)) {
                    enemy = p;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (this.player.getClassType() == Player.ClassType.SUPPORT && !playerName.equals("None")) {
            for (Player p : playerList.getUserPlayers()) {
                if (p.getName().equals(playerName)) {
                    user = p;
                    break;
                }
            }
            try {
                player.useSkill(user);
                System.out.println(player + " used healing on " + user + ".");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (playerName.equals("All")) {
            System.out.println(player + " used skill on every enemy player.");
            player.useSkill(playerList.getEnemyPlayers());
        } else if (!playerName.equals("None")) {
            System.out.println(player + " used skill on " + enemy + ".");
            player.useSkill(enemy);
        }
    }
}
