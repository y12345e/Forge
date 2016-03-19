package forge.planarconquest;

import java.util.ArrayList;
import java.util.List;
import forge.card.CardRarity;
import forge.item.PaperCard;
import forge.model.FModel;
import forge.planarconquest.ConquestPreferences.CQPref;
import forge.util.Aggregates;

public class ConquestAwardPool {
    private final BoosterPool commons, uncommons, rares, mythics;

    public ConquestAwardPool(Iterable<PaperCard> cards) {
        ConquestPreferences prefs = FModel.getConquestPreferences();

        commons = new BoosterPool();
        uncommons = new BoosterPool();
        rares = new BoosterPool();
        mythics = new BoosterPool();

        for (PaperCard c : cards) {
            switch (c.getRarity()) {
            case Common:
                commons.add(c);
                break;
            case Uncommon:
                uncommons.add(c);
                break;
            case Rare:
            case Special: //lump special cards in with rares for simplicity
                rares.add(c);
                break;
            case MythicRare:
                mythics.add(c);
                break;
            default:
                break;
            }
        }

        //calculate odds of each rarity
        float commonOdds = commons.getOdds(prefs.getPrefInt(CQPref.BOOSTER_COMMONS));
        float uncommonOdds = uncommons.getOdds(prefs.getPrefInt(CQPref.BOOSTER_UNCOMMONS));
        int raresPerBooster = prefs.getPrefInt(CQPref.BOOSTER_RARES);
        float rareOdds = rares.getOdds(raresPerBooster);
        float mythicOdds = mythics.getOdds((float)raresPerBooster / (float)prefs.getPrefInt(CQPref.BOOSTERS_PER_MYTHIC));

        //determine multipliers for each rarity based on ratio of odds
        commons.multiplier = 1;
        uncommons.multiplier = commonOdds / uncommonOdds;
        rares.multiplier = commonOdds / rareOdds;
        mythics.multiplier = mythics.isEmpty() ? 0 : commonOdds / mythicOdds;
    }

    public int getShardValue(CardRarity rarity, int baseValue) {
        switch (rarity) {
        case Common:
            return baseValue;
        case Uncommon:
            return Math.round(baseValue * uncommons.multiplier);
        case Rare:
        case Special:
            return Math.round(baseValue * rares.multiplier);
        case MythicRare:
            return Math.round(baseValue * mythics.multiplier);
        default:
            return 0;
        }
    }

    public BoosterPool getCommons() {
        return commons;
    }
    public BoosterPool getUncommons() {
        return uncommons;
    }
    public BoosterPool getRares() {
        return rares;
    }
    public BoosterPool getMythics() {
        return mythics;
    }

    public class BoosterPool {
        private final List<PaperCard> cards = new ArrayList<PaperCard>();
        private float multiplier;

        private BoosterPool() {
        }

        public boolean isEmpty() {
            return cards.isEmpty();
        }

        private float getOdds(float perBoosterCount) {
            int count = cards.size();
            if (count == 0) { return 0; }
            return (float)perBoosterCount / (float)count;
        }

        private void add(PaperCard c) {
            cards.add(c);
        }

        public void rewardCard(List<PaperCard> rewards) {
            int index = Aggregates.randomInt(0, cards.size() - 1);
            PaperCard c = cards.get(index);
            cards.remove(index);
            rewards.add(c);
        }
    }
}