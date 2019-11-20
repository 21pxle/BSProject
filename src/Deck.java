import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private List<Card> cards = new ArrayList<>();
    private final String suits = "cdhs";
    private final String ranks = "A23456789TJQK";

    public Deck() {
        for (int i = 0; i < 13; i++) {
            for (int i1 = 0; i1 < 4; i1++)
            cards.add(new Card("" + ranks.charAt(i) + suits.charAt(i1)));
        }
        Collections.shuffle(cards);
    }

    public List<Card> draw(int numCards) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < numCards; i++) {
            cards.add(draw());
        }
        return cards;
    }

    public Card draw() {
        return cards.remove(0);
    }

    public int size() {
        return cards.size();
    }
}
