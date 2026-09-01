package solitaire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Deck {
    private final List<Card> cards = new ArrayList<>();

    public Deck() {
        for (var suit : Card.Suit.values()) {
            for (var rank : Card.Rank.values()) {
                cards.add(new Card(suit, rank, false));
            }
        }
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card deal() {
        return cards.isEmpty() ? null : cards.removeLast();
    }

    public void addBottom(Card card) {
        cards.addFirst(card);
    }

    public Card peek() {
        return cards.isEmpty() ? null : cards.getLast();
    }

    public int size() {
        return cards.size();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public List<Card> drain() {
        var drained = new ArrayList<>(cards);
        cards.clear();
        return drained;
    }
}
