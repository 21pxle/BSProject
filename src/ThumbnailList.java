import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThumbnailList extends VBox {
    private PlayerList playerList;
    private int accumulatedTurns = 1;

    public ThumbnailList(PlayerList playerList) {
        setSpacing(5);
        this.playerList = playerList;
    }

    public void setImageList() {
        HashMap<Integer, Player> turnOrder = playerList.getTurnOrder();
        List<Player> list = new ArrayList<>(turnOrder.values());
        List<Player> subList = list.subList(1, 4);
        for (Player p : subList) {

            //Use thumbnails
            Thumbnail thumbnail = new Thumbnail(p);
            thumbnail.setOrdinalNumber(subList.indexOf(p) + 1);
            getChildren().add(thumbnail);
        }

    }

    public void reset(PlayerList playerList) {
        getChildren().clear();
        setPlayerList(playerList);
    }

    public void transition() {
        HashMap<Integer, Player> turnOrder = playerList.getTurnOrder();
        int turns = accumulatedTurns++;

        List<Player> list = new ArrayList<>(turnOrder.values());
        if (playerList.getTurns() > 3) {
            Timeline line1 = new Timeline();
            Thumbnail t = new Thumbnail(list.get(3));
            t.setOrdinalNumber(4); /* To make sure that the thumbnail has the correct ordinal,
        because it decreases right after it is placed. */
            getChildren().add(t);
            for (Node node : getChildren()) {
                //Account for the dead players.
                if (node instanceof Thumbnail) {
                    Thumbnail thumbnail = (Thumbnail) node;
                    line1.getKeyFrames().add(new KeyFrame(Duration.seconds(2),
                            new KeyValue(thumbnail.translateYProperty(), -(t.getBoundsInLocal().getHeight() + 41) * turns)));
                }
            }
            line1.play();
        }
        Timeline line2 = new Timeline();
        for (Node node : getChildren()) {
            if (node instanceof Thumbnail) {
                Thumbnail thumbnail = (Thumbnail) node;
                if (thumbnail.getOrdinalNumber() != 1) {
                    thumbnail.setOrdinalNumber(thumbnail.getOrdinalNumber() - 2);
                }
                else {
                    line2.getKeyFrames().add(new KeyFrame(Duration.seconds(2),
                            new KeyValue(thumbnail.visibleProperty(), false)));
                }
            }
        }
        line2.play();
    }

    public void setPlayerList(PlayerList playerList) {
        accumulatedTurns = 1;
        getChildren().removeIf(n -> n instanceof Thumbnail);
        this.playerList = playerList;
        HashMap<Integer, Player> turnOrder = playerList.getTurnOrder();
        List<Player> list = new ArrayList<>(turnOrder.values());
        List<Player> subList = list.size() <= 3 ? list.subList(1, list.size()) : list.subList(1, 4);
        for (Player p : subList) {

            //Use thumbnails
            Thumbnail thumbnail = new Thumbnail(p);
            thumbnail.setOrdinalNumber(subList.indexOf(p) + 1);
            getChildren().add(thumbnail);
        }
    }
}