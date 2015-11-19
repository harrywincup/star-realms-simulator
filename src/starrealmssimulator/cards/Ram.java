package starrealmssimulator.cards;

import starrealmssimulator.model.*;

public class Ram extends Ship implements ScrapableCard, AlliableCard
{
    public Ram()
    {
        name = "Ram";

        faction = Faction.BLOB;
        cost = 3;
        set = CardSet.CORE;
        text = "Add 5 Combat; Ally: Add 2 Combat; Scrap: Add 3 Trade";
    }

    @Override
    public void cardPlayed(Player player) {
        player.addCombat(5);
    }

    @Override
    public void cardAllied(Player player) {
        player.addCombat(2);
    }

    @Override
    public void cardScraped(Player player) {
        player.addTrade(3);
    }

    @Override
    public int getTradeWhenScrapped() {
        return 3;
    }
}