public class CardReader {

    private int[] cards = new int[13];

    public int get(int card) {
        return cards[card - 1];
    }

    public void countAce() {
        cards[0]++;
    }

    public void reset() {
        cards = new int[13];
    }

    public void countCards(int card, int cardCount) {
        if (cardCount > 1)
            cards[card] += cardCount;
    }
}
