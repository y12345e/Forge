package forge.ai.ability;

import forge.ai.SpellAbilityAi;
import forge.game.player.Player;
import forge.game.player.PlayerActionConfirmMode;
import forge.game.spellability.SpellAbility;

public class ShuffleAi extends SpellAbilityAi {
    @Override
    protected boolean canPlayAI(Player aiPlayer, SpellAbility sa) {
        // not really sure when the compy would use this; maybe only after a
        // human
        // deliberately put a card on top of their library
        return false;
        /*
         * if (!ComputerUtil.canPayCost(sa)) return false;
         * 
         * Card source = sa.getHostCard();
         * 
         * Random r = MyRandom.random; boolean randomReturn = r.nextFloat() <=
         * Math.pow(.667, sa.getActivationsThisTurn()+1);
         * 
         * if (AbilityFactory.playReusable(sa)) randomReturn = true;
         * 
         * Ability_Sub subAb = sa.getSubAbility(); if (subAb != null)
         * randomReturn &= subAb.chkAI_Drawback(); return randomReturn;
         */
    }

    @Override
    public boolean chkAIDrawback(SpellAbility sa, Player aiPlayer) {
        return shuffleTargetAI(sa);
    }

    private boolean shuffleTargetAI(final SpellAbility sa) {
        /*
         *  Shuffle at the end of some other effect where we'd usually shuffle
         *  inside that effect, but can't for some reason.
         */
        return sa.getParent() != null;
    } // shuffleTargetAI()

    @Override
    protected boolean doTriggerAINoCost(Player aiPlayer, SpellAbility sa, boolean mandatory) {
        return shuffleTargetAI(sa);
    }  

    @Override
    public boolean confirmAction(Player player, SpellAbility sa, PlayerActionConfirmMode mode, String message) {
     // ai could analyze parameter denoting the player to shuffle
        return true;
    }
}
