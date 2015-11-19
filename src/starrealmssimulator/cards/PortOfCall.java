package starrealmssimulator.cards;

import starrealmssimulator.model.*;

public class PortOfCall extends Outpost implements ScrapableCard
{
    public PortOfCall()
    {
        name = "Port Of Call";
        faction = Faction.TRADE_FEDERATION;
        cost = 6;
        set = CardSet.CORE;
        shield = 6;
        text = "Add 3 Trade; Scrap: Draw a card. You may destroy target base";
    }

    @Override
    public void cardPlayed(Player player) {
        player.addTrade(3);
    }

    @Override
    public void baseUsed(Player player) {

    }

    @Override
    public void cardScraped(Player player) {
        player.drawCard();
        player.destroyTargetBase();
    }
}