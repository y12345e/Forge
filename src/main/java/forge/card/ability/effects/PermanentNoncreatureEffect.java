package forge.card.ability.effects;

import forge.Card;
import forge.Singletons;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.zone.ZoneType;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public class PermanentNoncreatureEffect extends SpellAbilityEffect {

    /* (non-Javadoc)
     * @see forge.card.abilityfactory.SpellEffect#resolve(forge.card.spellability.SpellAbility)
     */
    @Override
    public void resolve(SpellAbility sa) {
        sa.getSourceCard().setController(sa.getActivatingPlayer(), 0);
        final Card c = Singletons.getModel().getGame().getAction().moveTo(sa.getActivatingPlayer().getZone(ZoneType.Battlefield), sa.getSourceCard());
        sa.setSourceCard(c);
    }

    @Override
    public String getStackDescription(final SpellAbility sa) {
        final Card sourceCard = sa.getSourceCard();
        final StringBuilder sb = new StringBuilder();
        sb.append(sourceCard.getName());
        return sb.toString();
    }
}
