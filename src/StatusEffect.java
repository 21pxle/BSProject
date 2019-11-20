public class StatusEffect {

    public static final StatusEffect BURNING = new StatusEffect(0, "Burning"),
            STUNNED = new StatusEffect(1, "Stunned"),
            TAUNTED = new StatusEffect(2, "Taunted"),
            BLEEDING = new StatusEffect(3, "Bleeding");
    private String name;
    private int id, turns;
    private Player player;

    public StatusEffect(StatusEffect effect) {
        id = effect.id;
        turns = effect.turns;
        name = effect.name;
    }
    public StatusEffect(int id, String name) {
        turns = 0;
        this.name = name;
        this.id = id;
    }

    public StatusEffect(int turns, int id, String name) {
        this.turns = turns;
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getTurns() {
        return turns;
    }

    public void setTurns(int turns) {
        this.turns = turns;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public void decreaseDuration() {
        turns--;
    }

    public int getDuration() {
        return turns;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof StatusEffect) {
            StatusEffect effect = (StatusEffect) other;
            return effect.getName().equals(name);
        }
        return false;
    }
}