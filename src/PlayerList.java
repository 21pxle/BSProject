import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PlayerList extends AbstractList<Player> {
    private ArrayList<Player> playerList, substitutePlayers = new ArrayList<>();
    private int turns = 50, accumulatedTurns = 1,
        cooldown = 0, cooldownPerSkill = 0;
    private boolean hasAceOfSpades = false; //To make sure that the ace of spades will be played.
    private HashMap<Integer, Player> turnOrder;
    private List<Player> deadPlayers = new ArrayList<>();

    public PlayerList(Player... players) {
        playerList = new ArrayList<>(Arrays.asList(players));
    }

    public PlayerList() {
        playerList = new ArrayList<>();
    }
    /**
     * @return The players in the list.
     */
    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>(playerList);
        players.addAll(deadPlayers);
        return players;
    }

    public boolean testFor(Predicate<Player> predicate) {
        for (Player player : getCurrentPlayers()) {
            if (predicate.test(player)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return The list of enemy players.
     */
    public List<Player> getEnemyPlayers() {
        List<Player> players = new ArrayList<>(playerList);
        players.removeIf(p -> p.getType() == Player.Type.USER || p.isDead());
        return players;
    }
    
    /**
     * @return The list of User players.
     */
    public List<Player> getUserPlayers() {
        List<Player> players = new ArrayList<>(playerList);
        players.removeIf(p -> p.getType() == Player.Type.ENEMY || p.isDead());
        return players;
    }

    public int size() {
        return playerList.size();
    }

    /**
     * Adds a player to the list.
     * @param player The player to add.
     */
    public boolean add(Player player) {
        return playerList.add(player);
    }

    @Override
    public boolean removeIf(Predicate<? super Player> filter) {
        return playerList.removeIf(filter);
    }

    @Override
    public Player get(int index) {
        return playerList.get(index);
    }

    /**
     * @param action The action to accept for each player.
     */
    public void forEach(Consumer<? super Player> action) {
        playerList.forEach(action);
    }

    /**
     * @param p The player to search for
     * @return The index of the player.
     */
    public int indexOf(Player p) {
        return playerList.indexOf(p);
    }

    public Player[] toArray() {
        return (Player[]) playerList.toArray();
    }

    public Player getRandomPlayer() {
        List<Player> temp = new ArrayList<>(getCurrentPlayers());
        Collections.shuffle(temp);
        return temp.get(0);
    }

    public boolean removeAll(Collection<?> collection) {
        return playerList.removeAll(collection);
    }

    public void remove(Player player) {
        playerList.remove(player);
    }

    //Be sure to do that every time a player dies or at the start of the game.

    public HashMap<Integer, Player> getTurnOrder() {
        if (turnOrder == null) {
            setTurnOrder();
        }
        return turnOrder;
    }

    public void setTurnOrder() {
        accumulatedTurns = 1;
        // Now ponder the order of the players...
        //Get order... for the next 300 turns (30 times will the player run and collect data.)
        HashMap<Integer, Player> AI = new HashMap<>();

        //List of tuples containing the player and the number of ticks.
        List<Tuple<Player, Integer>> list = new ArrayList<>();

        //Calculating the first 300 (or less, if the game has already started) iterations of each player's list.
        for (Player p : playerList) {
            if (!p.isDead()) {
                for (int i = 0; i <= turns; i++) {
                /* Print out numbers that when multiplied by j, will exceed (minimally)
                the multiples of 1000 (with initial progress counted in). The
                cards will be reset after a player dies, but the progress bar will be retained. */
                    int x = (int) Math.ceil((1000d * i - p.getProgress()) / p.getSpeed());
                    list.add(new Tuple<>(p, x));
                }
            }
        }


        //Now to update the AI using list. How does one add in a list and correspond it with each player?
        Collections.shuffle(list);
        list.sort(Comparator.comparing(Tuple::getValue));

        list = list.subList(0, turns + 1);

        for (Tuple<Player, Integer> tuple : list) {
            AI.put(list.indexOf(tuple) + 1, tuple.getKey());
        }
        turnOrder = AI;
    }

    public List<Player> getCurrentPlayers() {
        List<Player> list = new ArrayList<>(getPlayers());
        list.removeIf(Player::isDead);
        return list;
    }


    public int getTurns() {
        return turns;
    }

    public void setAceOfSpades(boolean b) {
        hasAceOfSpades = b;
    }

    public boolean isAlive() {
        return getEnemyPlayers().size() > 0 && getUserPlayers().size() > 0;
    }

    public void setSubstitutePlayers() {
        List<Player> players = getUserPlayers();
        List<Player> enemies = getEnemyPlayers();
        playerList.forEach(p -> {
            if (players.indexOf(p) > 2 || enemies.indexOf(p) > 2) {
                substitutePlayers.add(p);
            }
        });
        playerList.removeAll(substitutePlayers);
    }

    public void setNextOrder() {
        turns--;
        forEach(p -> {
            if (!p.isDead())
                p.setTurnsSurvived(50 - turns);
        });
        accumulatedTurns = 1 + accumulatedTurns % 13;
        //{1,..., N} (Size: N - 1) -> {1,..., N - 1} (Size: N - 2)
        int[] arr = turnOrder.keySet().stream().mapToInt(i -> i - 1).skip(1).toArray();
        List<Player> players = new ArrayList<>(turnOrder.values());
        players = players.subList(1, players.size());
        turnOrder.clear();
        for (int i : arr) {
            turnOrder.put(i, players.get(i - 1));
        }
        for (Player p : getPlayers()) {
            p.setRequestedCard(accumulatedTurns);
        }
    }

    public Player getCurrentPlayer() {
        return turnOrder.get(1);
    }

    public void addDeadPlayers(List<Player> deadPlayers) {
        this.deadPlayers.addAll(deadPlayers);
    }

    public List<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public boolean hasAceOfSpades() {
        return hasAceOfSpades;
    }

    public void setDeadPlayers(List<Player> deadPlayers) {
        this.deadPlayers = deadPlayers;
    }
}