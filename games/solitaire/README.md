# Java 25 Solitaire

A classic Klondike solitaire game with a Swing GUI, built for Java 25.

## Build

Requires JDK 25+. Compiles with `--release 25` on newer JDKs.

```
javac -d out src/solitaire/*.java
```

## Run

```
java -cp out solitaire.Main
```

## How to play

- **Stock** (top-left): click to deal 3 cards to the waste.
- **Waste**: click card to auto-move to a foundation; drag to tableau.
- **Tableau**: build descending alternating colors. Only kings go on empty piles. Drag a face-up run of cards.
- **Foundations** (top-right): build ascending by suit from Ace. Click a face-up top card to snap it up.
- **Keys**: `N` new game, `A` auto-move eligible cards to foundations, `D` deal from stock.
- Win by moving all 52 cards to the foundations.
