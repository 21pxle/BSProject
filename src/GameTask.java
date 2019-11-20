import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//Be forewarned... This code may trigger some exceptions...
public class GameTask extends Task<Void> {
    private GameLog log;
    private PlayerList playerList;
    private Button buttonBS = new Button("Call Baloney Sandwich?"), turnButton = new Button("Your Turn");
    private List<Card> discardPile = new ArrayList<>();
    private Text discardPileSize = new Text("Discard Pile: 0"), turns = new Text();
    private Group root;
    private ThumbnailList thumbnailList;
    private ImageView discardPileImage = new ImageView();
    private int numDead = 0;
    private boolean warning = false; //So that the player is warned for 3 cards or 15 turns left.
    private Button logButton = new Button("Game Log (WIP)");
    private Player startPlayer;
    // private ToggleButton autoButton;

    /*
    1.  Start Player has an Ace of Spades.
	2.  Play goes in order of the speed of the player (2’s, 3’s, etc.) until someone calls BS or someone dies, whichever comes first. If the number of turns exceeds 300, then proceed to step 5.
	2a. A “turn” is when you put down at least one card but less than 4 cards of the requested rank, which will increase sequentially.
	2b. The cards will remain hidden until someone calls BS.
	2c. If some person calls BS, then
	3.  If a player dies, the play will restart from the Ace of Spades.
	4.  Any player can use their skills, but each one has a cooldown.
	5.  If a player reaches 0 cards left, then he/she will deal damage equal to 50% of the enemy team's current health.
	6.  This process will continue until the team that completely eliminates the other team wins.
	7.  The ranking is as follows:
	    Level: The leveling index l will be calculated and then rounded to the nearest integer.
	        For example, a team that has 3 Level-1 players will have an index 2 because the geometric mean of the indices equals 1 and the arithmetic mean is 1.
	A team with a Level 8 player, a Level 15 player, and a Level 20 player will have an index of 28 because even though the geometric mean of the indices rounds to 13 and the arithmetic mean rounds to 14 (or the sum of the two is only 27), the result gets rounded up.
	Health: The health index h will be calculated with the average of the percentages, rounded to the nearest integer.
	For example, a team with 28% health will have an index of 0.53 (10√p), and a team with 50% health will have an index of 0.71.
	Number of Players Lost: If the number of players lost is less than that of the other team, the number of players goes up by the difference, added to one.
	Ranking Point Index: (5-p)(L ̅+e^X ̅  )(1+√(h/H)), where X ̅ is the set of the logarithms of each individual value of L, and p is the number of players that are lost.
	The team with the highest ranking-point index wins. If the indices are the same, the defending team wins.
     */

    public static void main(String[] args) {
        new JFXPanel();
        Platform.runLater(new Thread(() -> {
            String[] names = {"Team 1", "Team 2", "Team 3", "Team 4", "Team 5", "Team 6"};
            List<Player> enemies = Stream.generate(() -> new Player(200)).limit(3).collect(Collectors.toList());
            List<Player> players = Stream.generate(() -> new Player(200)).limit(3).collect(Collectors.toList());
            players.addAll(0, enemies);
            PlayerList list = new PlayerList(players.toArray(new Player[0]));
            list.forEach(p -> {
                int index = list.indexOf(p);
                p.setName(names[index]);

                if (index / 3 == 1) { //Index
                    p.setType(Player.Type.USER);
                } else {
                    p.setType(Player.Type.ENEMY);
                }
                p.setClassType(Player.ClassType.ARCHER);
            });
            GameTask task = new GameTask(list);
            try {
                task.log = new GameLog(list);
                task.call();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
    }

    public GameTask(PlayerList playerList) {
        this.playerList = playerList;
    }

    @Override
    protected Void call() {
        new JFXPanel();

        playerList.setSubstitutePlayers();
        //Instantiate the group.
        root = new Group();
        Scene scene = new Scene(root, 750, 600);
        List<UserInterface> interfacesList = new ArrayList<>();
        playerList.forEach(p -> interfacesList.add(new UserInterface(p)));

        for (Player player : playerList.getEnemyPlayers()) {
            int index = playerList.getEnemyPlayers().indexOf(player);
            List<UserInterface> enemiesList = new ArrayList<>(interfacesList);
            enemiesList.removeIf(x -> !x.getPlayer().isEnemy());
            enemiesList.get(index).setLayoutX(index * 190 + 90);
            enemiesList.get(index).setLayoutY(120);
        }
        for (Player player : playerList.getUserPlayers()) {
            int index = playerList.getUserPlayers().indexOf(player);
            List<UserInterface> usersList = new ArrayList<>(interfacesList);
            usersList.removeIf(x -> !x.getPlayer().isUser());
            usersList.get(index).setLayoutX(index * 190 + 90);
            usersList.get(index).setLayoutY(500);
        }

        Node[] nodes = {turns, discardPileSize, discardPileImage, turnButton, buttonBS, logButton};
        Point2D[] places = {new Point2D(290, 250), new Point2D(290, 450),
                new Point2D(290, 350), new Point2D(470, 350), new Point2D(90, 330), new Point2D(90, 370)};

        for (int i = 0; i < 6; i++) {
            nodes[i].setLayoutX(places[i].getX());
            nodes[i].setLayoutY(places[i].getY());
        }
        root.getChildren().addAll(interfacesList);
        turns.setText(String.format("Turns Left: %d", playerList.getTurns()));
        thumbnailList = new ThumbnailList(playerList);
        thumbnailList.setImageList();
        thumbnailList.setLayoutX(660);
        thumbnailList.setLayoutY(350);
        discardPileImage.setImage(Card.CARD_BACK);
        /* autoButton = new ToggleButton("Auto Mode");
        autoButton.setOnMouseClicked(e -> {

        });
        */
        // root.getChildren().add(autoButton);
        root.getChildren().add(turns);
        root.getChildren().add(discardPileSize);
        root.getChildren().add(discardPileImage);
        root.getChildren().add(thumbnailList);
        root.getChildren().add(buttonBS);
        root.getChildren().add(logButton);
        root.getChildren().add(turnButton);

        for (Node node : root.getChildren()) {
            if (node instanceof Button) {
                Button button = (Button) node;
                double width = node.getBoundsInParent().getMinX() - new Text(button.getText()).getBoundsInParent().getWidth() / 2;
                double height = node.getBoundsInParent().getMinY() - new Text(button.getText()).getBoundsInParent().getHeight() / 2;
                node.setLayoutX(width);
                node.setLayoutY(height);
            } else {
                node.setLayoutX(node.getBoundsInParent().getMinX() - node.getBoundsInParent().getWidth() / 2);
                node.setLayoutY(node.getBoundsInParent().getMinY() - node.getBoundsInParent().getHeight() / 2);
            }
        }

        discardPileImage.setImage(null);

        thumbnailList.setLayoutY(0);

        //Turn images
        Stage stage = new Stage();
        stage.setTitle("Baloney Sandwich");
        stage.setScene(scene);
        stage.show();
        takeTurns();
        turnButton.setOnAction(e -> {
            //The current player will...
            if (playerList.hasAceOfSpades()) {
                Player player = playerList.getCurrentPlayer();
                GUITurn guiTurn = new GUITurn(player, playerList);
                Optional<ButtonType> type = guiTurn.showAndWait();
                if (type.isPresent()) {
                    if (type.get() == ButtonType.OK) {
                        Player defender = null;
                        try {
                            defender = guiTurn.getEnemy();
                        } catch (Exception ex) {
                            System.out.println("Please put in your enemy!");
                        }
                        if (guiTurn.getSelectedIndices().size() == 0) {
                            new Alert(Alert.AlertType.WARNING, "Please select at least one card.", ButtonType.OK).showAndWait();
                        } else if (guiTurn.getSelectedIndices().size() > 4) {
                            new Alert(Alert.AlertType.WARNING, "Please select no more than 4 cards.", ButtonType.OK).showAndWait();
                        } else if (defender == null) {
                            new Alert(Alert.AlertType.WARNING, "Please select a player to attack.", ButtonType.OK).showAndWait();
                        } else {
                            turnButton.setDisable(true);
                            List<Integer> integers = guiTurn.getSelectedIndices();
                            integers.sort(Comparator.comparing(Integer::intValue).reversed());
                            List<Card> selectedCards = new ArrayList<>();
                            for (int i : integers) {
                                selectedCards.add(player.getCards().get(i));
                            }
                            player.setCardsPut(selectedCards.size());
                            //Attack another person.
                            try {
                                System.out.println(player + " attacked " + guiTurn.getEnemy() + " after " + (50 - playerList.getTurns()) +
                                        " turns.");
                                player.attack(guiTurn.getEnemy());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            for (Player p : playerList.getPlayers()) {
                                p.countCards(player.getRequestedCard() - 1, player.getCardsPut());
                            }
                            player.getCards().removeAll(selectedCards);
                            discardPile.addAll(selectedCards);
                            try {
                                discardPileImage.setImage(
                                        new Image(new FileInputStream("res/CardBack.jpg"), 100,
                                                100, true, false));
                            } catch (FileNotFoundException ex) {
                                ex.printStackTrace();
                            }
                            try {
                                guiTurn.applySkill();
                                //Now comes the part where the enemies try to call BS on you.

                                for (Player p : playerList.getEnemyPlayers()) {
                                    if ((p.callBS(player) && playerList.getUserPlayers().size() == 3 ||
                                            p.callBSMidgame(player) && playerList.getUserPlayers().size() == 2 ||
                                            p.callBSEndgame(player) && playerList.getUserPlayers().size() == 1)
                                            && !p.isStunned()) { //Because you need to be not stunned at all.
                                        attackBS(p, player); //BS Attacks will not be affected by taunts.
                                        for (Player p1 : playerList.getPlayers()) {
                                            p1.resetReader();
                                        }
                                        break;
                                    } else if (playerList.getEnemyPlayers().indexOf(p) == playerList.getEnemyPlayers().size() - 1) {
                                        takeTurns();
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            handleDeadPlayers();
                        }
                    }
                }
            } else {
                AttackMenu menu = new AttackMenu(playerList, startPlayer);
                menu.show();
                menu.setOnHidden(ev -> {
                    if (menu.getResult() == ButtonType.OK && menu.attackPlayer()) {
                        playerList.setAceOfSpades(true);
                        playerList.setTurnOrder();
                        thumbnailList.setPlayerList(playerList);
                        for (Player p : playerList.getEnemyPlayers()) {
                            p.setAI(playerList.getTurnOrder());
                        }
                        takeTurns();
                    } else if (menu.getResult() == ButtonType.CANCEL) {
                        turnButton.setDisable(false);
                        buttonBS.setDisable(true);
                    }
                });
            }
        });
        logButton.setOnAction(e -> log.start(new Stage()));

        buttonBS.setOnAction(e -> {
            Player player = playerList.getCurrentPlayer();
            ButtonType callBS = new ButtonType("Call Baloney Sandwich on " + player.getName(), ButtonBar.ButtonData.YES);
            ButtonType callNoBS = new ButtonType("Nah, it\'s too risky to call Baloney Sandwich on "
                    + player.getName(), ButtonBar.ButtonData.NO);
            ButtonType cancel = new ButtonType("I need more time to think.", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert bsTurn = new Alert(Alert.AlertType.NONE, "Do you want to call Baloney Sandwich on " + player + "?",
                    callBS, callNoBS, cancel);
            bsTurn.setHeaderText("");
            List<RadioButton> buttons = new ArrayList<>();
            ToggleGroup group = new ToggleGroup();
            VBox box = new VBox(new Text(player + " claims to have put down " + player.getCardsPut()
                    + " card(s) of " + new Card(player.getRequestedCard(), 1).getRankName()), new Text("Who is going to call Baloney Sandwich?"));
            for (Player p : playerList.getUserPlayers()) {
                RadioButton button = new RadioButton("" + p + (p.isStunned() ? " (Stunned)" : ""));
                button.setToggleGroup(group);
                buttons.add(button);
                box.getChildren().add(button);
                button.setDisable(p.isStunned());
            }

            bsTurn.getDialogPane().setContent(box);
            bsTurn.setTitle("Are you sure you want to call Baloney Sandwich?");
            Optional<ButtonType> type = bsTurn.showAndWait();
            if (type.isPresent()) {
                if (type.get() == callBS) {
                    RadioButton button = (RadioButton) group.getSelectedToggle();
                    int index = buttons.indexOf(button);
                    if (index == -1)
                        new Alert(Alert.AlertType.WARNING,
                                "Please choose a player to call BS if you could."
                                        + "\nOtherwise, please select \"Nah, it\'s too risky to call\nBaloney Sandwich on "
                                        + player.getName() + ".\"", ButtonType.OK).showAndWait();
                    else {
                        //Calls BS.
                        attackBS(playerList.getUserPlayers().get(index), player);
                    }
                } else if (type.get() == callNoBS) {
                    turnButton.setDisable(true);
                    buttonBS.setDisable(true);
                    takeTurns();
                } else {
                    e.consume();
                }
            }
        });

        return null;
    }

    public void takeTurns() {
        Stream<Player> userPlayerStream = playerList.getUserPlayers().stream();
        Stream<Player> enemyPlayerStream = playerList.getEnemyPlayers().stream();
        boolean stunnedAndDone;

        if (playerList.getTurns() == 0) {
            new Alert(Alert.AlertType.NONE, "It's a draw...", ButtonType.CLOSE).show();
            return;
        }

        try {
            stunnedAndDone = (userPlayerStream.allMatch(Player::isStunned)
                && enemyPlayerStream.anyMatch(p -> p.getCards().isEmpty())) ||
                (enemyPlayerStream.allMatch(Player::isStunned)
                        && userPlayerStream.anyMatch(p -> p.getCards().isEmpty()));
        } catch (IllegalStateException e) {
            stunnedAndDone = false;
        }

        if (playerList.isAlive()) {
            if (!playerList.hasAceOfSpades()) {
                if (stunnedAndDone) {
                    for (Player player : playerList) {
                        if (player.getType() == playerList.getCurrentPlayer().getType().opposite()) {
                            player.setHealth(new BigDecimal(0.5).multiply(new BigDecimal(player.getHealth())).toBigInteger());
                        }
                    }
                }
                playerList.forEach(Player::countAce);
                warning = false;
                discardPile.clear();
                Deck deck = new Deck();
                //Get everyone some cards
                for (Player player : playerList.getUserPlayers()) {
                    player.clearCards();
                    player.addCards(deck.draw(26 / playerList.getUserPlayers().size()));
                }
                for (Player player : playerList.getEnemyPlayers()) {
                    player.clearCards();
                    player.addCards(deck.draw(26 / playerList.getEnemyPlayers().size()));
                }

                //Start the game, already!
                while (deck.size() != 0) {
                    playerList.getRandomPlayer().addCard(deck.draw());
                }

                //Determine who has the Ace of Spades.
                startPlayer = null;
                for (Player p : playerList.getCurrentPlayers()) {
                    if (p.hasAceOfSpades()) {
                        startPlayer = p;
                        break;
                    }
                }
                //Gets to attack the player in the front.
                try {
                    discardPileImage.setImage(new Image(new FileInputStream("res/Ace of Spades.jpg")));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (startPlayer != null) {
                    startPlayer.getCards().remove(new Card("As"));
                    discardPile.add(new Card("As"));
                    if (startPlayer.isUser()) {
                        AttackMenu menu = new AttackMenu(playerList, startPlayer);
                        menu.show();
                        menu.setOnHidden(e -> {
                            if (menu.getResult() == ButtonType.OK && menu.attackPlayer()) {
                                playerList.setTurnOrder();
                                thumbnailList.setPlayerList(playerList);
                                playerList.setAceOfSpades(true);
                                for (Player p : playerList.getPlayers()) {
                                    p.setAI(playerList.getTurnOrder());
                                }
                                takeTurns();
                            } else if (menu.getResult() == ButtonType.CANCEL) {
                                turnButton.setDisable(false);
                                buttonBS.setDisable(true);
                            }
                        });
                    } else {
                        playerList.setTurnOrder();
                        thumbnailList.setPlayerList(playerList);
                        for (Player p : playerList.getPlayers()) {
                            p.setAI(playerList.getTurnOrder());
                        }
                        List<Player> users = playerList.getUserPlayers();
                        Collections.shuffle(users);
                        users.sort(Comparator.comparing(Player::getHealth));
                        try {
                            startPlayer.setCardsPut(1);
                            startPlayer.attack(users.get(0));
                            startPlayer.setCardsPut(0);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.out.println(startPlayer + " attacked " + users.get(0) + " after " + (50 - playerList.getTurns()) +
                                " turns.");
                        handleDeadPlayers();
                        takeTurns();
                    }
                }
            } else {
                boolean insufficientCards = playerList.testFor(p -> p.getCards().size() == 0);
                if (insufficientCards) {
                    for (Player player : playerList) {
                        if (player.getType() == playerList.getCurrentPlayer().getType().opposite()) {
                            player.setHealth(new BigDecimal(0.5).multiply(new BigDecimal(player.getHealth())).toBigInteger());
                        }
                    }
                    playerList.setAceOfSpades(false);
                } else {
                    if (playerList.getCurrentPlayer().getCooldown() != 0) {
                        playerList.getCurrentPlayer().takeTurns();
                    }
                    playerList.setNextOrder();
                    thumbnailList.transition();
                    Player player = playerList.getCurrentPlayer();
                    player.updateStatusEffects();
                    updateUserInterfaces();

                    boolean enemyCardsRunOut = false;
                    for (Player p : playerList.getEnemyPlayers()) {
                        enemyCardsRunOut |= p.getCards().size() <= 3;
                    }

                    if (!warning && playerList.getTurns() == 15 && enemyCardsRunOut) {
                        new Alert(Alert.AlertType.WARNING,
                                "Uh-oh! You only have 15 turns left and your\nenemy has only 3 cards left!").show();
                        warning = true;
                    } else if (!warning && playerList.getTurns() == 15) {
                        new Alert(Alert.AlertType.WARNING,
                                "Uh-oh! You only have 15 turns left!").show();
                    } else if (!warning && enemyCardsRunOut) {
                        new Alert(Alert.AlertType.WARNING,
                                "Uh-oh! Your enemy has only 3 cards left!").show();
                        warning = true;
                    }
                    if (player.isUser()) {
                        //Turn Button
                        buttonBS.setDisable(true);
                        turnButton.setDisable(player.isStunned());
                        updateUserInterfaces();
                        if (player.isStunned()) {
                            takeTurns();
                        }
                    } else if (!player.isStunned()) {
                        //BS Button
                        buttonBS.setDisable(false); //Two players can still be involved in BS.
                        turnButton.setDisable(true);
                        List<Player> users = new ArrayList<>(playerList.getUserPlayers());
                        Collections.shuffle(users);
                        users = users.stream().sorted(Comparator.comparing(Player::getClassType).reversed().thenComparing(Player::getHealth)).collect(Collectors.toList());
                        //Play cards
                        player.setCardsPut(0);
                        List<Integer> AI = player.getAI();
                        Set<Integer> rawStrategy = new HashSet<>(player.getStrategy());
                        List<Integer> strategy = new ArrayList<>(rawStrategy);
                        Collections.reverse(strategy);
                        //This is where the enemy could play cards.

                        List<Card> selectedCards = new ArrayList<>();
                        List<Card> cards;
                        if (strategy.contains(player.getRequestedCard())) {
                            strategy.remove((Integer) player.getRequestedCard());
                            strategy.add(0, player.getRequestedCard());
                        }
                        for (int i : strategy) {
                            cards = new ArrayList<>(player.getCards());
                            cards.removeIf(c -> c.getRank() != i);
                            if (!cards.isEmpty() && i == player.getRequestedCard()) {
                                //Divide them up according to the number of elements in the list.
                                Collections.shuffle(cards);
                                selectedCards = cards.stream()
                                        .limit(Math.max(cards.size() / Math.max(Collections.frequency(AI, player.getRequestedCard()), 1), 1)).collect(Collectors.toList());
                                break;
                            } else if (!cards.isEmpty()) {
                                AI.removeIf(x -> x != i);
                                Collections.shuffle(cards);
                                selectedCards.add(cards.get(0));
                                break;
                            }
                        }
                        player.setCardsPut(selectedCards.size());
                        try {
                            player.attack(users.get(0));
                            System.out.println(player + " attacked " + users.get(0) + " after " + (50 - playerList.getTurns()) +
                                    " turns.");
                            updateUserInterfaces();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        player.getCards().removeAll(selectedCards);
                        discardPile.addAll(selectedCards);
                        try {
                            discardPileImage.setImage(
                                    new Image(new FileInputStream("res/CardBack.jpg"), 100,
                                            100, true, false));
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }

                        if (!player.isStunned()) {
                            if (player.getClassType() == Player.ClassType.TANK) {
                                player.useSkill(playerList.getUserPlayers());
                                if (player.getCooldown() == 10 ||
                                        player.getCooldown() == 5 && player.getClassType() == Player.ClassType.SUPPORT)
                                    System.out.println(player + " used skill on every player");
                            } else if (player.getClassType() == Player.ClassType.SUPPORT) {

                                Map<Double, Player> map = new HashMap<>();
                                for (Player p : playerList.getEnemyPlayers()) {
                                    map.put(p.getHealthBar().getProgress(), p); //Sorts by the percentage of HP that they have.
                                }
                                List<Map.Entry<Double, Player>> list = new ArrayList<>(map.entrySet());
                                list.sort((entry1, entry2) -> (int) Math.signum(entry1.getKey() - entry2.getKey()));
                                Player enemy = list.get(0).getValue();
                                double healthCondition = list.get(0).getKey();
                                if (healthCondition < 0.6) {
                                    player.useSkill(enemy);
                                    if (player.getCooldown() == 10 ||
                                            player.getCooldown() == 5 && player.getClassType() == Player.ClassType.SUPPORT)
                                        System.out.println(player + " used skill on " + enemy);
                                }
                            } else {
                                if (player.getFocusPlayer() != null) {
                                    player.useSkill(player.getFocusPlayer());
                                    if (player.getCooldown() == 10 ||
                                            player.getCooldown() == 5 && player.getClassType() == Player.ClassType.SUPPORT) {
                                        System.out.println(player + " used skill on " + player.getFocusPlayer());
                                    }
                                } else {
                                    Map<BigInteger, Player> map = new HashMap<>();
                                    for (Player p : playerList.getUserPlayers()) {
                                        map.put(p.getHealth(), p); //Sorts by the amount of HP that they have.
                                    }
                                    List<Map.Entry<BigInteger, Player>> list = new ArrayList<>(map.entrySet());
                                    //Sort in terms of damage

                                    Collections.shuffle(list);

                                    list.sort(Comparator.comparing(entry -> entry.getKey()
                                            .subtract(entry.getValue().getBleedingDamage().add(entry.getValue().getBurningDamage()))));

                                    while (list.size() > 1) {
                                        if (list.get(0).getKey().compareTo(BigInteger.ZERO) <= 0) {
                                            list.remove(0);
                                        } else {
                                            break;
                                        }
                                    }

                                    Player user = list.get(0).getValue();
                                    player.useSkill(user);
                                    if (player.getCooldown() == 10 ||
                                            player.getCooldown() == 5 && player.getClassType() == Player.ClassType.SUPPORT)
                                        System.out.println(player + " used skill on " + users.get(0));
                                }
                            }
                        }
                        updateUserInterfaces();
                        playerList.setAceOfSpades(!stunnedAndDone);
                    } else {
                        playerList.setAceOfSpades(!stunnedAndDone);
                        takeTurns();
                    }
                    handleDeadPlayers();
                }
            }
        }
    }

    public void handleDeadPlayers() {
        List<Player> users = playerList.getUserPlayers();
        List<Player> enemies = playerList.getEnemyPlayers();
        List<Player> deadPlayers = playerList.getPlayers();
        deadPlayers.removeAll(playerList.getCurrentPlayers());
        //We need some system to do that...

        if (deadPlayers.size() > numDead) {
            //Restart the AI!
            /*
            for (Player p : deadPlayers) {
                //Get the substitute players if possible

                PlayerList substitutePlayers = substituteList;
                List<Player> userList = substitutePlayers.getUserPlayers();
                List<Player> enemyList = substitutePlayers.getEnemyPlayers();
                if (p.isUser() && userList.size() > 0) {
                    Player user = userList.get(0);
                    playerList.add(user);
                    substitutePlayers.remove(user);
                } else if (p.isEnemy() && enemyList.size() > 0) {
                    Player enemy = enemyList.get(0);
                    playerList.add(enemy);
                    substitutePlayers.remove(enemy);
                }
            }
            */
            if (!playerList.isAlive()) {
                playerList.forEach(p -> {
                    if (!p.isDead())
                        p.setTurnsSurvived(50 - playerList.getTurns());
                });
                if (enemies.size() == 0) {
                    new Alert(Alert.AlertType.NONE, "Congratulations! You have won!", ButtonType.CLOSE).show();
                } else if (users.size() == 0) {
                    new Alert(Alert.AlertType.NONE, "Sorry... you have lost...", ButtonType.CLOSE).show();
                }
                turnButton.setDisable(true);
                buttonBS.setDisable(true);
            } else {
                numDead = deadPlayers.size();
                List<Player> prevDeadPlayers = new ArrayList<>(playerList.getDeadPlayers());
                playerList.setDeadPlayers(deadPlayers);
                List<Player> newDeadPlayers = new ArrayList<>(deadPlayers);
                newDeadPlayers.removeAll(prevDeadPlayers);
                for (Player p : newDeadPlayers) {
                    int turns = playerList.getTurns();
                    System.out.println("In " + (50 - turns) + " turns, " + p.getName() + " has died...");
                }
                newDeadPlayers.removeIf(p -> p.getClassType() != Player.ClassType.TANK);
                for (Player player : playerList) {
                    if (newDeadPlayers.contains(player.getFocusPlayer())) {
                        player.getStatusEffects().removeIf(e -> e.equals(StatusEffect.TAUNTED));
                        player.setFocus(null);
                    }
                }
                deadPlayers.clear();
                playerList.setTurnOrder();
                thumbnailList.setPlayerList(playerList);
                for (Player p : enemies) {
                    p.setAI(playerList.getTurnOrder());
                }
                playerList.setAceOfSpades(false);
                takeTurns();

            }
        } else {
            playerList.setAceOfSpades(true);
        }
    }

    public void updateUserInterfaces() {
        discardPileSize.setText("Discard Pile: " + discardPile.size());
        Player currentPlayer = playerList.getCurrentPlayer();
        for (int i = 0; i < root.getChildren().size(); i++) {
            Node node = root.getChildren().get(i);
            if (node instanceof UserInterface) {
                UserInterface userInterface = (UserInterface) node;
                userInterface.update();
                Player player = userInterface.getPlayer();
                BigInteger difference = player.getHealth();
                BigInteger minuend = new ScientificNumber(player.getMaxHealth()).multiply(userInterface.getHPBar().getProgress());
                difference = difference.subtract(minuend);
                String string = new BigDecimal(difference).round(new MathContext(3)).toString().toLowerCase() + String.format(" (%.2f%%)",
                        100 * (player.getHealthBar().getProgress() - userInterface.getHPBar().getProgress()));
                double x = userInterface.localToParent(userInterface.getBoundsInLocal()).getMinX()
                        + userInterface.localToParent(userInterface.getBoundsInLocal()).getWidth() / 2;
                double y = userInterface.localToParent(userInterface.getBoundsInLocal()).getMinY()
                        + userInterface.localToParent(userInterface.getBoundsInLocal()).getHeight() / 2;

                Text healthText = new Text("+" + string);

                Timeline timeline = new Timeline();

                if (player.getHealthBar().getProgress() > userInterface.getHPBar().getProgress()) {
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(500),
                            new KeyValue(userInterface.getHPBar().progressProperty(), player.getHealthBar().getProgress())));
                    double centerX = x - healthText.localToParent(healthText.getBoundsInLocal()).getWidth() / 2;
                    double centerY = y - healthText.localToParent(healthText.getBoundsInLocal()).getHeight();
                    healthText.setLayoutX(centerX);
                    healthText.setLayoutY(centerY);
                    healthText.setStroke(Color.GREEN);
                    root.getChildren().add(healthText);
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5),
                            new KeyValue(healthText.opacityProperty(), 0)));
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3),
                            new KeyValue(healthText.translateYProperty(), -100)));
                    //Color the text green because it is a healing effect.
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3),
                            new KeyValue(healthText.strokeProperty(), Color.GREEN)));
                } else if (player.getHealthBar().getProgress() < userInterface.getHPBar().getProgress()) {
                    timeline.getKeyFrames().add(new KeyFrame(new Duration(500),
                            new KeyValue(userInterface.getHPBar().progressProperty(), player.getHealthBar().getProgress())));
                    healthText = new Text(string);
                    healthText.setStroke(Color.RED);
                    double centerX = x - healthText.localToParent(healthText.getBoundsInLocal()).getWidth() / 2;
                    double centerY = y - healthText.localToParent(healthText.getBoundsInLocal()).getHeight() / 2;

                    //How to only get the player that is attacked?
                    if (currentPlayer.hasBlockedAttack() && player.isAttacked()) {
                        Text textBlocked = new Text("Blocked!");
                        textBlocked.setStroke(Color.GRAY);
                        textBlocked.setX(centerX);
                        textBlocked.setY(centerY + 20);
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5),
                                new KeyValue(textBlocked.opacityProperty(), 0)));
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2),
                                new KeyValue(textBlocked.strokeProperty(), Color.WHITESMOKE)));
                        root.getChildren().add(textBlocked);
                    }
                    if (currentPlayer.hasCritAttack() && player.isAttacked()) {
                        Text textCritical = new Text("Critical!");
                        textCritical.setX(centerX);
                        textCritical.setY(centerY + 40);
                        textCritical.setStroke(Color.YELLOW);
                        root.getChildren().add(textCritical);
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5),
                                new KeyValue(textCritical.opacityProperty(), 0)));
                        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(2),
                                new KeyValue(textCritical.strokeProperty(), Color.ORANGERED)));
                    }
                    healthText.setLayoutX(centerX);
                    healthText.setLayoutY(centerY);
                    healthText.setStroke(Color.RED);
                    root.getChildren().add(healthText);
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(5),
                            new KeyValue(healthText.opacityProperty(), 0)));
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3),
                            new KeyValue(healthText.strokeProperty(), Color.RED)));
                    timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(3),
                            new KeyValue(healthText.translateYProperty(), -100)));
                    //Color the text red because it is a damaging effect.
                }
                currentPlayer.setAttackBlocked(false);
                currentPlayer.setAttackCrit(false);
                timeline.play();
            }
        }
        turns.setText("Turns Left: " + playerList.getTurns());
    }



    public void setDiscardPileImage(ImageView discardPileImage) {
        this.discardPileImage = discardPileImage;
    }

    public ImageView getDiscardPileImage() {
        return discardPileImage;
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }


    /**
     * Attacks the player with the size of the cards.
     *  @param attacker The player who called "Baloney Sandwich".
     * @param defender The player to call "Baloney Sandwich" on.*/
    public void attackBS(Player attacker, Player defender) {
        List<Card> cards = discardPile;
        List<Card> selection = cards.subList(cards.size() - defender.getCardsPut(), cards.size());
        Timeline timeline = new Timeline();
        boolean bsSuccessful = false;

        int numTurns = 50 - playerList.getTurns();

        System.out.println("In " + (50 - playerList.getTurns()) + " turns, " + attacker + " called Baloney Sandwich on " + defender + ".");

        Text text = new Text("Baloney Sandwich!");
        text.setFont(Font.font(text.getFont().getFamily(), 30));
        double centerX = 350 - text.getBoundsInParent().getWidth() / 2;
        double centerY = 300 - text.getBoundsInParent().getHeight() / 2;
        text.setLayoutX(centerX);
        text.setLayoutY(centerY);

        Text subText = new Text("Successful");
        subText.setFont(Font.font(subText.getFont().getFamily(), 20));
        double centerXSub = 350 - subText.getBoundsInParent().getWidth() / 2;
        double centerYSub = 310 + subText.getBoundsInParent().getHeight() / 2;
        subText.setLayoutX(centerXSub);
        subText.setLayoutY(centerYSub);
        subText.setText("");
        Text playerText = new Text();

        String[] possibleSuccessComments = {
                "%s has to draw the cards because of %s.", //Defender, Attacker
                "%s fell victim to %s.", //Defender, Attacker
                "%s, how dare you lie to %s!", //Defender, Attacker
                "Resistance is futile, %s, thanks to %s.", //Defender, Attacker
                "Look at what you've done to %s, %s!", //Defender, Attacker
                "What on Earth did you do to %s, %s?", //Defender, Attacker
                "Here's your reward for calling Baloney Sandwich on %s, %s.", //Defender, Attacker
                "Thank you, %s! You just made %s draw the cards.", //Attacker, Defender
                "You might need to upgrade your insurance against %s, %s.",
                "I blame %s for making %s draw the cards!", //Attacker, Defender
                "%s, how dare you make %s draw the cards!", //Attacker, Defender
                "You're about to get yeeted by %s, %s!", //Attacker, Defender
                "It's so hard trying to keep up with the calls of %s, %s", //Attacker, Defender
                "Go, %s, you can defeat %s!", //Attacker, Defender
                "Congratulations, %s, you did the right maneuver on %s!", //Attacker, Defender
                "Keep it up, %s, show %s the right way to do it!",
                "Good job, %s, you showed %s the true meaning of Baloney Sandwich!",
                "%s, how did you know that %s was lying?",
                "Congratulations, %s, you swept the floor with %s!"
        };
        /*

         */
        String[] possibleFailureComments = {
                "Would you like a cupcake, %s?",
                "On the bright side, I brought you a teddy bear, %s.",
                "Every action has an equal and opposite reaction, %s.",
                "%s, I suggest you have a pizza party to compensate for your loss.",
                "Is that your final answer, %s?",
                "Hasta la vista, %s.",
                "Please don't call Baloney Sandwich again, %s.",
                "I have a bad feeling about this, %s.",
                "%s, you got some splaining to do!",
                "May I present to you the Darwin Award, %s?",
                "You've yeed your last haw, %s!",
                "Did you really just yeet yourself, %s?",
                "Well yes, but actually no, %s.",
                "Well, at least you tried, %s...",
                "When pigs fly, %s, you will successfully call Baloney Sandwich.",
                "Hush, little %s, don't you cry...",
                "It's OK, %s, we all make mistakes.",
                "Here's your reward for calling too many Baloney Sandwiches, %s.",
                "Cool, very cool, %s."
        };

        //if Baloney Sandwich fails...
        List<String> possibleFailureCommentsList = Arrays.asList(possibleFailureComments);
        List<String> possibleSuccessCommentsList = Arrays.asList(possibleSuccessComments);
        Random random = new Random();
        int selected = random.nextInt(19);

        double centerXPlayer = 300 - playerText.getBoundsInParent().getWidth() / 2;
        double centerYPlayer = 340 + subText.getBoundsInParent().getHeight() / 2;
        playerText.setFont(Font.font(playerText.getFont().getFamily(), 20));
        playerText.setLayoutX(centerXPlayer);
        playerText.setLayoutY(centerYPlayer);
        root.getChildren().add(text);
        root.getChildren().add(subText);
        root.getChildren().add(playerText);
        try {
            List<Card> bsCards = new ArrayList<>();
            for (Card card : selection) {
                if (card.getRank() != defender.getRequestedCard()) {
                    bsCards.add(card);
                }
                timeline.getKeyFrames()
                        .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card)),
                                new KeyValue(discardPileImage.imageProperty(),
                                        card.getImage())));
                if (card.getRank() != defender.getRequestedCard()) {
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 2),
                                    new KeyValue(discardPileImage.imageProperty(),
                                            null)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 2),
                                    new KeyValue(subText.textProperty(),
                                            "Successful!")));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 3),
                                    new KeyValue(playerText.textProperty(),
                                            String.format(possibleSuccessCommentsList.get(selected),
                                                    (selected > 6) ? attacker : defender,
                                                    (selected > 6) ? defender : attacker))));
                    bsSuccessful = true;
                    Color[] colors = new Color[]
                            {Color.RED, Color.YELLOW, Color.LIME, Color.CYAN, Color.BLUE, Color.MAGENTA};

                    int r = (int) (Math.random() * 6);
                    for (int i = 0; i <= 12; i++) {
                        //12 * 0.5 = 6 seconds between the last card and the disappearance of the animation
                        timeline.getKeyFrames()
                                .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 0.5 * i),
                                        new KeyValue(subText.strokeProperty(), colors[(r + i) % 6])));
                    }

                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 6), //size() == indexOf(last) + 1
                                    new KeyValue(subText.visibleProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 6),
                                    new KeyValue(text.visibleProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 6),
                                    new KeyValue(playerText.visibleProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 6),
                                    new KeyValue(subText.managedProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 6),
                                    new KeyValue(text.managedProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 6),
                                    new KeyValue(playerText.managedProperty(), false)));
                    break;
                } else if (bsCards.size() == 0 && selection.indexOf(card) == selection.size() - 1) {
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 2),
                                    new KeyValue(discardPileImage.imageProperty(),
                                            null)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 2),
                                    new KeyValue(subText.textProperty(),
                                            "Failed!")));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 2),
                                    new KeyValue(subText.strokeProperty(), Color.RED)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 2.5),
                                    new KeyValue(subText.strokeProperty(), Color.DARKRED)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 3),
                                    new KeyValue(subText.strokeProperty(), Color.BLACK)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.indexOf(card) + 3),
                                    new KeyValue(playerText.textProperty(),
                                            String.format(possibleFailureCommentsList.get(selected), attacker))));

                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.size() + 4), //size() == indexOf(last) + 1
                                    new KeyValue(subText.visibleProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.size() + 4),
                                    new KeyValue(text.visibleProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.size() + 4),
                                    new KeyValue(playerText.visibleProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.size() + 4),
                                    new KeyValue(subText.managedProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.size() + 4),
                                    new KeyValue(text.managedProperty(), false)));
                    timeline.getKeyFrames()
                            .add(new KeyFrame(Duration.seconds(2 * selection.size() + 4),
                                    new KeyValue(playerText.managedProperty(), false)));
                }
            }
            if (bsSuccessful) {
                centerXPlayer = 300 - new Text(String.format(possibleSuccessCommentsList.get(selected),
                        (selected > 6) ? attacker : defender,
                        (selected > 6) ? defender : attacker)).getBoundsInParent().getWidth() / 2;
                centerYPlayer = 340 + subText.getBoundsInParent().getHeight() / 2;
                playerText.setFont(Font.font(playerText.getFont().getFamily(), 20));
                playerText.setLayoutX(centerXPlayer);
                playerText.setLayoutY(centerYPlayer);
            } else {
                centerXPlayer = 300 - new Text(String.format(possibleFailureCommentsList.get(selected), attacker)).getBoundsInParent().getWidth() / 2;
                centerYPlayer = 340 + subText.getBoundsInParent().getHeight() / 2;
                playerText.setFont(Font.font(playerText.getFont().getFamily(), 20));
                playerText.setLayoutX(centerXPlayer);
                playerText.setLayoutY(centerYPlayer);
            }
            if (bsCards.size() > 0) {
                try {
                    attacker.attack(defender, cards.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                defender.addCards(discardPile);
                discardPile.clear();
                boolean insufficientCards = playerList.testFor(p -> p.getCards().size() == 0);
                if (insufficientCards) {
                    for (Player player : playerList) {
                        if (player.getType() == attacker.getType().opposite()) {
                            player.setHealth(new BigDecimal(0.5).multiply(new BigDecimal(player.getHealth())).toBigInteger());
                        }
                    }
                    playerList.setAceOfSpades(false);
                }
            } else {
                try {
                    defender.attack(attacker, cards.size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                attacker.addCards(discardPile);
                discardPile.clear();
                boolean insufficientCards = playerList.testFor(p -> p.getCards().size() == 0);
                if (insufficientCards) {
                    for (Player player : playerList) {
                        if (player.getType() == defender.getType().opposite()) {
                            player.setHealth(new BigDecimal(0.5).multiply(new BigDecimal(player.getHealth())).toBigInteger());
                        }
                    }
                    playerList.setAceOfSpades(false);
                }
            }
            buttonBS.setDisable(true);
            turnButton.setDisable(true);
            discardPileSize.setText("Discard Pile: " + discardPile.size());

            timeline.setOnFinished(e -> {
                if (attacker.isDead() || defender.isDead()) {

                    updateUserInterfaces();

                    List<Player> users = playerList.getUserPlayers();
                    List<Player> enemies = playerList.getEnemyPlayers();
                    List<Player> deadPlayers = playerList.getPlayers();
                    deadPlayers.removeAll(playerList.getCurrentPlayers());

                    if (!playerList.isAlive()) {
                        if (enemies.size() == 0) {
                            new Alert(Alert.AlertType.NONE, "Congratulations! You have won!", ButtonType.CLOSE).show();
                        } else if (users.size() == 0) {
                            new Alert(Alert.AlertType.NONE, "Sorry... you have lost...", ButtonType.CLOSE).show();
                        }
                        turnButton.setDisable(true);
                        buttonBS.setDisable(true);
                    } else {
                        numDead = deadPlayers.size();
                        playerList.setDeadPlayers(deadPlayers);
                        deadPlayers.clear();
                        playerList.setTurnOrder();
                        thumbnailList.setPlayerList(playerList);
                        for (Player p : enemies) {
                            p.setAI(playerList.getTurnOrder());
                        }
                        playerList.setAceOfSpades(false);
                        warning = false;
                        discardPile.clear();
                        Deck deck = new Deck();
                        //Get everyone some cards
                        for (Player player : playerList.getUserPlayers()) {
                            player.clearCards();
                            player.addCards(deck.draw(26 / playerList.getUserPlayers().size()));
                        }
                        for (Player player : playerList.getEnemyPlayers()) {
                            player.clearCards();
                            player.addCards(deck.draw(26 / playerList.getEnemyPlayers().size()));
                        }

                        //Start the game, already!
                        while (deck.size() != 0) {
                            playerList.getRandomPlayer().addCard(deck.draw());
                        }

                        //Determine who has the Ace of Spades.
                        Player startPlayer = null;
                        for (Player p : playerList.getCurrentPlayers()) {
                            if (p.hasAceOfSpades()) {
                                startPlayer = p;
                                break;
                            }
                        }
                        //Gets to attack the player in the front.
                        try {
                            discardPileImage.setImage(new Image(new FileInputStream("res/Ace of Spades.jpg")));
                        } catch (FileNotFoundException ex) {
                            ex.printStackTrace();
                        }
                        if (startPlayer != null && startPlayer.isUser()) {
                            turnButton.setDisable(false);
                            buttonBS.setDisable(true);
                        } else if (startPlayer != null) {
                            users = playerList.getUserPlayers();
                            users.sort(Comparator.comparing(Player::getHealth));
                            try {
                                startPlayer.attack(users.get(0));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            handleDeadPlayers();
                        }
                    }
                }
                if (numTurns == 50 - playerList.getTurns()) {
                    takeTurns();
                }
            });

            timeline.play();
            turns.setText("Turns Left: " + playerList.getTurns());
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println((bsSuccessful ? (String.format(possibleSuccessCommentsList.get(selected),
                (selected > 6) ? attacker : defender,
                (selected > 6) ? defender : attacker)) : String.format(possibleFailureComments[selected], attacker)));
    }
}
