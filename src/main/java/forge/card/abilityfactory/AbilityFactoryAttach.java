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
package forge.card.abilityfactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import forge.AllZone;
import forge.AllZoneUtil;
import forge.Card;
import forge.CardList;
import forge.CardListFilter;
import forge.CardUtil;
import forge.CombatUtil;
import forge.Command;
import forge.ComputerUtil;
import forge.Constant;
import forge.Constant.Zone;
import forge.GameEntity;
import forge.Player;
import forge.card.cardfactory.CardFactoryUtil;
import forge.card.cost.Cost;
import forge.card.spellability.AbilityActivated;
import forge.card.spellability.AbilitySub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.SpellPermanent;
import forge.card.spellability.Target;
import forge.card.staticability.StaticAbility;
import forge.gui.GuiUtils;
import forge.util.MyRandom;

/**
 * The Class AbilityFactory_Attach.
 */
public class AbilityFactoryAttach {

    /**
     * Creates the spell attach.
     * 
     * @param abilityFactory
     *            the aF
     * @return the spell ability
     */
    public static SpellAbility createSpellAttach(final AbilityFactory abilityFactory) {
        // There are two types of Spell Attachments: Auras and
        // Instants/Sorceries
        // Auras generally target what that card will attach to
        // I/S generally target the Attacher and the Attachee
        SpellAbility spAttach = null;
        if (abilityFactory.getHostCard().isAura()) {
            // The 4th parameter is to resolve an issue with SetDescription in
            // default Spell_Permanent constructor
            spAttach = new SpellPermanent(abilityFactory.getHostCard(), abilityFactory.getAbCost(),
                    abilityFactory.getAbTgt(), false) {
                private static final long serialVersionUID = 6631124959690157874L;

                private final AbilityFactory af = abilityFactory;

                @Override
                public String getStackDescription() {
                    // when getStackDesc is called, just build exactly what is
                    // happening
                    return AbilityFactoryAttach.attachStackDescription(this.af, this);
                }

                @Override
                public boolean canPlayAI() {
                    return AbilityFactoryAttach.attachCanPlayAI(this.af, this);
                }

                @Override
                public void resolve() {
                    // The Spell_Permanent (Auras) version of this AF needs to
                    // move the card into play before Attaching
                    final Card c = AllZone.getGameAction().moveToPlay(this.getSourceCard());
                    this.setSourceCard(c);
                    AbilityFactoryAttach.attachResolve(this.af, this);
                }
            };
        } else {
            // This is here to be complete, however there's only a few cards
            // that use it
            // And the Targeting system can't really handle them at this time
            // (11/7/1)
            spAttach = new Spell(abilityFactory.getHostCard(), abilityFactory.getAbCost(), abilityFactory.getAbTgt()) {
                private static final long serialVersionUID = 6631124959690157874L;

                private final AbilityFactory af = abilityFactory;

                @Override
                public String getStackDescription() {
                    // when getStackDesc is called, just build exactly what is
                    // happening
                    return AbilityFactoryAttach.attachStackDescription(this.af, this);
                }

                @Override
                public boolean canPlayAI() {
                    return AbilityFactoryAttach.attachCanPlayAI(this.af, this);
                }

                @Override
                public void resolve() {
                    AbilityFactoryAttach.attachResolve(this.af, this);
                }
            };
        }
        return spAttach;
    }

    // Attach Ability
    /**
     * Creates the ability attach.
     * 
     * @param abilityFactory
     *            the aF
     * @return the spell ability
     */
    public static SpellAbility createAbilityAttach(final AbilityFactory abilityFactory) {
        final SpellAbility abAttach = new AbilityActivated(abilityFactory.getHostCard(), abilityFactory.getAbCost(),
                abilityFactory.getAbTgt()) {
            private static final long serialVersionUID = 2116313811316915141L;

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryAttach.attachCanPlayAI(abilityFactory, this);
            }

            @Override
            public String getStackDescription() {
                return AbilityFactoryAttach.attachStackDescription(abilityFactory, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryAttach.attachResolve(abilityFactory, this);
            } // resolve()

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryAttach.attachDoTriggerAI(abilityFactory, this, mandatory);
            }

        }; // SpellAbility

        return abAttach;
    }

    // Attach Drawback
    /**
     * Creates the drawback attach.
     * 
     * @param abilityFactory
     *            the aF
     * @return the spell ability
     */
    public static SpellAbility createDrawbackAttach(final AbilityFactory abilityFactory) {
        final SpellAbility dbAttach = new AbilitySub(abilityFactory.getHostCard(), abilityFactory.getAbTgt()) {
            private static final long serialVersionUID = 7211414518191821125L;

            private final AbilityFactory af = abilityFactory;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is
                // happening
                return AbilityFactoryAttach.attachStackDescription(this.af, this);
            }

            @Override
            public boolean canPlayAI() {
                return AbilityFactoryAttach.attachCanPlayAI(this.af, this);
            }

            @Override
            public void resolve() {
                AbilityFactoryAttach.attachResolve(this.af, this);
            }


            @Override
            public boolean chkAIDrawback() {
                return true;
            }

            @Override
            public boolean doTrigger(final boolean mandatory) {
                return AbilityFactoryAttach.attachDoTriggerAI(this.af, this, mandatory);
            }

        };
        return dbAttach;
    }

    /**
     * Attach stack description.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     * @return the string
     */
    public static String attachStackDescription(final AbilityFactory af, final SpellAbility sa) {
        final StringBuilder sb = new StringBuilder();

        if (!(sa instanceof AbilitySub)) {
            sb.append(sa.getSourceCard().getName()).append(" - ");
        } else {
            sb.append(" ");
        }

        final String conditionDesc = af.getMapParams().get("ConditionDescription");
        if (conditionDesc != null) {
            sb.append(conditionDesc).append(" ");
        }

        sb.append(" Attach to ");

        ArrayList<Object> targets;

        // Should never allow more than one Attachment per card
        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            targets = tgt.getTargets();
        } else {
            targets = AbilityFactory.getDefinedObjects(sa.getSourceCard(), af.getMapParams().get("Defined"), sa);
        }

        for (final Object o : targets) {
            sb.append(o).append(" ");
        }

        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * Attach preference.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     * @param params
     *            the params
     * @param tgt
     *            the tgt
     * @param mandatory
     *            the mandatory
     * @return true, if successful
     */
    public static boolean attachPreference(final AbilityFactory af, final SpellAbility sa,
            final Map<String, String> params, final Target tgt, final boolean mandatory) {
        Object o;
        if (tgt.canTgtPlayer()) {
            o = AbilityFactoryAttach.attachToPlayerAIPreferences(af, sa, mandatory);
        } else {
            o = AbilityFactoryAttach.attachToCardAIPreferences(af, sa, params, mandatory);
        }

        if (o == null) {
            return false;
        }

        tgt.addTarget(o);
        return true;
    }

    /**
     * Attach to card ai preferences.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     * @param params
     *            the params
     * @param mandatory
     *            the mandatory
     * @return the card
     */
    public static Card attachToCardAIPreferences(final AbilityFactory af, final SpellAbility sa,
            final Map<String, String> params, final boolean mandatory) {
        final Target tgt = sa.getTarget();
        final Card attachSource = sa.getSourceCard();
        // TODO AttachSource is currently set for the Source of the Spell, but
        // at some point can support attaching a different card

        CardList list = AllZoneUtil.getCardsIn(tgt.getZone());
        list = list.getValidCards(tgt.getValidTgts(), sa.getActivatingPlayer(), attachSource);

        // TODO If Attaching without casting, don't need to actually target.
        // I believe this is the only case where mandatory will be true, so just
        // check that when starting that work
        // But we shouldn't attach to things with Protection
        if (tgt.getZone().contains(Zone.Battlefield) && !mandatory) {
            list = list.getTargetableCards(sa);
        } else {
            list = list.getUnprotectedCards(attachSource);
        }

        if (list.size() == 0) {
            return null;
        }

        Card c = AbilityFactoryAttach.attachGeneralAI(sa, list, mandatory, attachSource, params.get("AILogic"));

        if ((c == null) && mandatory) {
            list.shuffle();
            c = list.get(0);
        }

        return c;
    }

    /**
     * Attach general ai.
     * 
     * @param sa
     *            the sa
     * @param list
     *            the list
     * @param mandatory
     *            the mandatory
     * @param attachSource
     *            the attach source
     * @param logic
     *            the logic
     * @return the card
     */
    public static Card attachGeneralAI(final SpellAbility sa, final CardList list, final boolean mandatory,
            final Card attachSource, final String logic) {
        final Player prefPlayer = "Pump".equals(logic) ? AllZone.getComputerPlayer() : AllZone.getHumanPlayer();
        // Some ChangeType cards are beneficial, and PrefPlayer should be
        // changed to represent that
        final CardList prefList = list.getController(prefPlayer);

        // If there are no preferred cards, and not mandatory bail out
        if (prefList.size() == 0) {
            return AbilityFactoryAttach.chooseUnpreferred(mandatory, list);
        }

        // Preferred list has at least one card in it to make to the actual
        // Logic
        Card c = null;
        if ("GainControl".equals(logic)) {
            c = AbilityFactoryAttach.attachAIControlPreference(sa, prefList, mandatory, attachSource);
        } else if ("Curse".equals(logic)) {
            c = AbilityFactoryAttach.attachAICursePreference(sa, prefList, mandatory, attachSource);
        } else if ("Pump".equals(logic)) {
            c = AbilityFactoryAttach.attachAIPumpPreference(sa, prefList, mandatory, attachSource);
        } else if ("ChangeType".equals(logic)) {
            c = AbilityFactoryAttach.attachAIChangeTypePreference(sa, prefList, mandatory, attachSource);
        } else if ("KeepTapped".equals(logic)) {
            c = AbilityFactoryAttach.attachAIKeepTappedPreference(sa, prefList, mandatory, attachSource);
        }

        return c;
    }

    /**
     * Choose unpreferred.
     * 
     * @param mandatory
     *            the mandatory
     * @param list
     *            the list
     * @return the card
     */
    public static Card chooseUnpreferred(final boolean mandatory, final CardList list) {
        if (!mandatory) {
            return null;
        }

        return CardFactoryUtil.getWorstPermanentAI(list, true, true, true, false);
    }

    /**
     * Choose less preferred.
     * 
     * @param mandatory
     *            the mandatory
     * @param list
     *            the list
     * @return the card
     */
    public static Card chooseLessPreferred(final boolean mandatory, final CardList list) {
        if (!mandatory) {
            return null;
        }

        return CardFactoryUtil.getBestAI(list);
    }

    /**
     * Acceptable choice.
     * 
     * @param c
     *            the c
     * @param mandatory
     *            the mandatory
     * @return the card
     */
    public static Card acceptableChoice(final Card c, final boolean mandatory) {
        if (mandatory) {
            return c;
        }

        // TODO If Not Mandatory, make sure the card is "good enough"
        if (c.isCreature()) {
            final int eval = CardFactoryUtil.evaluateCreature(c);
            if ((eval < 160) && ((eval < 130) || (AllZone.getComputerPlayer().getLife() > 5))) {
                return null;
            }
        }

        return c;
    }

    // Should generalize this code a bit since they all have similar structures
    /**
     * Attach ai control preference.
     * 
     * @param sa
     *            the sa
     * @param list
     *            the list
     * @param mandatory
     *            the mandatory
     * @param attachSource
     *            the attach source
     * @return the card
     */
    public static Card attachAIControlPreference(final SpellAbility sa, final CardList list, final boolean mandatory,
            final Card attachSource) {
        // AI For choosing a Card to Gain Control of.

        if (sa.getTarget().canTgtPermanent()) {
            // If can target all Permanents, and Life isn't in eminent danger,
            // grab Planeswalker first, then Creature
            // if Life < 5 grab Creature first, then Planeswalker. Lands,
            // Enchantments and Artifacts are probably "not good enough"

        }

        final Card c = CardFactoryUtil.getBestAI(list);

        // If Mandatory (brought directly into play without casting) gotta
        // choose something
        if (c == null) {
            return AbilityFactoryAttach.chooseLessPreferred(mandatory, list);
        }

        return AbilityFactoryAttach.acceptableChoice(c, mandatory);
    }

    /**
     * Attach ai pump preference.
     * 
     * @param sa
     *            the sa
     * @param list
     *            the list
     * @param mandatory
     *            the mandatory
     * @param attachSource
     *            the attach source
     * @return the card
     */
    public static Card attachAIPumpPreference(final SpellAbility sa, final CardList list, final boolean mandatory,
            final Card attachSource) {
        // AI For choosing a Card to Pump
        Card c = null;
        CardList magnetList = null;
        String stCheck = null;
        if (attachSource.isAura()) {
            stCheck = "EnchantedBy";
            magnetList = list.getEnchantMagnets();
        } else if (attachSource.isEquipment()) {
            stCheck = "EquippedBy";
            magnetList = list.getEquipMagnets();
        }

        if ((magnetList != null) && !magnetList.isEmpty()) {
            // Always choose something from the Magnet List.
            // Probably want to "weight" the list by amount of Enchantments and
            // choose the "lightest"

            magnetList = magnetList.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return CombatUtil.canAttack(c);
                }
            });

            return CardFactoryUtil.getBestAI(magnetList);
        }

        int totToughness = 0;
        int totPower = 0;
        final ArrayList<String> keywords = new ArrayList<String>();
        boolean grantingAbilities = false;

        for (final StaticAbility stAbility : attachSource.getStaticAbilities()) {
            final Map<String, String> params = stAbility.getMapParams();

            if (!params.get("Mode").equals("Continuous")) {
                continue;
            }

            final String affected = params.get("Affected");

            if (affected == null) {
                continue;
            }
            if ((affected.contains(stCheck) || affected.contains("AttachedBy"))) {
                totToughness += CardFactoryUtil.parseSVar(attachSource, params.get("AddToughness"));
                totPower += CardFactoryUtil.parseSVar(attachSource, params.get("AddPower"));

                grantingAbilities |= params.containsKey("AddAbility");

                final String kws = params.get("AddKeyword");
                if (kws != null) {
                    for (final String kw : kws.split(" & ")) {
                        keywords.add(kw);
                    }
                }
            }
        }

        CardList prefList = new CardList(list);
        if (totToughness < 0) {
            // Don't kill my own stuff with Negative toughness Auras
            final int tgh = totToughness;
            prefList = prefList.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return c.getLethalDamage() > Math.abs(tgh);
                }
            });
        }

        else if ((totToughness == 0) && (totPower == 0)) {
            // Just granting Keywords don't assign stacking Keywords
            final Iterator<String> it = keywords.iterator();
            while (it.hasNext()) {
                final String key = it.next();
                if (CardUtil.isStackingKeyword(key)) {
                    it.remove();
                }
            }
            if (!keywords.isEmpty()) {
                final ArrayList<String> finalKWs = keywords;
                prefList = prefList.filter(new CardListFilter() {
                    // If Aura grants only Keywords, don't Stack unstackable
                    // keywords
                    @Override
                    public boolean addCard(final Card c) {
                        for (final String kw : finalKWs) {
                            if (c.hasKeyword(kw)) {
                                return false;
                            }
                        }
                        return true;
                    }
                });
            }
        }

        if (attachSource.isAura()) {
            // TODO For Auras like Rancor, that aren't as likely to lead to
            // card disadvantage, this check should be skipped
            prefList = prefList.filter(new CardListFilter() {

                @Override
                public boolean addCard(final Card c) {
                    return !c.isEnchanted();
                }
            });
        }

        if (!grantingAbilities) {
            // Probably prefer to Enchant Creatures that Can Attack
            // Filter out creatures that can't Attack or have Defender
            prefList = prefList.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    return !c.isCreature() || CombatUtil.canAttack(c);
                }
            });
            c = CardFactoryUtil.getBestAI(prefList);
        } else {
            // If we grant abilities, we may want to put it on something Weak?
            // Possibly more defensive?
            c = CardFactoryUtil.getWorstPermanentAI(prefList, false, false, false, false);
        }

        if (c == null) {
            return AbilityFactoryAttach.chooseLessPreferred(mandatory, list);
        }

        return AbilityFactoryAttach.acceptableChoice(c, mandatory);
    }

    /**
     * Attach ai curse preference.
     * 
     * @param sa
     *            the sa
     * @param list
     *            the list
     * @param mandatory
     *            the mandatory
     * @param attachSource
     *            the attach source
     * @return the card
     */
    public static Card attachAICursePreference(final SpellAbility sa, final CardList list, final boolean mandatory,
            final Card attachSource) {
        // AI For choosing a Card to Curse of.

        // TODO Figure out some way to combine The "gathering of data" from
        // statics used in both Pump and Curse
        String stCheck = null;
        if (attachSource.isAura()) {
            stCheck = "EnchantedBy";
        } else if (attachSource.isEquipment()) {
            stCheck = "EquippedBy";
        }

        int totToughness = 0;
        // int totPower = 0;
        final ArrayList<String> keywords = new ArrayList<String>();
        // boolean grantingAbilities = false;

        for (final StaticAbility stAbility : attachSource.getStaticAbilities()) {
            final Map<String, String> params = stAbility.getMapParams();

            if (!params.get("Mode").equals("Continuous")) {
                continue;
            }

            final String affected = params.get("Affected");

            if (affected == null) {
                continue;
            }
            if ((affected.contains(stCheck) || affected.contains("AttachedBy"))) {
                totToughness += CardFactoryUtil.parseSVar(attachSource, params.get("AddToughness"));
                // totPower += CardFactoryUtil.parseSVar(attachSource,
                // params.get("AddPower"));

                // grantingAbilities |= params.containsKey("AddAbility");

                final String kws = params.get("AddKeyword");
                if (kws != null) {
                    for (final String kw : kws.split(" & ")) {
                        keywords.add(kw);
                    }
                }
            }
        }

        CardList prefList = null;
        if (totToughness < 0) {
            // Kill a creature if we can
            final int tgh = totToughness;
            prefList = list.filter(new CardListFilter() {
                @Override
                public boolean addCard(final Card c) {
                    if (!c.hasKeyword("Indestructible") && (c.getLethalDamage() <= Math.abs(tgh))) {
                        return true;
                    }

                    return c.getNetDefense() <= Math.abs(tgh);
                }
            });
        }
        Card c = null;
        if ((prefList == null) || (prefList.size() == 0)) {
            prefList = new CardList(list);
        } else {
            c = CardFactoryUtil.getBestAI(prefList);
            if (c != null) {
                return c;
            }
        }

        if (!keywords.isEmpty()) {
            // Don't give Can't Attack or Defender to cards that can't do these
            // things to begin with
            if (keywords.contains("CARDNAME can't attack") || keywords.contains("Defender")
                    || keywords.contains("CARDNAME attacks each turn if able.")) {
                prefList = prefList.filter(new CardListFilter() {
                    @Override
                    public boolean addCard(final Card c) {
                        return !(c.hasKeyword("CARDNAME can't attack") || c.hasKeyword("Defender"));
                    }
                });
            }
        }

        c = CardFactoryUtil.getBestAI(prefList);

        if (c == null) {
            return AbilityFactoryAttach.chooseLessPreferred(mandatory, list);
        }

        return AbilityFactoryAttach.acceptableChoice(c, mandatory);
    }

    /**
     * Attach ai change type preference.
     * 
     * @param sa
     *            the sa
     * @param list
     *            the list
     * @param mandatory
     *            the mandatory
     * @param attachSource
     *            the attach source
     * @return the card
     */
    public static Card attachAIChangeTypePreference(final SpellAbility sa, CardList list, final boolean mandatory,
            final Card attachSource) {
        // AI For Cards like Evil Presence or Spreading Seas

        String type = "";

        for (final StaticAbility stAb : attachSource.getStaticAbilities()) {
            final HashMap<String, String> params = stAb.getMapParams();
            if (params.get("Mode").equals("Continuous") && params.containsKey("AddType")) {
                type = params.get("AddType");
            }
        }

        list = list.getNotType(type); // Filter out Basic Lands that have the
                                      // same type as the changing type

        final Card c = CardFactoryUtil.getBestAI(list);

        // TODO Port over some of the existing code, but rewrite most of it.
        // Ultimately, these spells need to be used to reduce mana base of a
        // color. So it might be better to choose a Basic over a Nonbasic
        // Although a nonbasic card with a nasty ability, might be worth it to
        // cast on

        if (c == null) {
            return AbilityFactoryAttach.chooseLessPreferred(mandatory, list);
        }

        return AbilityFactoryAttach.acceptableChoice(c, mandatory);
    }

    /**
     * Attach ai keep tapped preference.
     * 
     * @param sa
     *            the sa
     * @param list
     *            the list
     * @param mandatory
     *            the mandatory
     * @param attachSource
     *            the attach source
     * @return the card
     */
    public static Card attachAIKeepTappedPreference(final SpellAbility sa, final CardList list,
            final boolean mandatory, final Card attachSource) {
        // AI For Cards like Paralyzing Grasp and Glimmerdust Nap
        final CardList prefList = list.filter(new CardListFilter() {
            @Override
            public boolean addCard(final Card c) {
                // Don't do Untapped Vigilance cards
                if (c.isCreature() && c.hasKeyword("Vigilance") && c.isUntapped()) {
                    return false;
                }

                if (!c.isEnchanted()) {
                    return true;
                }

                final ArrayList<Card> auras = c.getEnchantedBy();
                final Iterator<Card> itr = auras.iterator();
                while (itr.hasNext()) {
                    final Card aura = itr.next();
                    final AbilityFactory af = aura.getSpellPermanent().getAbilityFactory();
                    if ((af != null) && af.getAPI().equals("Attach")) {
                        final Map<String, String> params = af.getMapParams();
                        if ("KeepTapped".equals(params.get("AILogic"))) {
                            // Don't attach multiple KeepTapped Auras to one
                            // card
                            return false;
                        }
                    }
                }

                return true;
            }
        });

        final Card c = CardFactoryUtil.getBestAI(prefList);

        if (c == null) {
            return AbilityFactoryAttach.chooseLessPreferred(mandatory, list);
        }

        return AbilityFactoryAttach.acceptableChoice(c, mandatory);
    }

    /**
     * Attach to player ai preferences.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     * @param mandatory
     *            the mandatory
     * @return the player
     */
    public static Player attachToPlayerAIPreferences(final AbilityFactory af, final SpellAbility sa,
            final boolean mandatory) {
        final Target tgt = sa.getTarget();
        Player p;
        if (tgt.canOnlyTgtOpponent()) {
            // If can Only Target Opponent, do so.
            p = AllZone.getHumanPlayer();
            if (p.canBeTargetedBy(sa)) {
                return p;
            } else {
                return null;
            }
        }

        if ("Curse".equals(af.getMapParams().get("AILogic"))) {
            p = AllZone.getHumanPlayer();
        } else {
            p = AllZone.getComputerPlayer();
        }

        if (p.canBeTargetedBy(sa)) {
            return p;
        }

        if (!mandatory) {
            return null;
        }

        p = p.getOpponent();
        if (p.canBeTargetedBy(sa)) {
            return p;
        }

        return null;
    }

    /**
     * Attach can play ai.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     * @return true, if successful
     */
    public static boolean attachCanPlayAI(final AbilityFactory af, final SpellAbility sa) {
        final Random r = MyRandom.getRandom();
        final Map<String, String> params = af.getMapParams();
        final Cost abCost = sa.getPayCosts();
        final Card source = sa.getSourceCard();

        if (abCost != null) {
            // No Aura spells have Additional Costs

        }

        // prevent run-away activations - first time will always return true
        boolean chance = r.nextFloat() <= .6667;

        // Attach spells always have a target
        final Target tgt = sa.getTarget();
        if (tgt != null) {
            tgt.resetTargets();
            if (!AbilityFactoryAttach.attachPreference(af, sa, params, tgt, false)) {
                return false;
            }
        }

        if (abCost.getTotalMana().contains("X") && source.getSVar("X").equals("Count$xPaid")) {
            // Set PayX here to maximum value. (Endless Scream and Venarian
            // Gold)
            final int xPay = ComputerUtil.determineLeftoverMana(sa);

            if (xPay == 0) {
                return false;
            }

            source.setSVar("PayX", Integer.toString(xPay));
        }

        if (AllZone.getPhaseHandler().isAfter(Constant.Phase.COMBAT_DECLARE_BLOCKERS_INSTANT_ABILITY)
                && !"Curse".equals(af.getMapParams().get("AILogic"))) {
            return false;
        }

        chance &= r.nextFloat() <= .75;

        return chance;
    }

    /**
     * Attach do trigger ai.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     * @param mandatory
     *            the mandatory
     * @return true, if successful
     */
    public static boolean attachDoTriggerAI(final AbilityFactory af, final SpellAbility sa, final boolean mandatory) {
        final Map<String, String> params = af.getMapParams();
        final Card card = sa.getSourceCard();

        if (!ComputerUtil.canPayCost(sa)) {
            // usually not mandatory
            return false;
        }

        // Check if there are any valid targets
        ArrayList<Object> targets = new ArrayList<Object>();
        final Target tgt = af.getAbTgt();
        if (tgt == null) {
            targets = AbilityFactory.getDefinedObjects(sa.getSourceCard(), params.get("Defined"), sa);
        }

        if (!mandatory && card.isEquipment() && !targets.isEmpty()) {
            Card newTarget = (Card) targets.get(0);
            //don't equip human creatures
            if (newTarget.getController().isPlayer(AllZone.getHumanPlayer())) {
                return false;
            }

            //don't equip a worse creature
            if (card.isEquipping()) {
                Card oldTarget = card.getEquipping().get(0);
                if (CardFactoryUtil.evaluateCreature(oldTarget) > CardFactoryUtil.evaluateCreature(newTarget)) {
                    return false;
                }
            }
        }

        // check SubAbilities DoTrigger?
        final AbilitySub abSub = sa.getSubAbility();
        if (abSub != null) {
            return abSub.doTrigger(mandatory);
        }

        return true;
    }

    /**
     * Attach resolve.
     * 
     * @param af
     *            the af
     * @param sa
     *            the sa
     */
    public static void attachResolve(final AbilityFactory af, final SpellAbility sa) {
        final Map<String, String> params = af.getMapParams();
        final Card card = sa.getSourceCard();

        ArrayList<Object> targets;

        final Target tgt = af.getAbTgt();
        if (tgt != null) {
            targets = tgt.getTargets();
            // TODO Remove invalid targets (although more likely this will just
            // fizzle earlier)
        } else {
            targets = AbilityFactory.getDefinedObjects(sa.getSourceCard(), params.get("Defined"), sa);
        }

        // If Cast Targets will be checked on the Stack
        for (final Object o : targets) {
            AbilityFactoryAttach.handleAttachment(card, o, af);
        }
    }

    /**
     * Handle attachment.
     * 
     * @param card
     *            the card
     * @param o
     *            the o
     * @param af
     *            the af
     */
    public static void handleAttachment(final Card card, final Object o, final AbilityFactory af) {

        if (o instanceof Card) {
            final Card c = (Card) o;
            if (card.isAura()) {
                // Most Auras can enchant permanents, a few can Enchant cards in
                // graveyards
                // Spellweaver Volute, Dance of the Dead, Animate Dead
                // Although honestly, I'm not sure if the three of those could
                // handle being scripted
                final boolean gainControl = "GainControl".equals(af.getMapParams().get("AILogic"));
                AbilityFactoryAttach.handleAura(card, c, gainControl);
            } else if (card.isEquipment()) {
                if (card.isEquipping()) {
                    card.unEquipCard(card.getEquipping().get(0));
                }

                card.equipCard(c);
                // else if (card.isFortification())
                // card.fortifyCard(c);
            }
        } else if (o instanceof Player) {
            // Currently, a few cards can enchant players
            // Psychic Possession, Paradox Haze, Wheel of Sun and Moon, New
            // Curse cards
            final Player p = (Player) o;
            if (card.isAura()) {
                AbilityFactoryAttach.handleAura(card, p, false);
            }
        }
    }

    /**
     * Handle aura.
     * 
     * @param card
     *            the card
     * @param tgt
     *            the tgt
     * @param gainControl
     *            the gain control
     */
    public static void handleAura(final Card card, final GameEntity tgt, final boolean gainControl) {
        if (card.isEnchanting()) {
            // If this Card is already Enchanting something
            // Need to unenchant it, then clear out the commands
            final GameEntity oldEnchanted = card.getEnchanting();
            card.removeEnchanting(oldEnchanted);
            card.clearEnchantCommand();
            card.clearUnEnchantCommand();
            card.clearTriggers(); // not sure if cleartriggers is needed?
        }

        if (gainControl) {
            // Handle GainControl Auras
            final Player[] pl = new Player[1];

            if (tgt instanceof Card) {
                pl[0] = ((Card) tgt).getController();
            } else {
                pl[0] = (Player) tgt;
            }

            final Command onEnchant = new Command() {
                private static final long serialVersionUID = -2519887209491512000L;

                @Override
                public void execute() {
                    final Card crd = card.getEnchantingCard();
                    if (crd == null) {
                        return;
                    }

                    pl[0] = crd.getController();

                    crd.addController(card);

                } // execute()
            }; // Command

            final Command onUnEnchant = new Command() {
                private static final long serialVersionUID = 3426441132121179288L;

                @Override
                public void execute() {
                    final Card crd = card.getEnchantingCard();
                    if (crd == null) {
                        return;
                    }

                    if (AllZoneUtil.isCardInPlay(crd)) {
                        crd.removeController(card);
                    }

                } // execute()
            }; // Command

            final Command onChangesControl = new Command() {
                /** automatically generated serialVersionUID. */
                private static final long serialVersionUID = -65903786170234039L;

                @Override
                public void execute() {
                    final Card crd = card.getEnchantingCard();
                    if (crd == null) {
                        return;
                    }
                    crd.removeController(card); // This looks odd, but will
                                                // simply refresh controller
                    crd.addController(card);
                } // execute()
            }; // Command

            // Add Enchant Commands for Control changers
            card.addEnchantCommand(onEnchant);
            card.addUnEnchantCommand(onUnEnchant);
            card.addChangeControllerCommand(onChangesControl);
        }

        final Command onLeavesPlay = new Command() {
            private static final long serialVersionUID = -639204333673364477L;

            @Override
            public void execute() {
                final GameEntity entity = card.getEnchanting();
                if (entity == null) {
                    return;
                }

                card.unEnchantEntity(entity);
            }
        }; // Command

        card.addLeavesPlayCommand(onLeavesPlay);
        card.enchantEntity(tgt);
    }

    /**
     * Gets the attach spell ability.
     * 
     * @param source
     *            the source
     * @return the attach spell ability
     */
    public static SpellAbility getAttachSpellAbility(final Card source) {
        SpellAbility aura = null;
        AbilityFactory af = null;
        for (final SpellAbility sa : source.getSpells()) {
            af = sa.getAbilityFactory();
            if ((af != null) && af.getAPI().equals("Attach")) {
                aura = sa;
                break;
            }
        }
        return aura;
    }

    /**
     * Attach aura on indirect enter battlefield.
     * 
     * @param source
     *            the source
     * @return true, if successful
     */
    public static boolean attachAuraOnIndirectEnterBattlefield(final Card source) {
        // When an Aura ETB without being cast you can choose a valid card to
        // attach it to
        final SpellAbility aura = AbilityFactoryAttach.getAttachSpellAbility(source);

        if (aura == null) {
            return false;
        }

        final AbilityFactory af = aura.getAbilityFactory();
        final Target tgt = aura.getTarget();
        final boolean gainControl = "GainControl".equals(af.getMapParams().get("AILogic"));

        if (source.getController().isHuman()) {
            if (tgt.canTgtPlayer()) {
                final ArrayList<Player> players = new ArrayList<Player>();

                // TODO Once Player's are gaining Protection we need to add a
                // check here

                players.add(AllZone.getComputerPlayer());
                if (!tgt.canOnlyTgtOpponent()) {
                    players.add(AllZone.getHumanPlayer());
                }

                final Object o = GuiUtils.getChoice(source + " - Select a player to attach to.", players.toArray());
                if (o instanceof Player) {
                    AbilityFactoryAttach.handleAura(source, (Player) o, false);
                    //source.enchantEntity((Player) o);
                    return true;
                }
            } else {
                CardList list = AllZoneUtil.getCardsIn(tgt.getZone());
                list = list.getValidCards(tgt.getValidTgts(), aura.getActivatingPlayer(), source);

                final Object o = GuiUtils.getChoice(source + " - Select a card to attach to.", list.toArray());
                if (o instanceof Card) {
                    AbilityFactoryAttach.handleAura(source, (Card) o, gainControl);
                    //source.enchantEntity((Card) o);
                    return true;
                }
            }
        }

        else if (AbilityFactoryAttach.attachPreference(af, aura, af.getMapParams(), tgt, true)) {
            final Object o = aura.getTarget().getTargets().get(0);
            if (o instanceof Card) {
                //source.enchantEntity((Card) o);
                AbilityFactoryAttach.handleAura(source, (Card) o, gainControl);
                return true;
            } else if (o instanceof Player) {
                //source.enchantEntity((Player) o);
                AbilityFactoryAttach.handleAura(source, (Player) o, false);
                return true;
            }
        }

        return false;
    }
}
