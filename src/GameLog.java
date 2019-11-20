import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.DecimalFormat;
import java.util.Arrays;

public class GameLog extends Application {

    private ObservableList<Player> players;

    public GameLog(PlayerList list) {
        players = FXCollections.observableArrayList(list.getPlayers());
    }
    @Override
    public void start(Stage primaryStage) {

        //Convert into a number out of 1,000 to "normalize" the damage dealt.
        BigInteger rawTotal = BigInteger.ZERO;
        for (Player p : players) {
            rawTotal = rawTotal.add(p.getDamageDealt());
        }
        if (rawTotal.equals(BigInteger.ZERO)) {
            new Alert(Alert.AlertType.WARNING, "You can\'t access the log until after the first person attacks.");
            return;
        }
        TableView<Player> tableView = new TableView<>();
        tableView.setItems(players);


        TableColumn<Player, String> name = new TableColumn<>("Name");
        name.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Player, String> damageDealtPercentage = new TableColumn<>("Damage Dealt Percentage");

        TableColumn<Player, Integer> kills = new TableColumn<>("Kills");
        kills.setCellValueFactory(new PropertyValueFactory<>("kills"));

        TableColumn<Player, Integer> turnsSurvived = new TableColumn<>("Turns Survived");
        turnsSurvived.setCellValueFactory(new PropertyValueFactory<>("turnsSurvived"));

        TableColumn<Player, Integer> baloneySandwiches = new TableColumn<>("Baloney Sandwiches");
        baloneySandwiches.setCellValueFactory(new PropertyValueFactory<>("baloneySandwiches"));

        TableColumn<Player, String> healthPercentage = new TableColumn<>("Health Percentage");

        TableColumn<Player, BigInteger> damageDealt = new TableColumn<>("Damage Dealt");
        damageDealt.setCellValueFactory(new PropertyValueFactory<>("damageDealt"));

        TableColumn<Player, BigInteger> damageTaken = new TableColumn<>("Damage Taken");
        damageTaken.setCellValueFactory(new PropertyValueFactory<>("damageTaken"));


        final BigInteger total = new BigInteger(rawTotal.toByteArray());
        damageDealtPercentage.setCellValueFactory(data -> {
            Player p = data.getValue();
            return Bindings.createStringBinding(
                    () -> "" + new DecimalFormat("0.0").format(new BigDecimal(p.getDamageDealt())
                            .divide(new BigDecimal(total), new MathContext(8))
                            .multiply(new BigDecimal(100)).doubleValue()));
        });

        healthPercentage.setCellValueFactory(data -> {
            Player p = data.getValue();
            return Bindings.createStringBinding(() ->
                    new DecimalFormat("0.0").format(p.getHealthBar().getProgress() * 100));
        });

        tableView.getColumns().addAll(Arrays.asList(name, damageDealtPercentage, kills, baloneySandwiches, healthPercentage, turnsSurvived,  damageDealt, damageTaken));
        PieChart chart = new PieChart();

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList();
        for (Player p : players) {
            double x = new BigDecimal(p.getDamageDealt()).multiply(BigDecimal.valueOf(1000))
                    .divide(new BigDecimal(total), new MathContext(8)).intValue();
            data.add(new PieChart.Data(p.getName(), x));
        }


        chart.setData(data);
        final Label caption = new Label("");
        Group group = new Group(chart, caption);
        Tab tab = new Tab("Stats");
        tab.setContent(tableView);
        Tab tab2 = new Tab("Damage Dealt");
        tab2.setContent(group);

        TabPane pane = new TabPane(tab, tab2);
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pane.setPrefSize(600, 600);

        for (PieChart.Data d : chart.getData()) {
            d.getNode().addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
                caption.setTranslateX(event.getSceneX());
                caption.setTranslateY(event.getSceneY() - 30);
                caption.setText(new DecimalFormat("0.0%").format(d.getPieValue() / 1000));
            });
        }
        primaryStage.setTitle("Game Log");
        primaryStage.setScene(new Scene(pane));
        primaryStage.show();
    }

}
