import javafx.beans.property.*;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.*;

public class Player {

    private Type type;
    private ClassType classType = null;
    private List<StatusEffect> effects = new ArrayList<>();
    private BigInteger attack, defense, health, maxHealth;
    private int level, blockRate, criticalRate;
    private String name = "";
    private Image image = null;
    private List<Card> cardList = new ArrayList<>();
    private int speed = 0, progress = 0, requestedCard = 0, order = 0, cardsPut = 0;
    private StringProperty nameProperty = new SimpleStringProperty();
    private IntegerProperty orderProperty, turnsSurvived = new SimpleIntegerProperty(0),
            kills = new SimpleIntegerProperty(0);
    private ImageView imageView;
    private List<Integer> AI;
    private CardReader reader = new CardReader();
    private Set<Integer> cardStrategy;
    private Player tauntingPlayer;
    private int cooldown;
    private boolean stunned, blocked, crit, attacked;
    private SimpleObjectProperty<BigInteger> damageDealt = new SimpleObjectProperty<>(BigInteger.ZERO);
    private SimpleObjectProperty<BigInteger> damageTaken = new SimpleObjectProperty<>(BigInteger.ZERO);
    private SimpleIntegerProperty baloneySandwiches = new SimpleIntegerProperty(0);

    public ProgressBar getHealthBar() {
        return new ProgressBar(new BigDecimal(health).divide(new BigDecimal(maxHealth),
                new MathContext(8)).doubleValue());
    }

    public void addProgress() {
        progress += speed;
    }

    /**
     * @return The progress that determines how long to wait until a turn will be made.
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Forces the player to reset their progress, but will make them gain
     * one charge of the skill.
     */
    public void takeTurns() {
        cooldown--;
    }

    public int getTurnsSurvived() {
        return turnsSurvived.get();
    }

    public void setTurnsSurvived(int turnsSurvived) {
        this.turnsSurvived.set(turnsSurvived);
    }

    public boolean isEnemy() {
        return type == Type.ENEMY;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public BigInteger getMaxHealth() {
        return maxHealth;
    }

    public String getMaxHealthString() {
        return new ScientificNumber(maxHealth).toString();
    }

    public String getAttackString() {
        return new ScientificNumber(attack).toString();
    }

    public String getDefenseString() {
        return new ScientificNumber(defense).toString();
    }

    public int getSpeed() {
        return speed;
    }

    public void addCards(List<Card> cards) {
        cardList.addAll(cards);
    }

    public void addCard(Card card) {
        cardList.add(card);
    }

    public void clearCards() {
        cardList.clear();
    }

    public List<Card> getCards() {
        return cardList;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getOrderProperty() {
        return orderProperty.get();
    }

    public IntegerProperty orderPropertyProperty() {
        return orderProperty;
    }

    public void setOrderProperty(int orderProperty) {
        this.orderProperty.set(orderProperty);
    }

    public String getNameProperty() {
        return nameProperty.get();
    }

    public StringProperty namePropertyProperty() {
        return nameProperty;
    }

    public void setNameProperty(String nameProperty) {
        this.nameProperty.set(nameProperty);
    }

    public boolean isDead() {
        return health.compareTo(BigInteger.ZERO) <= 0;
    }

    /**
     * Attacks a player just after a Baloney Sandwich call.
     *
     * @param player The player to attack.
     * @param size   The size of the deck.
     * @throws Exception If the player is on the same side. Will be removed if the Charm effect is implemented.
     */
    public void attack(Player player, int size) throws Exception {
        //The throwing of the Exception will be removed if the Charm effect is implemented.
        if (player.getType() == type) {
            throw new Exception("A player cannot attack another of the same side!");
        }
        BigInteger rawDamage = new BigInteger(attack.toByteArray());

        //If the damage is critical... (Chosen by a random number generator)
        double critSuccess = Math.random();
        if (critSuccess < criticalRate / (1000d + criticalRate)) {
            rawDamage = rawDamage.multiply(new BigInteger("2"));
            crit = true;
        }





        double blockSuccess = Math.random();

        //Multiply by a random number between 0.6 and 1.4

        double randomMultiplier = 0.6 + 0.8 * Math.random();
        BigInteger damage = rawDamage.multiply(new BigDecimal("" + size * 1.5).multiply(BigDecimal.valueOf(randomMultiplier)).toBigInteger())
                .subtract(defense).max(BigInteger.ONE);

        //If the damage is somehow blocked... (The blocking comes last!)
        if (blockSuccess < blockRate / (1000d + blockRate)) {
            damage = new BigDecimal(damage).divide(new BigDecimal("2"), new MathContext(8)).toBigInteger();
            blocked = true;
        }
        damageDealt.set(damageDealt.get().add(damage.min(health)));
        player.setDamageTaken(player.getDamageTaken().add(damage.min(health)));
        player.setHealth(player.getHealth().subtract(damage).max(BigInteger.ZERO));
        if (player.getHealth().compareTo(BigInteger.ZERO) == 0) {
            kills.set(kills.get() + 1);
        }
        player.setAttacked(true);
        setBaloneySandwiches(baloneySandwiches.get() + 1);
    }

    public int getCardsPut() {
        return cardsPut;
    }

    public void setCardsPut(int cardsPut) {
        this.cardsPut = cardsPut;
    }

    public void setRequestedCard(int requestedCard) {
        this.requestedCard = requestedCard;
    }

    public void resetReader() {
        reader.reset();
    }

    public void countCards(int requestedCard, int cardsPut) {
        reader.countCards(requestedCard, cardsPut);
    }

    public void countAce() {
        reader.countAce();
    }

    public Set<Integer> getStrategy() {
        return cardStrategy;
    }

    public List<Integer> getAI() {
        return AI;
    }

    public boolean hasAceOfSpades() {
        return cardList.contains(new Card("As"));
    }

    public int getCooldown() {
        return cooldown;
    }

    public Player getFocusPlayer() {
        return tauntingPlayer;
    }

    public void free() {
        tauntingPlayer = null;
    }

    public String getSkillName() {
        return classType.getSkillName();
    }

    public boolean isStunned() {
        return stunned;
    }

    public void clearStatusEffects() {
        effects.clear();
    }

    public List<StatusEffect> getStatusEffects() {
        return effects;
    }

    public boolean hasBlockedAttack() {
        return blocked;
    }

    public void setAttackBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean hasCritAttack() {
        return crit;
    }

    public void setAttackCrit(boolean crit) {
        this.crit = crit;
    }

    public boolean isAttacked() {
        return attacked;
    }

    public BigInteger getDamageTaken() {
        return damageTaken.get();
    }

    public void setDamageTaken(BigInteger damageTaken) {
        this.damageTaken.set(damageTaken);
    }

    public BigInteger getDamageDealt() {
        return damageDealt.get();
    }

    public void setDamageDealt(BigInteger damageDealt) {
        this.damageDealt.set(damageDealt);
    }

    public int getBaloneySandwiches() {
        return baloneySandwiches.get();
    }

    public void setBaloneySandwiches(int baloneySandwiches) {
        this.baloneySandwiches.set(baloneySandwiches);
    }

    public int getKills() {
        return kills.get();
    }

    public IntegerProperty killsProperty() {
        return kills;
    }

    public void setKills(int kills) {
        this.kills.set(kills);
    }

    public int getBleedingDuration() {
        if (effects.contains(StatusEffect.BLEEDING)) {
            int index = effects.indexOf(StatusEffect.BLEEDING);
            return effects.get(index).getDuration();
        }
        return 0;
    }

    public int getBurningDuration() {
        if (effects.contains(StatusEffect.BURNING)) {
            int index = effects.indexOf(StatusEffect.BURNING);
            return effects.get(index).getDuration();
        }
        return 0;
    }

    public BigInteger getBleedingDamage() {
        if (effects.contains(StatusEffect.BLEEDING)) {
            int index = effects.indexOf(StatusEffect.BLEEDING);
            return new BigDecimal(effects.get(index).getPlayer().getAttack())
                    .multiply(BigDecimal.valueOf(5.2 * effects.get(index).getDuration()))
                    .toBigInteger();
        }
        return BigInteger.ZERO;
    }
    public BigInteger getBurningDamage() {
        if (effects.contains(StatusEffect.BURNING)) {
            int index = effects.indexOf(StatusEffect.BURNING);
            StatusEffect effect = effects.get(index);
            return new BigDecimal(effect.getPlayer().getAttack()) //Can get prevented with defense.
                    .multiply(new BigDecimal(6.2))
                    .subtract(new BigDecimal(defense))
                    .multiply(BigDecimal.valueOf(effect.getDuration())).toBigInteger();
        }
        return BigInteger.ZERO;
    }


    public enum Type {USER, ENEMY;

        public Type opposite() {
            if (this == USER) {
                return ENEMY;
            } else if (this == ENEMY) {
                return USER;
            }
            return null;
        }
    }

    public enum ClassType {
        TANK, WARRIOR, MAGE, SUPPORT, ARCHER;

        public BigDecimal getHealthMultiplier() {
            switch (this) {
                case TANK:
                    return new BigDecimal(2.7);
                case WARRIOR:
                    return new BigDecimal(2.2);
                case MAGE:
                    return new BigDecimal(1.6);
                case SUPPORT:
                    return new BigDecimal(1.2);
                case ARCHER:
                    return new BigDecimal(0.8);
                default:
                    return BigDecimal.ZERO;
            }
        }

        public BigDecimal getDefenseMultiplier() {
            switch (this) {
                case TANK:
                    return new BigDecimal(1.8);
                case WARRIOR:
                    return new BigDecimal(1.5);
                case MAGE:
                    return new BigDecimal(1.2);
                case SUPPORT:
                    return new BigDecimal(0.9);
                case ARCHER:
                    return new BigDecimal(0.8);
                default:
                    return BigDecimal.ZERO;
            }
        }

        public BigDecimal getAttackMultiplier() {
            switch (this) {
                case TANK:
                    return new BigDecimal(0.8);
                case WARRIOR:
                    return new BigDecimal(0.9);
                case MAGE:
                    return new BigDecimal(1.1);
                case SUPPORT:
                    return new BigDecimal(1.5);
                case ARCHER:
                    return new BigDecimal(2.1);
                default:
                    return BigDecimal.ZERO;
            }
        }

        public int getSpeed() {
            switch (this) {
                case TANK:
                    return 123;
                case WARRIOR:
                    return 137;
                case MAGE:
                    return 149;
                case SUPPORT:
                    return 158;
                case ARCHER:
                    return 166;
                default:
                    return 0;
            }
        }

        public int getBlockRate() {
            switch (this) {
                case TANK:
                    return 227;
                case WARRIOR:
                    return 183;
                case MAGE:
                    return 162;
                case SUPPORT:
                    return 152;
                case ARCHER:
                    return 140;
                default:
                    return 0;
            }
        }

        public int getCriticalRate() {
            switch (this) {
                case TANK:
                    return 70;
                case WARRIOR:
                    return 78;
                case MAGE:
                    return 83;
                case SUPPORT:
                    return 94;
                case ARCHER:
                    return 106;
                default:
                    return 0;
            }
        }

        public String getSkillName() {
            switch (this) {
                case MAGE: return "Burning Flames";
                case TANK: return "Taunt";
                case ARCHER: return "Penetrating Arrow";
                case SUPPORT: return "Heal";
                case WARRIOR: return "Hard Hit";
            }
            return null;
        }
    }

    /**
     * Default Player Constructor
     */
    public Player() {
        attack = new BigInteger("25");
        defense = BigInteger.TEN;
        health = new BigInteger("200");
        maxHealth = new BigInteger("1000");
        level = 1;
    }

    /**
     * The standard Player constructor
     *
     * @param attack  The amount of damage the Player does. Determines how much HP
     *                can be healed if (s)he is a support.
     * @param defense The amount of damage the Player can resist in one attack.
     * @param health  This property could determine how many attacks the player can withstand
     *                before dying.
     * @param level   The number of levels in the player.
     * @param type    The class of the Player.
     */
    public Player(BigInteger attack, BigInteger defense, BigInteger health, int level, ClassType type) {
        this.attack = attack;
        this.defense = defense;
        this.health = health;
        maxHealth = new BigInteger(health.toByteArray());
        this.level = level;
        setClassType(type);
    }

    /**
     * The Player constructor
     *
     * @param level The level of the player. Determines how powerful the player is.
     */
    public Player(int level) {
        if (level < 0)
            throw new NumberFormatException("A player\'s level must not be negative!");
        attack = new BigDecimal(13).multiply(new BigDecimal(1.07).pow(level)).toBigInteger();
        defense = new BigDecimal(2).multiply(new BigDecimal(1.07).pow(level)).toBigInteger();
        maxHealth = new BigDecimal(1000).multiply(new BigDecimal(1.07).pow(level)).toBigInteger();
        this.health = new BigInteger(maxHealth.toString());
        this.level = level;
    }

    /**
     * Attacks a player. If the player has the same side, throw an Exception.
     *
     * @param player The player to attack.
     * @throws Exception If the player that is attacked is on the same side.
     */
    public void attack(Player player) throws Exception {
        if (player == null) {
            return;
        }
        //The throwing of the Exception will be removed if the Charm effect is implemented.
        if (player.getType() == type) {
            throw new Exception("A player cannot attack another of the same side!");
        }
        BigInteger health = player.getHealth();
        BigInteger rawDamage = new BigDecimal(attack.subtract(defense).max(BigInteger.ONE))
                .multiply(new BigDecimal(Math.sqrt(Math.max(8/3d * cardsPut - 5/3d, 0)))).toBigInteger();
        //If the damage is critical... (Chosen by a random number generator)
        double critSuccess = Math.random();
        if (critSuccess < criticalRate / (1000d + criticalRate)) {
            rawDamage = rawDamage.multiply(new BigInteger("2"));
        }

        BigInteger damage = rawDamage.subtract(defense).max(BigInteger.ONE);
        double blockSuccess = Math.random();
        //If the damage is somehow blocked... (The blocking comes last!)
        if (blockSuccess < blockRate / (1000d + blockRate)) {
            damage = new BigDecimal(damage).divide(new BigDecimal("2"), new MathContext(8)).toBigInteger();
        }

        //Multiply by a random number
        double randomMultiplier = 0.6 + 0.8 * Math.random();
        damage = new BigDecimal(damage).multiply(BigDecimal.valueOf(randomMultiplier)).toBigInteger();

        setDamageDealt(damageDealt.get().add(damage.min(health)));
        player.setDamageTaken(player.getDamageTaken().add(damage.min(health)));
        health = health.subtract(damage).max(BigInteger.ZERO);

        if (health.compareTo(BigInteger.ZERO) == 0) {
            kills.set(kills.get() + 1);
        }

        player.setHealth(health);
        player.setAttacked(true);
    }

    public void setAttacked(boolean attacked) {
        this.attacked = attacked;
    }

    /**
     * Heals a player. If the player that is being healed is on the other side, throw an Exception.
     *
     * @param player The player to attack.
     * @throws Exception If the player that is attacked is on the same side.
     */
    public void heal(Player player) throws Exception {
        if (player.getType() != type) {
            throw new Exception("A player cannot heal another person from a different side!");
        }
        BigInteger health = player.getHealth();
        health = health.add(new BigDecimal(attack)
                .multiply(new BigDecimal( 6 + 0.05 * Math.sqrt(level)).min(new BigDecimal(20)))
                .toBigInteger())
                .min(player.getMaxHealth());
        player.setHealth(health);
    }

    /**
     * Sets the name of the Player.
     *
     * @param name The name of the player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return The name of the player. This is equivalent to the <code>toString</code> method.
     */
    public String getName() {
        return name;
    }

    /**
     * @return The amount of damage that the player can deal in one attack. (excluding the opponent's defense)
     */
    public BigInteger getAttack() {
        return attack;
    }

    /**
     * @return The data that checks whether or not the player is a user.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return Whether or not the player is a user.
     */
    public boolean isUser() {
        return type == Type.USER;
    }

    /**
     * @return The class type of the player which determines the player's role in the game.
     */
    public ClassType getClassType() {
        return classType;
    }

    /**
     * Sets the attack to a specified
     *
     * @param attack The attack of the player.
     */
    public void setAttack(BigInteger attack) {
        this.attack = attack;
    }

    /**
     * @return The amount of damage the player can resist in one blow.
     */
    public BigInteger getDefense() {
        return defense;
    }

    /**
     * @param defense The defense of the player.
     */
    public void setDefense(BigInteger defense) {
        this.defense = defense;
    }

    /**
     * @return The health of the player.
     */
    public BigInteger getHealth() {
        return health;
    }

    /**
     * Sets the health to a specified amount.
     *
     * @param health The amount of health that the player will have.
     */
    public void setHealth(BigInteger health) {
        this.health = health;
    }

    /**
     * @return The image of the player, if it is there, or <code>null</code> otherwise
     */
    public Image getImage() {
        return image;
    }

    /**
     * Sets the image to the specified URL.
     *
     * @param url The URL that the image will use.
     */
    public void setImage(String url) {
        this.image = new Image(url, 100, 100,
                true, false);
        imageView = new ImageView(image);
    }

    /**
     * Sets the image to the specified stream.
     *
     * @param stream The stream to use for the image.
     */
    public void setImage(FileInputStream stream) {
        this.image = new Image(stream, 100, 100,
                true, false);
        imageView = new ImageView(image);
    }

    /**
     * Sets the image to the specified image.
     *
     * @param image The image to use.
     */
    public void setImage(Image image) {
        this.image = image;
        imageView.setImage(image);
    }

    /**
     * Sets the class type of the player.
     *
     * @param type The class to set the type to
     * @throws ClassCastException if an attempt was made to change the player's class.
     */
    public void setClassType(ClassType type) throws ClassCastException {
        if (classType != null) {
            throw new ClassCastException("The class of any player cannot be changed!");
        } else {
            classType = type;
            maxHealth = new BigDecimal(maxHealth).multiply(type.getHealthMultiplier()).toBigInteger();
            health = new BigDecimal(health).multiply(type.getHealthMultiplier()).toBigInteger();
            attack = new BigDecimal(attack).multiply(type.getAttackMultiplier()).toBigInteger();
            defense = new BigDecimal(defense).multiply(type.getDefenseMultiplier()).toBigInteger();
            speed = type.getSpeed();
            blockRate = type.getBlockRate();
            criticalRate = type.getCriticalRate();
        }
    }

    public String getHealthString() {
        //Number formatting
        return new ScientificNumber(health).toString();
    }

    /**
     * @return Whether or not the class type of the player is already specified.
     */
    public boolean hasClassType() {
        return classType != null;
    }

    public void setType(Type type) {
        if (type == Type.ENEMY) {
            maxHealth = new BigDecimal(1.2 - 0.2 * Math.pow(1/2d, level / 40d))
                    .multiply(new BigDecimal(maxHealth)).toBigInteger();
            health = new BigInteger(maxHealth.toByteArray());
            attack = new BigDecimal(1.3 - 0.3 * Math.pow(1/2d, level / 40d)).multiply(new BigDecimal(attack)).toBigInteger();
        }
        this.type = type;
    }

    public int getRequestedCard() {
        return requestedCard;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setImageView(ImageView imageView) {
        image = imageView.getImage();
        this.imageView = imageView;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Sets the AI to be configured from a HashMap. It reduces the HashMap to the list of integers,
     * so that the computer will be able to access their cards relatively quickly. (Not fool-proof
     * as it could be asked for the same rank twice.)
     *
     * @param AI The AI for the computers.
     */
    public void setAI(HashMap<Integer, Player> AI) {
        List<Integer> integers = new ArrayList<>();
        AI.forEach((i, p) -> {
            if (p.equals(this)) integers.add(i);
        });
        Set<Integer> set = new LinkedHashSet<>();
        List<Integer> list = new ArrayList<>();
        for (int i = 0; set.size() < 13 && i < integers.size(); i++) {
            set.add(integers.get(i) % 13 + 1);
            list.add(integers.get(i) % 13 + 1);
        }
        //If you have two cards of rank 3, split it into two distinct sessions.
        this.AI = list;
        this.cardStrategy = set;
    }

    /**
     * Determines whether or not the AI will call Baloney Sandwich on the Player.
     *
     * @param player The player to call BS on
     * @return Whether or not the AI will call Baloney Sandwich.
     */
    public boolean callBS(Player player) {
        List<Card> list = new ArrayList<>(cardList);
        list.removeIf(c -> c.getRank() != player.getRequestedCard());
        return list.size() > 1 || player.getCards().size() < 4
                || (player.getCardsPut() < 4) && list.size() + reader.get(player.getRequestedCard()) >= 4 ||
                (player.getCardsPut() == 4) && (cardList.contains(new Card(player.getRequestedCard(), 0))
                        || cardList.contains(new Card(player.getRequestedCard(), 1))
                        || cardList.contains(new Card(player.getRequestedCard(), 2))
                        || cardList.contains(new Card(player.getRequestedCard(), 3)))
                || maxHealth.divide(health).compareTo(BigInteger.valueOf(8)) >= 0;
        //Less than or equal to 12.5% HP
    }

    public boolean callBSMidgame(Player player) {
        List<Card> list = new ArrayList<>(cardList);
        list.removeIf(c -> c.getRank() != player.getRequestedCard());
        return list.size() > 2 || player.getCards().size() < 4
                || (player.getCardsPut() < 4) && list.size() + reader.get(player.getRequestedCard()) >= 4 ||
                (player.getCardsPut() == 4) && (cardList.contains(new Card(player.getRequestedCard(), 0))
                        || cardList.contains(new Card(player.getRequestedCard(), 1))
                        || cardList.contains(new Card(player.getRequestedCard(), 2))
                        || cardList.contains(new Card(player.getRequestedCard(), 3)))
                || maxHealth.divide(health).compareTo(BigInteger.valueOf(8)) >= 0;
        //Less than or equal to 12.5% HP
    }

    public boolean callBSEndgame(Player player) {
        List<Card> list = new ArrayList<>(cardList);
        list.removeIf(c -> c.getRank() != player.getRequestedCard());
        return list.size() + player.getCardsPut() > 4;
    }

    public void useSkill(Player player) {
        if (cooldown == 0) {
            if (classType == ClassType.SUPPORT) {
                try {
                    heal(player);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                cooldown += 5;
            } else if (classType == ClassType.MAGE) {
                burn(player, 2);
                addCooldown();
            } else if (classType == ClassType.ARCHER) {
                bleedAttack(player, 2);
                addCooldown();
            } else if (classType == ClassType.WARRIOR) {
                stun(player, 2);
                addCooldown();
            } else {
                new Exception("You cannot use the skill with type " + classType.toString() + " on only one enemy!").printStackTrace();
            }
        }
    }

    private void bleedAttack(Player player, int turns) {
        try {
            attack(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        int accumulatedTurns = 0;
        for (StatusEffect effect : player.getStatusEffects()) {
            if (effect.equals(StatusEffect.BLEEDING))
                accumulatedTurns += effect.getDuration();
        }
        player.effects.removeIf(e -> e.equals(StatusEffect.BLEEDING));
        StatusEffect effect = new StatusEffect(StatusEffect.BLEEDING);
        effect.setTurns(turns + accumulatedTurns);
        effect.setPlayer(this);
        player.addStatusEffect(effect);
    }

    private void stun(Player player, int turns) {
        int accumulatedTurns = 0;
        for (StatusEffect effect : player.getStatusEffects()) {
            if (effect.equals(StatusEffect.STUNNED))
                accumulatedTurns += effect.getDuration();
        }
        player.effects.removeIf(e -> e.equals(StatusEffect.STUNNED));
        StatusEffect effect = new StatusEffect(StatusEffect.STUNNED);
        effect.setTurns(turns + accumulatedTurns);
        effect.setPlayer(this);
        player.addStatusEffect(effect);
    }

    private void burn(Player player, int turns) {
        int accumulatedTurns = 0;
        for (StatusEffect effect : player.getStatusEffects()) {
            if (effect.equals(StatusEffect.BURNING))
                accumulatedTurns += effect.getDuration();
        }
        player.effects.removeIf(e -> e.equals(StatusEffect.BURNING));
        StatusEffect effect = new StatusEffect(StatusEffect.BURNING);
        effect.setTurns(turns + accumulatedTurns);
        effect.setPlayer(this);
        player.addStatusEffect(effect);
    }

    public void updateStatusEffects() {
        stunned = false;
        setFocus(null);
        for (StatusEffect effect : effects) {
            applyEffect(effect);
        }
        effects.removeIf(e -> e.getDuration() <= 0);
    }

    private void applyEffect(StatusEffect effect) {
        BigInteger damage;
        switch (effect.getName()) {
            case "Burning":
                damage = new BigDecimal(effect.getPlayer().getAttack()) //Can get prevented with defense.
                        .multiply(new BigDecimal(6.2)).subtract(new BigDecimal(defense)).toBigInteger();
                setHealth(getHealth().subtract(damage).max(BigInteger.ZERO));
                if (health.compareTo(BigInteger.ZERO) == 0) {
                    System.out.println(this + " is killed by the burning effect caused by " + effect.getPlayer() + ".");
                    effect.getPlayer().setKills(kills.get() + 1);
                }
                break;
            case "Bleeding":
                damage =
                        new BigDecimal(effect.getPlayer().getAttack()) //Does not get prevented with defense.
                                .multiply(new BigDecimal(5.2)).toBigInteger();
                setHealth(getHealth().subtract(damage).max(BigInteger.ZERO));
                if (health.compareTo(BigInteger.ZERO) == 0) {
                    effect.getPlayer().setKills(kills.get() + 1);
                    System.out.println(this + " is killed by the bleeding effect caused by " + effect.getPlayer() + ".");
                }
                break;
            case "Stunned":
                stunned = true;
                break;
            case "Taunted":
                setFocus(effect.getPlayer());
                break;
        }
        effect.decreaseDuration();
    }

    private void addStatusEffect(StatusEffect effect) {
        effects.add(effect);
    }

    private void addCooldown() {
        cooldown += 10;
    }

    public void useSkill(List<Player> players) {
        if (classType == ClassType.TANK && cooldown == 0) {
            //Taunts enemies.
            for (Player player : players) {
                taunt(player, 3);
            }
            addCooldown();
        } else if (cooldown == 0) {
            new Exception("You cannot use the skill with type " + classType.toString()
                    + " on multiple enemies!").printStackTrace();
        }
    }

    private void taunt(Player player, int turns) {
        player.effects.removeIf(e -> e.equals(StatusEffect.TAUNTED));
        StatusEffect effect = new StatusEffect(StatusEffect.TAUNTED);
        effect.setTurns(turns);
        effect.setPlayer(this);
        player.addStatusEffect(effect);
    }

    public void setFocus(Player tauntingPlayer) {
        if (tauntingPlayer != null && !tauntingPlayer.isDead()) {
            this.tauntingPlayer = tauntingPlayer;
            return;
        }
        this.tauntingPlayer = null;
    }
}