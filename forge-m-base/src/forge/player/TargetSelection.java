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
package forge.player;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;

import forge.game.Game;
import forge.game.GameObject;
import forge.game.card.Card;
import forge.game.card.CardLists;
import forge.game.spellability.SpellAbility;
import forge.game.spellability.SpellAbilityStackInstance;
import forge.game.spellability.TargetRestrictions;
import forge.game.zone.Zone;
import forge.game.zone.ZoneType;
import forge.screens.match.input.InputSelectTargets;
import forge.toolbox.GuiChoose;
import forge.util.Aggregates;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Target_Selection class.
 * </p>
 * 
 * @author Forge
 * @version $Id: TargetSelection.java 25112 2014-03-09 10:18:52Z swordshine $
 */
public class TargetSelection {
    private final SpellAbility ability;


    public TargetSelection(final SpellAbility sa) {
        this.ability = sa;
    }

    private final TargetRestrictions getTgt() {
        return this.ability.getTargetRestrictions();
    }

    private boolean bTargetingDone = false;


    /**
     * <p>
     * resetTargets.
     * </p>
     */

    public final boolean chooseTargets(Integer numTargets) {
        TargetRestrictions tgt = getTgt();
        final boolean canTarget = tgt != null && tgt.doesTarget();
        if (!canTarget) {
            throw new RuntimeException("TargetSelection.chooseTargets called for ability that does not target - " + ability);
        }
        
        // Number of targets is explicitly set only if spell is being redirected (ex. Swerve or Redirect) 
        final int minTargets = numTargets != null ? numTargets.intValue() : tgt.getMinTargets(ability.getHostCard(), ability);
        final int maxTargets = numTargets != null ? numTargets.intValue() : tgt.getMaxTargets(ability.getHostCard(), ability);
        final int numTargeted = ability.getTargets().getNumTargeted();

        boolean hasEnoughTargets = minTargets == 0 || numTargeted >= minTargets;
        boolean hasAllTargets = numTargeted == maxTargets && maxTargets > 0;
        if (maxTargets == 0) return true;
        // if not enough targets chosen, cancel Ability
        if (this.bTargetingDone && !hasEnoughTargets) {
            return false;
        }

        if (this.bTargetingDone && hasEnoughTargets || hasAllTargets || tgt.isDividedAsYouChoose() && tgt.getStillToDivide() == 0) {
            return true;
        }

        if (!tgt.hasCandidates(this.ability, true) && !hasEnoughTargets) {
            // Cancel ability if there aren't any valid Candidates
            return false;
        }
        
        final List<ZoneType> zone = tgt.getZone();
        final boolean mandatory = tgt.getMandatory() && tgt.hasCandidates(this.ability, true);
        
        final boolean choiceResult;
        final boolean random = tgt.isRandomTarget();
        if (random) {
            List<GameObject> candidates = tgt.getAllCandidates(this.ability, true);
            GameObject choice = Aggregates.random(candidates);
            return ability.getTargets().add(choice);
        }
        else if (zone.size() == 1 && zone.get(0) == ZoneType.Stack) {
            // If Zone is Stack, the choices are handled slightly differently.
            // Handle everything inside function due to interaction with StackInstance
            return this.chooseCardFromStack(mandatory);
        }
        else {
            List<Card> validTargets = this.getValidCardsToTarget();
            if (zone.size() == 1 && (zone.get(0) == ZoneType.Battlefield || zone.get(0) == ZoneType.Hand)) {
                InputSelectTargets inp = new InputSelectTargets(validTargets, ability, mandatory);
                inp.showAndWait();
                choiceResult = !inp.hasCancelled();
                bTargetingDone = inp.hasPressedOk();
            } else {
                // for every other case an all-purpose GuiChoose
                choiceResult = this.chooseCardFromList(validTargets, true, mandatory);
            }
        }
        // some inputs choose cards one-by-one and need to be called again 
        return choiceResult && chooseTargets(numTargets);
    }

    

    // these have been copied over from CardFactoryUtil as they need two extra
    // parameters for target selection.
    // however, due to the changes necessary for SA_Requirements this is much
    // different than the original

    /**
     * <p>
     * chooseValidInput.
     * </p>
     * @return 
     */
    private final List<Card> getValidCardsToTarget() {
        final TargetRestrictions tgt = this.getTgt();
        final Game game = ability.getActivatingPlayer().getGame();
        final List<ZoneType> zone = tgt.getZone();

        final boolean canTgtStack = zone.contains(ZoneType.Stack);
        List<Card> validCards = CardLists.getValidCards(game.getCardsIn(zone), tgt.getValidTgts(), this.ability.getActivatingPlayer(), this.ability.getHostCard());
        List<Card> choices = CardLists.getTargetableCards(validCards, this.ability);
        if (canTgtStack) {
            // Since getTargetableCards doesn't have additional checks if one of the Zones is stack
            // Remove the activating card from targeting itself if its on the Stack
            Card activatingCard = ability.getHostCard();
            if (activatingCard.isInZone(ZoneType.Stack)) {
                choices.remove(ability.getHostCard());
            }
        }
        List<GameObject> targetedObjects = this.ability.getUniqueTargets();

        if (tgt.isUniqueTargets()) {
            for (final Object o : targetedObjects) {
                if ((o instanceof Card) && targetedObjects.contains(o)) {
                    choices.remove(o);
                }
            }
        }

        // Remove cards already targeted
        final List<Card> targeted = Lists.newArrayList(ability.getTargets().getTargetCards());
        for (final Card c : targeted) {
            if (choices.contains(c)) {
                choices.remove(c);
            }
        }

        // If all cards (including subability targets) must have the same controller
        if (tgt.isSameController() && !targetedObjects.isEmpty()) {
            final List<Card> list = new ArrayList<Card>();
            for (final Object o : targetedObjects) {
                if (o instanceof Card) {
                    list.add((Card) o);
                }
            }
            if (!list.isEmpty()) {
                final Card card = list.get(0);
                choices = CardLists.filter(choices, new Predicate<Card>() {
                    @Override
                    public boolean apply(final Card c) {
                        return c.sharesControllerWith(card);
                    }
                });
            }
        }

        return choices;
    }

    /**
     * <p>
     * chooseCardFromList.
     * </p>
     * 
     * @param choices
     *            a {@link forge.CardList} object.
     * @param targeted
     *            a boolean.
     * @param mandatory
     *            a boolean.
     */
    private final boolean chooseCardFromList(final List<Card> choices, final boolean targeted, final boolean mandatory) {
        // Send in a list of valid cards, and popup a choice box to target
        final Game game = ability.getActivatingPlayer().getGame();

        final List<Card> crdsBattle = new ArrayList<Card>();
        final List<Card> crdsExile = new ArrayList<Card>();
        final List<Card> crdsGrave = new ArrayList<Card>();
        final List<Card> crdsLibrary = new ArrayList<Card>();
        final List<Card> crdsStack = new ArrayList<Card>();
        final List<Card> crdsAnte = new ArrayList<Card>();
        for (final Card inZone : choices) {
            Zone zz = game.getZoneOf(inZone);
            if (zz.is(ZoneType.Battlefield))    crdsBattle.add(inZone);
            else if (zz.is(ZoneType.Exile))     crdsExile.add(inZone);
            else if (zz.is(ZoneType.Graveyard)) crdsGrave.add(inZone);
            else if (zz.is(ZoneType.Library))   crdsLibrary.add(inZone);
            else if (zz.is(ZoneType.Stack))     crdsStack.add(inZone);
            else if (zz.is(ZoneType.Ante))      crdsAnte.add(inZone);
        }
        List<Object> choicesFiltered = new ArrayList<Object>();
        if (!crdsBattle.isEmpty()) {
            choicesFiltered.add("--CARDS ON BATTLEFIELD:--");
            choicesFiltered.addAll(crdsBattle);
        }
        if (!crdsExile.isEmpty()) {
            choicesFiltered.add("--CARDS IN EXILE:--");
            choicesFiltered.addAll(crdsExile);
        }
        if (!crdsGrave.isEmpty()) {
            choicesFiltered.add("--CARDS IN GRAVEYARD:--");
            choicesFiltered.addAll(crdsGrave);
        }
        if (!crdsLibrary.isEmpty()) {
            choicesFiltered.add("--CARDS IN LIBRARY:--");
            choicesFiltered.addAll(crdsLibrary);
        }
        if (!crdsStack.isEmpty()) {
            choicesFiltered.add("--CARDS IN STACK:--");
            choicesFiltered.addAll(crdsStack);
        }
        if (!crdsAnte.isEmpty()) {
            choicesFiltered.add("--CARDS IN ANTE:--");
            choicesFiltered.addAll(crdsAnte);
        }

        final String msgDone = "[FINISH TARGETING]";
        if (this.getTgt().isMinTargetsChosen(this.ability.getHostCard(), this.ability)) {
            // is there a more elegant way of doing this?
            choicesFiltered.add(msgDone);
        }
        
        Object chosen = null;
        if (!choices.isEmpty() && mandatory) {
            chosen = GuiChoose.one(getTgt().getVTSelection(), choicesFiltered);
        }
        else {
            chosen = GuiChoose.oneOrNone(getTgt().getVTSelection(), choicesFiltered);
        }
        if (chosen == null) {
            return false;
        }
        if (msgDone.equals(chosen)) {
            bTargetingDone = true;
            return true;
        }
        
        if (chosen instanceof Card)
            ability.getTargets().add((Card)chosen);
        return true;
    }

    /**
     * <p>
     * chooseCardFromStack.
     * </p>
     * 
     * @param mandatory
     *            a boolean.
     */
    private final boolean chooseCardFromStack(final boolean mandatory) {
        final TargetRestrictions tgt = this.getTgt();
        final String message = tgt.getVTSelection();
        // Find what's targetable, then allow human to choose
        final List<Object> selectOptions = new ArrayList<Object>();

        final Game game = ability.getActivatingPlayer().getGame();
        for (SpellAbilityStackInstance si : game.getStack()) {
            SpellAbility abilityOnStack = si.getSpellAbility();
            if (ability.equals(abilityOnStack)) {
                // By peeking at stack item, target is set to its SI state. So set it back before adding targets
                ability.resetTargets();
            }
            else if (ability.canTargetSpellAbility(abilityOnStack)) {
                selectOptions.add(abilityOnStack);
            }
        }

        while(!bTargetingDone) {
            if (tgt.isMaxTargetsChosen(this.ability.getHostCard(), this.ability)) {
                bTargetingDone = true;
                return true;
            }

            if (!selectOptions.contains("[FINISH TARGETING]") && tgt.isMinTargetsChosen(this.ability.getHostCard(), this.ability)) {
                selectOptions.add("[FINISH TARGETING]");
            }

            if (selectOptions.isEmpty()) {
                // Not enough targets, cancel targeting
                return false;
            } else {
                final Object madeChoice = GuiChoose.oneOrNone(message, selectOptions);
                if (madeChoice == null) {
                    return false;
                }
                if (madeChoice instanceof SpellAbility) {
                    ability.getTargets().add((SpellAbility)madeChoice);
                } else // 'FINISH TARGETING' chosen 
                    bTargetingDone = true;
            }
        }
        return true;
    }
}
