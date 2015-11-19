package starrealmssimulator.model;

import starrealmssimulator.cards.Explorer;
import starrealmssimulator.cards.FleetHQ;
import starrealmssimulator.cards.MechWorld;
import starrealmssimulator.cards.StealthNeedle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public abstract class Player {
    private int authority = 50;
    private List<Card> deck = new ArrayList<>();
    private List<Card> hand = new ArrayList<>();
    private List<Card> discard = new ArrayList<>();
    private List<Card> played = new ArrayList<>();
    private List<Card> inPlay = new ArrayList<>();
    private List<Base> bases = new ArrayList<>();

    private int combat;
    private int trade;

    private Game game;
    private Player opponent;

    private boolean nextShipToTop;

    private boolean allShipsAddOneCombat;

    private boolean allFactionsAllied;

    private int shuffles;

    private boolean firstPlayer;

    protected Comparator<Base> baseShieldAscending = (b1, b2) -> Integer.compare(b1.getShield(), b2.getShield());
    protected Comparator<Base> baseShieldDescending = baseShieldAscending.reversed();

    protected Player() {
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public Player getOpponent() {
        return opponent;
    }

    public void setOpponent(Player opponent) {
        this.opponent = opponent;
    }

    public int getAuthority() {
        return authority;
    }

    public List<Card> getDeck() {
        return deck;
    }

    public void setDeck(List<Card> deck) {
        this.deck = deck;
    }

    public int getCombat() {
        return combat;
    }

    public void setCombat(int combat) {
        this.combat = combat;
    }

    public int getTrade() {
        return trade;
    }

    public void setTrade(int trade) {
        this.trade = trade;
    }

    public List<Card> getHand() {
        return hand;
    }

    public List<Card> getDiscard() {
        return discard;
    }

    public List<Card> getPlayed() {
        return played;
    }

    public List<Card> getInPlay() {
        return inPlay;
    }

    public List<Base> getBases() {
        return bases;
    }

    public abstract String getPlayerName();

    public List<Outpost> getOutposts() {
        return bases.stream().filter(Card::isOutpost).map(base -> (Outpost) base).collect(Collectors.toList());
    }

    public int getShuffles() {
        return shuffles;
    }

    public void drawCard() {
        drawCards(1);
    }

    public boolean isFirstPlayer() {
        return firstPlayer;
    }

    public void setFirstPlayer(boolean firstPlayer) {
        this.firstPlayer = firstPlayer;
    }

    public void drawCards(int cards) {
        getGame().gameLog("Drawing " + cards + " cards");
        for (int i = 0; i < cards; i++) {
            if (deck.isEmpty()) {
                deck.addAll(discard);
                discard.clear();
                getGame().gameLog("Shuffling deck");
                Collections.shuffle(deck);
                shuffles++;
            }

            if (!deck.isEmpty()) {
                Card cardToDraw = deck.remove(0);
                hand.add(cardToDraw);
                getGame().gameLog("Added " + cardToDraw.getName() + " to hand");
            }
        }
    }

    public void discardCard(Card card) {
        getGame().gameLog("Discarded " + card.getName());
        hand.remove(card);
        discard.add(card);
        cardRemovedFromPlay(card);
    }

    public void opponentDiscardsCard() {
        getGame().gameLog("Opponent discarding card");
        opponent.discardCards(1, false);
    }

    private void cardRemovedFromPlay(Card card) {
        card.setAlliedAbilityUsed(false);
        if (card instanceof FleetHQ) {
            allShipsAddOneCombat = false;
        }
        if (card instanceof MechWorld) {
            allFactionsAllied = false;
        }
        if (card instanceof StealthNeedle) {
            ((StealthNeedle) card).setCardBeingCopied(null);
        }
        if (card.isBase()) {
            ((Base) card).setUsed(false);
        }
    }

    public abstract int discardCards(int cards, boolean optional);

    public void addTrade(int trade) {
        this.trade += trade;
    }

    public void addCombat(int combat) {
        this.combat += combat;
    }

    public void addAuthority(int authority) {
        this.authority += authority;
    }

    public void endTurn() {
        getGame().gameLog("Ending turn");

        combat = 0;
        trade = 0;

        played.clear();

        for (Card card : inPlay) {
            if (card.isBase()) {
                ((Base) card).setUsed(false);
                card.setAlliedAbilityUsed(false);
            } else {
                discard.add(card);
                cardRemovedFromPlay(card);
            }
        }

        inPlay.clear();

        discard.addAll(hand);
        hand.clear();

        drawCards(5);

        game.turnEnded();
    }

    public void addBase(Base base) {
        this.getBases().add(base);
    }

    public void addOutpost(Outpost outpost) {
        this.getOutposts().add(outpost);
    }

    public void destroyTargetBase() {
        if (!opponent.getBases().isEmpty()) {
            Base base = chooseTargetBaseToDestroy();
            if (base != null) {
                opponent.baseDestroyed(base);
            }
        }
    }

    public void baseDestroyed(Base base) {
        getGame().gameLog("Destroyed base: " + base.getName());
        if (base instanceof FleetHQ) {
            allShipsAddOneCombat = false;
        }
        if (base instanceof MechWorld) {
            allFactionsAllied = false;
        }
        bases.remove(base);
        discard.add(base);
        cardRemovedFromPlay(base);
    }

    public List<Base> getUnusedBasesAndOutposts() {
        List<Base> unusedBases = new ArrayList<>();

        for (Base base : bases) {
            if (!base.isUsed()) {
                unusedBases.add(base);
            }
        }

        return unusedBases;
    }

    public int getSmallestBaseShield() {
        if (!getBases().isEmpty()) {
            List<Base> sortedBases = getBases().stream().sorted(baseShieldAscending).collect(toList());
            return sortedBases.get(0).getShield();
        }
        return 0;
    }

    public int getSmallestOutpostShield() {
        if (!getOutposts().isEmpty()) {
            List<Outpost> sortedOutposts = getOutposts().stream().sorted(baseShieldAscending).collect(toList());
            return sortedOutposts.get(0).getShield();
        }
        return 0;
    }

    public int getBiggestBaseShield() {
        if (!getBases().isEmpty()) {
            List<Base> sortedBases = getBases().stream().sorted(baseShieldDescending).collect(toList());
            return sortedBases.get(0).getShield();
        }
        return 0;
    }

    public int getBiggestOutpostShield() {
        if (!getOutposts().isEmpty()) {
            List<Outpost> sortedOutposts = getOutposts().stream().sorted(baseShieldDescending).collect(toList());
            return sortedOutposts.get(0).getShield();
        }
        return 0;
    }

    public abstract Base chooseTargetBaseToDestroy();

    public void scrapCard() {
        scrapCards(1);
    }

    public int scrapCards(int cards) {
        List<List<Card>> cardsToScrap = getCardsToOptionallyScrapFromDiscardOrHand(cards);

        List<Card> cardsToScrapFromDiscard = cardsToScrap.get(0);
        List<Card> cardsToScrapFromHand = cardsToScrap.get(1);

        for (Card card : cardsToScrapFromDiscard) {
            scrapCardFromDiscard(card);
        }

        for (Card card : cardsToScrapFromHand) {
            scrapCardFromHand(card);
        }

        return cardsToScrap.size();
    }

    private void scrapCardFromDiscard(Card card) {
        getGame().gameLog("Scrapped " + card.getName() + " from discard");
        discard.remove(card);
        playerCardScrapped(card);
    }

    private void scrapCardFromHand(Card card) {
        getGame().gameLog("Scrapped " + card.getName() + " from hand");
        hand.remove(card);
        playerCardScrapped(card);
    }

    private void playerCardScrapped(Card card) {
        if (card instanceof FleetHQ) {
            allShipsAddOneCombat = false;
        }
        if (card instanceof MechWorld) {
            allFactionsAllied = false;
        }
        game.getScrapped().add(card);
    }

    public void scrapCardInPlayForBenefit(Card card) {
        if (card.isScrapable()) {
            getGame().gameLog("Scrapped " + card.getName() + " from in play for benefit");
            inPlay.remove(card);
            if (card.isBase()) {
                bases.remove(card);
            }
            playerCardScrapped(card);
            ((ScrapableCard) card).cardScraped(this);
        }
    }

    public abstract List<List<Card>> getCardsToOptionallyScrapFromDiscardOrHand(int cards);

    public void scrapCardInTradeRow() {
        Card card = chooseCardToScrapInTradeRow();
        if (card != null) {
            getGame().gameLog("Scrapped " + card.getName() + " from trade row");
            game.getTradeRow().remove(card);
            game.getScrapped().add(card);
        }
    }

    public abstract Card chooseCardToScrapInTradeRow();

    public void acquireFreeShipAndPutOnTopOfDeck() {
        if (!getGame().getTradeRow().isEmpty()) {
            Card card = chooseFreeShipToPutOnTopOfDeck();
            if (card != null) {
                getGame().gameLog("Acquired free ship on top of deck: " + card.getName());
                deck.add(0, card);
            }
        }
    }

    private void cardAcquired(Card card) {
        if (card.isShip() && nextShipToTop) {
            nextShipToTop = false;
            deck.add(0, card);
        } else {
            discard.add(card);
        }
    }

    public abstract Card chooseFreeShipToPutOnTopOfDeck();

    public abstract void makeChoice(Card card, Choice... choices);

    public void nextShipToTop() {
        nextShipToTop = true;
    }

    public void allShipsGet1Combat() {
        allShipsAddOneCombat = true;
    }

    public void scrapCardFromHand() {
        if (!hand.isEmpty()) {
            Card card = getCardToScrapFromHand();
            if (card != null) {
                scrapCardFromHand(card);
            }
        }
    }

    public abstract Card getCardToScrapFromHand();

    public void allFactionsAllied() {
        allFactionsAllied = true;
    }

    public abstract void discardAndDrawCards(int cards);

    public void copyShip(StealthNeedle stealthNeedle) {
        if (!inPlay.isEmpty()) {
            Ship shipToCopy = getShipToCopy();
            if (shipToCopy != null) {
                getGame().gameLog("Copying ship: " + shipToCopy.getName());
                try {
                    Card shipToCopyCopy = shipToCopy.getClass().newInstance();
                    stealthNeedle.setCardBeingCopied(shipToCopyCopy);
                    this.playCard(shipToCopyCopy);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public abstract Ship getShipToCopy();

    public abstract void takeTurn();

    public void buyCard(Card card) {
        if (trade >= card.getCost()) {
            getGame().gameLog("Bought card: " + card.getName());
            trade -= card.getCost();
            if (card instanceof Explorer) {
                cardAcquired(new Explorer());
            } else {
                game.getTradeRow().remove(card);
                game.addCardToTradeRow();
                cardAcquired(card);
            }
        }
    }

    public void destroyOpponentBase(Base base) {
        combat -= base.getShield();

        opponent.baseDestroyed(base);
    }

    public void reduceAuthority(int combat) {
        authority -= combat;
    }

    public void attackOpponentWithRemainingCombat() {
        getGame().gameLog("Applied " + combat + " combat to opponent");
        opponent.reduceAuthority(combat);
        combat = 0;
    }

    public List<Card> getAllCards() {
        List<Card> cards = new ArrayList<>();
        cards.addAll(hand);
        cards.addAll(deck);
        cards.addAll(discard);
        cards.addAll(inPlay);

        return cards;
    }

    public int getNumShips() {
        int ships = 0;

        for (Card card : getAllCards()) {
            if (card instanceof Ship) {
                ships++;
            }
        }

        return ships;
    }

    public int getNumBases() {
        int bases = 0;

        for (Card card : getAllCards()) {
            if (card instanceof Base) {
                bases++;
            }
        }

        return bases;
    }

    public int getNumOutposts() {
        int outposts = 0;

        for (Card card : getAllCards()) {
            if (card instanceof Outpost) {
                outposts++;
            }
        }

        return outposts;
    }

    public boolean useAlliedAbility(AlliableCard card) {
        Card cardToUse = (Card) card;

        if (card instanceof StealthNeedle && ((StealthNeedle) card).getCardBeingCopied() != null) {
            cardToUse = ((StealthNeedle) card).getCardBeingCopied();
        }
        if (!cardToUse.isAlliedAbilityUsed() && cardHasAlly(cardToUse)) {
            getGame().gameLog("Using allied ability of " + cardToUse.getName());
            cardToUse.setAlliedAbilityUsed(true);
            ((AlliableCard) cardToUse).cardAllied(this);
            return true;
        }

        return false;
    }

    public boolean cardHasAlly(Card card) {
        if (allFactionsAllied) {
            game.gameLog("All factions allied");
            return true;
        }

        for (Card c : inPlay) {
            if (c != card && c.isAlly(card)) {
                return true;
            }
        }

        return false;
    }

    public void playCard(Card card) {
        game.gameLog("Played card: " + card.getName());
        played.add(card);
        inPlay.add(card);
        hand.remove(card);

        if (card.isBase()) {
            addBase((Base) card);
        }

        if (card.isShip() && allShipsAddOneCombat) {
            addCombat(1);
        }

        card.cardPlayed(this);
    }

    public int countCardsByType(List<Card> cards, Function<Card, Boolean> typeMatcher) {
        return (int) cards.stream().filter(typeMatcher::apply).count();
    }
}