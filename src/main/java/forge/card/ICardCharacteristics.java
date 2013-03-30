package forge.card;

import forge.card.mana.ManaCost;

public interface ICardCharacteristics {
    String   getName();
    CardType getType();
    ManaCost getManaCost();
    ColorSet getColor();
    ColorSet getColorIdentity();

    int    getIntPower();
    int    getIntToughness();
    String getPower();
    String getToughness();
    int    getInitialLoyalty();

    String getOracleText();
}
