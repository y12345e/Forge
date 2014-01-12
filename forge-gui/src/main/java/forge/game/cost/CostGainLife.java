/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.game.cost;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import forge.game.ability.AbilityUtils;
import forge.game.card.Card;
import forge.game.player.Player;
import forge.game.spellability.SpellAbility;
import forge.gui.GuiChoose;

/**
 * The Class CostGainLife.
 */
public class CostGainLife extends CostPart {
    private final int cntPlayers; // MAX_VALUE means ALL/EACH PLAYERS

    /**
     * Instantiates a new cost gain life.
     * 
     * @param amount
     *            the amount
     */
    public CostGainLife(final String amount, String playerSelector, int qty) {
        super(amount, playerSelector, null);
        cntPlayers = qty;
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#toString()
     */
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Have an opponent gain ").append(this.getAmount()).append(" life");
        return sb.toString();
    }
    
    public List<Player> getPotentialTargets(final Player payer, final Card source)
    {
        List<Player> res = new ArrayList<Player>();
        for(Player p : payer.getGame().getPlayers())
        {
            if(p.isValid(getType(), payer, source))
                res.add(p);
        }
        return res;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#canPay(forge.card.spellability.SpellAbility,
     * forge.Card, forge.Player, forge.card.cost.Cost)
     */
    @Override
    public final boolean canPay(final SpellAbility ability) {
        final Integer amount = this.convertAmount();
        if ( amount == null ) return false;

        int cntAbleToGainLife = 0;
        List<Player> possibleTargets = getPotentialTargets(ability.getActivatingPlayer(), ability.getSourceCard());

        for (final Player opp : possibleTargets) {
            if (opp.canGainLife()) {
                cntAbleToGainLife++;
            }
        }

        return cntAbleToGainLife >= cntPlayers || cntPlayers == Integer.MAX_VALUE && cntAbleToGainLife == possibleTargets.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see forge.card.cost.CostPart#payAI(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final boolean payAsDecided(final Player ai, final PaymentDecision decision, SpellAbility ability) {
        Integer c = this.convertAmount();
        
        int playersLeft = cntPlayers;
        for (final Player opp : decision.players) {
            if (playersLeft == 0)
                break;

            if (!opp.canGainLife()) // you should not have added him to decision.
                return false;

            playersLeft--;
            opp.gainLife(c, null);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * forge.card.cost.CostPart#payHuman(forge.card.spellability.SpellAbility,
     * forge.Card, forge.card.cost.Cost_Payment)
     */
    @Override
    public final PaymentDecision payHuman(final SpellAbility ability, final Player activator) {
        final Card source = ability.getSourceCard();
        final String amount = this.getAmount();

        final int life = activator.getLife();

        Integer c = this.convertAmount();
        if (c == null) {
            final String sVar = ability.getSVar(amount);
            // Generalize this
            if (sVar.equals("XChoice")) {
                c = chooseXValue(source, ability,  life);
            } else {
                c = AbilityUtils.calculateAmount(source, amount, ability);
            }
        }

        final List<Player> oppsThatCanGainLife = new ArrayList<Player>();
        for (final Player opp : getPotentialTargets(activator, source)) {
            if (opp.canGainLife()) {
                oppsThatCanGainLife.add(opp);
            }
        }

        if (cntPlayers == Integer.MAX_VALUE) // applied to all players who can gain
            return PaymentDecision.players(oppsThatCanGainLife);

        final StringBuilder sb = new StringBuilder();
        sb.append(source.getName()).append(" - Choose an opponent to gain ").append(c).append(" life:");

        final Player chosenToGain = GuiChoose.oneOrNone(sb.toString(), oppsThatCanGainLife);
        if (null == chosenToGain)
            return null;
        else
            return PaymentDecision.players(Lists.newArrayList(chosenToGain));
    }

    
    public <T> T accept(ICostVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
