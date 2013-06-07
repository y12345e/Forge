package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import forge.Card;
import forge.Constant;
import forge.card.CardType;
import forge.card.MagicColor;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class ChangeTextEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.ability.SpellAbilityEffect#resolve(forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        String[] typeKind = sa.getParam("TypeKinds").split(",");
        List<String> validTypes = new ArrayList<String>();
        
        String type = sa.getActivatingPlayer().getController().chooseSomeType("Type", "", Arrays.asList(typeKind), new ArrayList<String>());
        
        if (type.equals("Card")) {
            validTypes.addAll(Constant.CardTypes.CARD_TYPES);
        } else if (type.equals("Creature")) {
            validTypes.addAll(CardType.getCreatureTypes());
        } else if (type.equals("Basic Land")) {
            validTypes.addAll(CardType.getBasicTypes());
        } else if (type.equals("Land")) {
            validTypes.addAll(CardType.getLandTypes());
        } else if (type.equals("Color")) {
            for(byte b : MagicColor.WUBRG) {
                String l = MagicColor.toLongString(b);
                validTypes.add(l.substring(0,1).toUpperCase() + l.substring(1));
            }
        }
        
        String chosenFirst = sa.getActivatingPlayer().getController().chooseSomeType(type, "", validTypes, new ArrayList<String>());
        String chosenSecond = sa.getActivatingPlayer().getController().chooseSomeType(type, "", validTypes, new ArrayList<String>());
        
        for(Card c : sa.getTarget().getTargetCards()) {
            c.addTextReplacement(chosenFirst, chosenSecond);
            c.addTextReplacement(chosenFirst.toLowerCase(),chosenSecond.toLowerCase());
        }
    }

}
