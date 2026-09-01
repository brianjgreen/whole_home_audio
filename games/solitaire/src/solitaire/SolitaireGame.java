package solitaire;

import java.util.ArrayList;
import java.util.List;

public class SolitaireGame {
    public static final int TABLEAU_SIZE = 7;
    public static final int FOUNDATION_SIZE = 4;

    private final Deck stock = new Deck();
    private final List<Card> waste = new ArrayList<>();
    private final List<List<Card>> tableau = new ArrayList<>();
    private final List<List<Card>> foundations = new ArrayList<>();
    private int score = 0;
    private int moves = 0;

    public SolitaireGame() {
        for (int i = 0; i < TABLEAU_SIZE; i++) tableau.add(new ArrayList<>());
        for (int i = 0; i < FOUNDATION_SIZE; i++) foundations.add(new ArrayList<>());
        deal();
    }

    private void deal() {
        stock.shuffle();
        for (int col = 0; col < TABLEAU_SIZE; col++) {
            for (int row = 0; row <= col; row++) {
                var card = stock.deal();
                if (card != null) {
                    tableau.get(col).add(row == col ? card.flip() : card);
                }
            }
        }
    }

    public boolean canPlaceOnFoundation(Card card, int foundationIndex) {
        var pile = foundations.get(foundationIndex);
        if (pile.isEmpty()) return card.rank() == Card.Rank.ACE;
        return pile.getLast().suit() == card.suit() && pile.getLast().rank().value + 1 == card.rank().value;
    }

    public boolean canPlaceOnTableau(Card card, int tableauIndex) {
        var pile = tableau.get(tableauIndex);
        if (pile.isEmpty()) return card.rank() == Card.Rank.KING;
        var top = pile.getLast();
        return top.faceUp() && top.isRed() != card.isRed() && top.rank().value - 1 == card.rank().value;
    }

    public boolean moveToFoundation(int tableauIndex, int cardPosition) {
        var pile = tableau.get(tableauIndex);
        if (cardPosition < 0 || cardPosition >= pile.size()) return false;
        var card = pile.get(cardPosition);
        if (!card.faceUp()) return false;
        if (cardPosition != pile.size() - 1) return false;

        for (int f = 0; f < FOUNDATION_SIZE; f++) {
            if (canPlaceOnFoundation(card, f)) {
                pile.removeLast();
                foundations.get(f).add(card);
                flipTopCard(tableauIndex);
                score += 10;
                moves++;
                return true;
            }
        }
        return false;
    }

    public boolean moveToTableau(int fromTableau, int cardPosition, int toTableau) {
        if (fromTableau == toTableau) return false;
        var fromPile = tableau.get(fromTableau);
        if (cardPosition < 0 || cardPosition >= fromPile.size()) return false;
        if (!fromPile.get(cardPosition).faceUp()) return false;

        var movingCards = new ArrayList<>(fromPile.subList(cardPosition, fromPile.size()));
        var targetCard = movingCards.getFirst();

        if (canPlaceOnTableau(targetCard, toTableau)) {
            fromPile.subList(cardPosition, fromPile.size()).clear();
            tableau.get(toTableau).addAll(movingCards);
            flipTopCard(fromTableau);
            moves++;
            return true;
        }
        return false;
    }

    public boolean moveToFoundationFromTableau(int tableauIndex) {
        var pile = tableau.get(tableauIndex);
        if (pile.isEmpty()) return false;
        return moveToFoundation(tableauIndex, pile.size() - 1);
    }

    public boolean wasteToFoundation() {
        if (waste.isEmpty()) return false;
        var card = waste.getLast();
        for (int f = 0; f < FOUNDATION_SIZE; f++) {
            if (canPlaceOnFoundation(card, f)) {
                waste.removeLast();
                foundations.get(f).add(card);
                score += 10;
                moves++;
                return true;
            }
        }
        return false;
    }

    public boolean wasteToTableau(int tableauIndex) {
        if (waste.isEmpty()) return false;
        var card = waste.getLast();
        if (canPlaceOnTableau(card, tableauIndex)) {
            waste.removeLast();
            tableau.get(tableauIndex).add(card);
            moves++;
            return true;
        }
        return false;
    }

    public void dealFromStock() {
        if (stock.isEmpty()) {
            var cards = new ArrayList<>(waste.reversed());
            waste.clear();
            for (var card : cards) {
                stock.addBottom(card.flip());
            }
        }
        if (!stock.isEmpty()) {
            var card = stock.deal();
            if (card != null) waste.add(card.flip());
        }
    }

    public boolean autoMoveToFoundation() {
        boolean moved = false;
        for (int t = 0; t < TABLEAU_SIZE; t++) {
            var pile = tableau.get(t);
            if (!pile.isEmpty() && pile.getLast().faceUp()) {
                if (moveToFoundationFromTableau(t)) moved = true;
            }
        }
        if (!waste.isEmpty()) {
            if (wasteToFoundation()) moved = true;
        }
        return moved;
    }

    private void flipTopCard(int tableauIndex) {
        var pile = tableau.get(tableauIndex);
        if (!pile.isEmpty() && !pile.getLast().faceUp()) {
            var card = pile.removeLast();
            pile.addLast(card.flip());
            score += 5;
        }
    }

    public boolean isWon() {
        return foundations.stream().allMatch(pile -> pile.size() == 13);
    }

    public List<Card> getPileTopCards(int tableauIndex) {
        return List.copyOf(tableau.get(tableauIndex));
    }

    public Deck getStock() { return stock; }
    public List<Card> getWaste() { return waste; }
    public List<List<Card>> getTableau() { return tableau; }
    public List<List<Card>> getFoundations() { return foundations; }
    public int getScore() { return score; }
    public int getMoves() { return moves; }

    public void newGame() {
        stock.drain();
        waste.clear();
        foundations.forEach(List::clear);
        tableau.forEach(List::clear);
        score = 0;
        moves = 0;
        repopulate();
        deal();
    }

    private void repopulate() {
        for (var suit : Card.Suit.values()) {
            for (var rank : Card.Rank.values()) {
                stock.addBottom(new Card(suit, rank, false));
            }
        }
    }
}
