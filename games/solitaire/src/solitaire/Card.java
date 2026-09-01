package solitaire;

public record Card(Suit suit, Rank rank, boolean faceUp) {

    public enum Suit {
        HEARTS("\u2665", java.awt.Color.RED),
        DIAMONDS("\u2666", java.awt.Color.RED),
        CLUBS("\u2663", java.awt.Color.BLACK),
        SPADES("\u2660", java.awt.Color.BLACK);

        public final String symbol;
        public final java.awt.Color color;

        Suit(String symbol, java.awt.Color color) {
            this.symbol = symbol;
            this.color = color;
        }
    }

    public enum Rank {
        ACE(1, "A"), TWO(2, "2"), THREE(3, "3"), FOUR(4, "4"), FIVE(5, "5"),
        SIX(6, "6"), SEVEN(7, "7"), EIGHT(8, "8"), NINE(9, "9"), TEN(10, "10"),
        JACK(11, "J"), QUEEN(12, "Q"), KING(13, "K");

        public final int value;
        public final String symbol;

        Rank(int value, String symbol) {
            this.value = value;
            this.symbol = symbol;
        }
    }

    public Card flip() {
        return new Card(suit, rank, !faceUp);
    }

    public boolean isRed() {
        return suit == Suit.HEARTS || suit == Suit.DIAMONDS;
    }

    public boolean isBlack() {
        return !isRed();
    }

    @Override
    public String toString() {
        return rank.symbol + suit.symbol;
    }
}
