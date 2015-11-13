package starrealmssimulator.cards;

import starrealmssimulator.model.*;

public class BlobWheel extends Base implements ScrapableCard
{
    public BlobWheel()
    {
        name = "Blob Wheel";
        faction = Faction.BLOB;
        cost = 3;
        set = CardSet.CORE;
        shield = 5;
        text = "Add 1 Combat; Scrap: Add 3 Trade";
    }

    @Override
    public void cardPlayed(Player player)
    {
        player.addCombat(1);
    }

    @Override
    public void baseUsed(Player player) {

    }

    @Override
    public void cardScraped(Player player)
    {
        player.addTrade(3);
    }

    @Override
    public int getTradeWhenScrapped() {
        return 3;
    }
}
