import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class GamePane extends GridPane {
    private Stage stage;
    private List<Card> discardPile = new ArrayList<>();
    private PlayerList playerList, substituteList = new PlayerList();
    private Button turnButton;
    private ImageView discardPileImage;
    private Button buttonBS;
    private Text turns;

    public static void main(String[] args) {
        PlayerList list = new PlayerList(Stream.generate(() -> new Player(500)).limit(6).toArray(Player[]::new));
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
        try {
            ExecutorService service = Executors.newSingleThreadExecutor();
            service.execute(new GameTask(list));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public GamePane(Stage stage, PlayerList playerList) {}

    private boolean isAlive(PlayerList playerList) {
        return (playerList.getUserPlayers().size() > 0 && playerList.getEnemyPlayers().size() > 0);
    }

    public Stage getStage() {
        return stage;
    }

    public PlayerList getSubstitutePlayers() {
        return substituteList;
    }

    public Button getTurnButton() {
        return turnButton;
    }

    public void enable(Button button) {
        button.setDisable(false);
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }

    public ImageView getDiscardPileImage() {
        return discardPileImage;
    }

    public Button getBSButton() {
        return buttonBS;
    }
}
