package forge.game.card;

import forge.GameCommand;
import forge.card.CardCharacteristicName;
import forge.card.mana.ManaCost;
import forge.card.mana.ManaCostParser;
import forge.game.CardTraitBase;
import forge.game.ability.AbilityFactory;
import forge.game.cost.Cost;
import forge.game.replacement.ReplacementEffect;
import forge.game.replacement.ReplacementHandler;
import forge.game.replacement.ReplacementLayer;
import forge.game.spellability.SpellAbility;
import forge.game.trigger.Trigger;
import forge.game.trigger.TriggerHandler;
import forge.game.zone.ZoneType;
import forge.util.Lang;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Hellfish on 2014-02-10.
 */
public class KeywordInstance extends CardTraitBase {

    private KeywordType id;
    private boolean isHidden;
    private String script;

    private final List<String> magnitude = new ArrayList<String>();
    private final List<Cost> myCost = new ArrayList<Cost>();
    private final List<String> type = new ArrayList<String>();
    private final List<ReplacementLayer> replacementLayer = new ArrayList<ReplacementLayer>();
    private final List<String> rawString = new ArrayList<String>();
    private final List<CounterType> counterType = new ArrayList<CounterType>();
    private final List<Boolean> myBoolean = new ArrayList<Boolean>();
    private final List<ZoneType> zone = new ArrayList<ZoneType>();
    private final List<String> overridingDescription = new ArrayList<String>();

    public boolean equivalentTo(KeywordInstance that) {
        if (this.getId() != that.getId()
                || this.magnitude.size() != that.magnitude.size()
                || this.myCost.size() != that.myCost.size()
                || this.type.size() != that.type.size()
                || this.replacementLayer.size() != that.replacementLayer.size()
                || this.rawString.size() != that.rawString.size()
                || this.counterType.size() != that.counterType.size()
                || this.myBoolean.size() != that.myBoolean.size()
                || this.zone.size() != that.zone.size()
                || this.overridingDescription.size() != that.overridingDescription.size())
            return false;

        for(int i = 0;i<this.magnitude.size();i++) {
            if(!magnitude.get(i).equals(that.magnitude.get(i)))
                return false;
        }
        for(int i = 0;i<this.myCost.size();i++) {
            if(!myCost.get(i).equals(that.myCost.get(i)))
                return false;
        }
        for(int i = 0;i<this.type.size();i++) {
            if(!type.get(i).equals(that.type.get(i)))
                return false;
        }
        for(int i = 0;i<this.replacementLayer.size();i++) {
            if(!replacementLayer.get(i).equals(that.replacementLayer.get(i)))
                return false;
        }
        for(int i = 0;i<this.rawString.size();i++) {
            if(!rawString.get(i).equals(that.rawString.get(i)))
                return false;
        }
        for(int i = 0;i<this.counterType.size();i++) {
            if(!counterType.get(i).equals(that.counterType.get(i)))
                return false;
        }
        for(int i = 0;i<this.myBoolean.size();i++) {
            if(!myBoolean.get(i).equals(that.myBoolean.get(i)))
                return false;
        }
        for(int i = 0;i<this.zone.size();i++) {
            if(!zone.get(i).equals(that.zone.get(i)))
                return false;
        }
        for(int i = 0;i<this.overridingDescription.size();i++) {
            if(!overridingDescription.get(i).equals(that.overridingDescription.get(i)))
                return false;
        }

        return true;
    }
    public boolean equals(KeywordInstance that) {
        return this.equivalentTo(that) && this.isHidden() == that.isHidden() && this.getHostCard().equals(that.getHostCard());
    }

    // more fields if needed

    public KeywordInstance(Card host, boolean intr, boolean h, KeywordType t) {
        this.hostCard = host;
        this.intrinsic = intr;

        id = t;
        isHidden = h;
    }

    public String toString() {
        String res;

        res = id.description != null ? id.description : "";

        if(id.reminderText != null) {
            res += " (" + id.reminderText + ")";
        }

        return res;
    }

    public void realize() {
        //This method will create Triggers,ReplacementEffects,SpellAbilities and StaticAbilities
        //Representing the functioning of the keyword, like CardFactoryUtil.makeKeywordAbilities does now.

        // this function should handle any keywords that need to be added after
        // a spell goes through the factory

        if(hostCard == null) {
            return;
        }

        if (id == KeywordType.Multikicker) {
            hostCard.getFirstSpellAbility().setMultiKickerManaCost(new ManaCost(new ManaCostParser(getMyCost().toString())));
        }

        else if(id == KeywordType.Fuse) {
            hostCard.getState(CardCharacteristicName.Original).getSpellAbility().add(AbilityFactory.buildFusedAbility(hostCard));
        }

        else if(id == KeywordType.Evoke) {
            //hostCard.addSpellAbility(CardFactoryUtil.makeEvokeSpell(hostCard, this));
        }

        else if (id == KeywordType.Monstrosity) {
            final String magnitude = getMagnitude();
            final Cost manacost = getMyCost();

            String ref = "X".equals(magnitude) ? " | References$ X" : "";
            String counters = StringUtils.isNumeric(magnitude)
                    ? Lang.nounWithNumeral(Integer.parseInt(magnitude), "+1/+1 counter"): "X +1/+1 counters";
            String effect = "AB$ PutCounter | Cost$ " + manacost + " | ConditionPresent$ " +
                    "Card.Self+IsNotMonstrous | Monstrosity$ True | CounterNum$ " +
                    magnitude + " | CounterType$ P1P1 | SpellDescription$ Monstrosity " +
                    magnitude + " (If this creature isn't monstrous, put " +
                    counters + " on it and it becomes monstrous.) | StackDescription$ SpellDescription" + ref;

            hostCard.addSpellAbility(AbilityFactory.getAbility(effect, hostCard));
        }

        else if (id == KeywordType.Level_up) {
            final Cost levelCost = getMyCost();
            final int maxLevel = Integer.parseInt(hostCard.getSVar("LevelMax"));

            String effect = "AB$ PutCounter | Cost$ " + levelCost + " | " +
                    "SorcerySpeed$ True | LevelUp$ True | CounterNum$ 1" +
                    " | CounterType$ LEVEL | PrecostDesc$ Level Up | MaxLevel$ " +
                    maxLevel + " | SpellDescription$ (Put a level counter on" +
                    " this permanent. Activate this ability only any time you" +
                    " could cast a sorcery.)";

            hostCard.addSpellAbility(AbilityFactory.getAbility(effect, hostCard));
        } // level up

        else if (id == KeywordType.Cycling) {
            hostCard.addSpellAbility(CardFactoryUtil.abilityCycle(hostCard, getMyCost().toString()));
        } // Cycling

        else if (id == KeywordType.TypeCycling) {
            hostCard.addSpellAbility(CardFactoryUtil.abilityTypecycle(hostCard, getMyCost().toString(), getType()));
        } // TypeCycling

        else if (id == KeywordType.Transmute) {
            final Cost manacost = getMyCost();
            final String sbTransmute = "AB$ ChangeZone | Cost$ " + manacost + " Discard<1/CARDNAME>"
                    + " | CostDesc$ Transmute " + manacost+ " | ActivationZone$ Hand"
                    + " | Origin$ Library | Destination$ Hand | ChangeType$ Card.cmcEQ" + hostCard.getManaCost().getCMC()
                    + " | ChangeNum$ 1 | SorcerySpeed$ True | References$ TransmuteX | SpellDescription$ ("
                    + manacost + ", Discard this card: Search your library for a card "
                    + "with the same converted mana cost as the discarded card, reveal that card, "
                    + "and put it into your hand. Then shuffle your library. Activate this ability "
                    + "only any time you could cast a sorcery.)";
            final SpellAbility abTransmute = AbilityFactory.getAbility(sbTransmute, hostCard);
            hostCard.addSpellAbility(abTransmute);
        } // transmute

        else if (id == KeywordType.Soulshift) {
            final int manacost = Integer.parseInt(getMagnitude());

            final String actualTrigger = "Mode$ ChangesZone | Origin$ Battlefield | Destination$ Graveyard"
                    + "| OptionalDecider$ You | ValidCard$ Card.Self | Execute$ SoulshiftAbility"
                    + "| TriggerController$ TriggeredCardController | TriggerDescription$ " + this.toString()
                    + " (When this creature dies, you may return target Spirit card with converted mana cost "
                    + manacost + " or less from your graveyard to your hand.)";
            final String abString = "DB$ ChangeZone | Origin$ Graveyard | Destination$ Hand"
                    + "| ValidTgts$ Spirit.YouOwn+cmcLE" + manacost;
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, hostCard, true);
            hostCard.addTrigger(parsedTrigger);
            hostCard.setSVar("SoulshiftAbility", abString);
        } // Soulshift 

        else if (id == KeywordType.Champion) {
            final String[] valid = getType().split(",");
            String desc = getOverridingDescription() != null ? getOverridingDescription() : getType();

            StringBuilder changeType = new StringBuilder();
            for (String v : valid) {
                if (changeType.length() != 0) {
                    changeType.append(",");
                }
                changeType.append(v).append(".YouCtrl+Other");
            }

            StringBuilder trig = new StringBuilder();
            trig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ValidCard$ Card.Self | ");
            trig.append("Execute$ ChampionAbility | TriggerDescription$ Champion a(n) ");
            trig.append(desc).append(" (When this enters the battlefield, sacrifice it unless you exile another ");
            trig.append(desc).append(" you control. When this leaves the battlefield, that card returns to the battlefield.)");

            StringBuilder trigReturn = new StringBuilder();
            trigReturn.append("Mode$ ChangesZone | Origin$ Battlefield | Destination$ Any | ValidCard$ Card.Self | ");
            trigReturn.append("Execute$ ChampionReturn | Secondary$ True | TriggerDescription$ When this leaves the battlefield, that card returns to the battlefield.");

            StringBuilder ab = new StringBuilder();
            ab.append("DB$ ChangeZone | Origin$ Battlefield | Destination$ Exile | RememberChanged$ True | Champion$ True | ");
            ab.append("Hidden$ True | Optional$ True | SubAbility$ ChampionSacrifice | ChangeType$ ").append(changeType);

            StringBuilder subAb = new StringBuilder();
            subAb.append("DB$ Sacrifice | Defined$ Card.Self | ConditionDefined$ Remembered | ConditionPresent$ Card | ConditionCompare$ EQ0");

            String returnChampion = "DB$ ChangeZone | Defined$ Remembered | Origin$ Exile | Destination$ Battlefield";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(trig.toString(), hostCard, this.isIntrinsic());
            final Trigger parsedTrigReturn = TriggerHandler.parseTrigger(trigReturn.toString(), hostCard, this.isIntrinsic());
            hostCard.addTrigger(parsedTrigger);
            hostCard.addTrigger(parsedTrigReturn);
            hostCard.setSVar("ChampionAbility", ab.toString());
            hostCard.setSVar("ChampionReturn", returnChampion);
            hostCard.setSVar("ChampionSacrifice", subAb.toString());
        }

        else if (id == KeywordType.Echo) {
            final Cost manacost = getMyCost();

            hostCard.setEchoCost(manacost.toString());

            final GameCommand intoPlay = new GameCommand() {

                private static final long serialVersionUID = -7913835645603984242L;

                @Override
                public void run() {
                    //hostCard.addKeyword(KeywordType.Echo_unpaid.getInstance(hostCard,false,false));
                }
            };
            hostCard.addComesIntoPlayCommand(intoPlay);
        } // echo

        else if (id == KeywordType.Suspend) {
            hostCard.setSuspend(true);

            final String timeCounters = getMagnitude();
            final Cost cost = getMyCost();
            hostCard.addSpellAbility(CardFactoryUtil.abilitySuspendStatic(hostCard, cost.toString(), timeCounters));
            CardFactoryUtil.addSuspendUpkeepTrigger(hostCard);
            CardFactoryUtil.addSuspendPlayTrigger(hostCard);
        } // Suspend

        else if (id == KeywordType.Fading) {

            //.addKeyword(KeywordType.parse("etbCounter:FADE:" + getMagnitude() + ":no Condition:no desc",hostCard,isIntrinsic()));

            String upkeepTrig = "Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | TriggerZones$ Battlefield " +
                    " | Execute$ TrigUpkeepFading | Secondary$ True | TriggerDescription$ At the beginning of " +
                    "your upkeep, remove a fade counter from CARDNAME. If you can't, sacrifice CARDNAME.";

            hostCard.setSVar("TrigUpkeepFading", "AB$ RemoveCounter | Cost$ 0 | Defined$ Self | CounterType$ FADE" +
                    " | CounterNum$ 1 | RememberRemoved$ True | SubAbility$ DBUpkeepFadingSac");
            hostCard.setSVar("DBUpkeepFadingSac","DB$ Sacrifice | SacValid$ Self | ConditionCheckSVar$ FadingCheckSVar" +
                    " | ConditionSVarCompare$ EQ0 | References$ FadingCheckSVar | SubAbility$ FadingCleanup");
            hostCard.setSVar("FadingCleanup","DB$ Cleanup | ClearRemembered$ True");
            hostCard.setSVar("FadingCheckSVar","Count$RememberedSize");
            final Trigger parsedUpkeepTrig = TriggerHandler.parseTrigger(upkeepTrig, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedUpkeepTrig);
        } // Fading

        else if (id == KeywordType.Vanishing) {
            //hostCard.addKeyword(KeywordType.parse("etbCounter:TIME:" + getMagnitude() + ":no Condition:no desc",hostCard,isIntrinsic()));
            // Remove Time counter trigger
            String upkeepTrig = "Mode$ Phase | Phase$ Upkeep | ValidPlayer$ You | " +
                    "TriggerZones$ Battlefield | IsPresent$ Card.Self+counters_GE1_TIME" +
                    " | Execute$ TrigUpkeepVanishing | TriggerDescription$ At the " +
                    "beginning of your upkeep, if CARDNAME has a time counter on it, " +
                    "remove a time counter from it. | Secondary$ True";
            hostCard.setSVar("TrigUpkeepVanishing", "AB$ RemoveCounter | Cost$ 0 | Defined$ Self" +
                    " | CounterType$ TIME | CounterNum$ 1");
            final Trigger parsedUpkeepTrig = TriggerHandler.parseTrigger(upkeepTrig, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedUpkeepTrig);
            // sacrifice trigger
            String sacTrig = "Mode$ CounterRemoved | TriggerZones$ Battlefield | ValidCard$" +
                    " Card.Self | NewCounterAmount$ 0 | Secondary$ True | CounterType$ TIME |" +
                    " Execute$ TrigVanishingSac | TriggerDescription$ When the last time " +
                    "counter is removed from CARDNAME, sacrifice it.";
            hostCard.setSVar("TrigVanishingSac", "AB$ Sacrifice | Cost$ 0 | SacValid$ Self");

            final Trigger parsedSacTrigger = TriggerHandler.parseTrigger(sacTrig, hostCard, false);
            hostCard.addTrigger(parsedSacTrigger);
        } // Vanishing


        else if (id == KeywordType.Delve) {
            hostCard.getSpellAbilities().get(0).setDelve(true);
        }

        else if (id == KeywordType.Haunt) {
            //CardFactoryUtil.setupHauntSpell(hostCard, this);
        }

        else if (id == KeywordType.Provoke) {
            final String actualTrigger = "Mode$ Attacks | ValidCard$ Card.Self | "
                    + "OptionalDecider$ You | Execute$ ProvokeAbility | Secondary$ True | TriggerDescription$ "
                    + "When this attacks, you may have target creature defending player "
                    + "controls untap and block it if able.";
            final String abString = "DB$ MustBlock | ValidTgts$ Creature.DefenderCtrl | "
                    + "TgtPrompt$ Select target creature defending player controls | SubAbility$ ProvokeUntap";
            final String dbString = "DB$ Untap | Defined$ Targeted";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedTrigger);
            hostCard.setSVar("ProvokeAbility", abString);
            hostCard.setSVar("ProvokeUntap", dbString);
        }

        else if (id == KeywordType.Living_weapon) {
            final StringBuilder sbTrig = new StringBuilder();
            sbTrig.append("Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | ");
            sbTrig.append("ValidCard$ Card.Self | Execute$ TrigGerm | TriggerDescription$ ");
            sbTrig.append("Living Weapon (When this Equipment enters the battlefield, ");
            sbTrig.append("put a 0/0 black Germ creature token onto the battlefield, then attach this to it.)");

            final StringBuilder sbGerm = new StringBuilder();
            sbGerm.append("DB$ Token | TokenAmount$ 1 | TokenName$ Germ | TokenTypes$ Creature,Germ | RememberTokens$ True | ");
            sbGerm.append("TokenOwner$ You | TokenColors$ Black | TokenPower$ 0 | TokenToughness$ 0 | TokenImage$ B 0 0 Germ | SubAbility$ DBGermAttach");

            final StringBuilder sbAttach = new StringBuilder();
            sbAttach.append("DB$ Attach | Defined$ Remembered | SubAbility$ DBGermClear");

            final StringBuilder sbClear = new StringBuilder();
            sbClear.append("DB$ Cleanup | ClearRemembered$ True");

            hostCard.setSVar("TrigGerm", sbGerm.toString());
            hostCard.setSVar("DBGermAttach", sbAttach.toString());
            hostCard.setSVar("DBGermClear", sbClear.toString());

            final Trigger etbTrigger = TriggerHandler.parseTrigger(sbTrig.toString(), hostCard, isIntrinsic());
            hostCard.addTrigger(etbTrigger);
        }

        else if (id == KeywordType.Epic) {
            //CardFactoryUtil.makeEpic(hostCard);
        }

        else if (id == KeywordType.Soulbond) {
            // Setup ETB trigger for card with Soulbond keyword
            final String actualTriggerSelf = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + "ValidCard$ Card.Self | Execute$ TrigBondOther | OptionalDecider$ You | "
                    + "IsPresent$ Creature.Other+YouCtrl+NotPaired | Secondary$ True | "
                    + "TriggerDescription$ When CARDNAME enters the battlefield, "
                    + "you may pair CARDNAME with another unpaired creature you control";
            final String abStringSelf = "AB$ Bond | Cost$ 0 | Defined$ Self | ValidCards$ Creature.Other+YouCtrl+NotPaired";
            final Trigger parsedTriggerSelf = TriggerHandler.parseTrigger(actualTriggerSelf, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedTriggerSelf);
            hostCard.setSVar("TrigBondOther", abStringSelf);
            // Setup ETB trigger for other creatures you control
            final String actualTriggerOther = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + "ValidCard$ Creature.Other+YouCtrl | TriggerZones$ Battlefield | OptionalDecider$ You | "
                    + "Execute$ TrigBondSelf | IsPresent$ Creature.Self+NotPaired | Secondary$ True | "
                    + " TriggerDescription$ When another unpaired creature you control enters the battlefield, "
                    + "you may pair it with CARDNAME";
            final String abStringOther = "AB$ Bond | Cost$ 0 | Defined$ TriggeredCard | ValidCards$ Creature.Self+NotPaired";
            final Trigger parsedTriggerOther = TriggerHandler.parseTrigger(actualTriggerOther, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedTriggerOther);
            hostCard.setSVar("TrigBondSelf", abStringOther);
        }

        else if (id == KeywordType.Extort) {
            final String extortTrigger = "Mode$ SpellCast | ValidCard$ Card | ValidActivatingPlayer$ You | "
                    + "TriggerZones$ Battlefield | Execute$ ExtortOpps | Secondary$ True"
                    + " | TriggerDescription$ Extort (Whenever you cast a spell, you may pay W/B. If you do, "
                    + "each opponent loses 1 life and you gain that much life.)";
            final String abString = "AB$ LoseLife | Cost$ WB | Defined$ Player.Opponent | "
                    + "LifeAmount$ 1 | SubAbility$ ExtortGainLife";
            final String dbString = "DB$ GainLife | Defined$ You | LifeAmount$ AFLifeLost | References$ AFLifeLost";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(extortTrigger, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedTrigger);
            hostCard.setSVar("ExtortOpps", abString);
            hostCard.setSVar("ExtortGainLife", dbString);
            hostCard.setSVar("AFLifeLost", "Number$0");
        }

        else if (id == KeywordType.Evolve) {
            final String evolveTrigger = "Mode$ ChangesZone | Origin$ Any | Destination$ Battlefield | "
                    + " ValidCard$ Creature.YouCtrl+Other | EvolveCondition$ True | "
                    + "TriggerZones$ Battlefield | Execute$ EvolveAddCounter | Secondary$ True | "
                    + "TriggerDescription$ Evolve (Whenever a creature enters the battlefield under your "
                    + "control, if that creature has greater power or toughness than this creature, put a "
                    + "+1/+1 counter on this creature.)";
            final String abString = "AB$ PutCounter | Cost$ 0 | Defined$ Self | CounterType$ P1P1 | "
                    + "CounterNum$ 1 | Evolve$ True";
            final Trigger parsedTrigger = TriggerHandler.parseTrigger(evolveTrigger, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedTrigger);
            hostCard.setSVar("EvolveAddCounter", abString);
        }

        else if (id == KeywordType.Dredge) {
            //final int dredgeAmount = hostCard.getKeywordMagnitude(KeywordType.Dredge);

            //final String actualRep = "Event$ Draw | ActiveZones$ Graveyard | ValidPlayer$ You | "
            //        + "ReplaceWith$ DredgeCards | Secondary$ True | Optional$ True | CheckSVar$ "
            //        + "DredgeCheckLib | SVarCompare$ GE" + dredgeAmount + " | References$ "
            //        + "DredgeCheckLib | AICheckDredge$ True | Description$ " + hostCard.getName()
            //        +  " - Dredge " + dredgeAmount;
            //final String abString = "DB$ Mill | Defined$ You | NumCards$ " + dredgeAmount + " | "
            //        + "SubAbility$ DredgeMoveToPlay";
            final String moveToPlay = "DB$ ChangeZone | Origin$ Graveyard | Destination$ Hand | "
                    + "Defined$ Self";
            final String checkSVar = "Count$ValidLibrary Card.YouOwn";
            //hostCard.setSVar("DredgeCards", abString);
            hostCard.setSVar("DredgeMoveToPlay", moveToPlay);
            hostCard.setSVar("DredgeCheckLib", checkSVar);
            //hostCard.addReplacementEffect(ReplacementHandler.parseReplacement(actualRep, hostCard, isIntrinsic()));
        }

        else if (id == KeywordType.Tribute) {
            //final int tributeAmount = hostCard.getKeywordMagnitude(KeywordType.Tribute);

            //final String actualRep = "Event$ Moved | Destination$ Battlefield | ValidCard$ Card.Self |"
            //        + " ReplaceWith$ TributeAddCounter | Secondary$ True | Description$ Tribute "
            //        + tributeAmount + " (As this creature enters the battlefield, an opponent of your"
            //        + " choice may place " + tributeAmount + " +1/+1 counter on it.)";
            //final String abString = "DB$ PutCounter | Defined$ ReplacedCard | Tribute$ True | "
            //        + "CounterType$ P1P1 | CounterNum$ " + tributeAmount
            //        + " | SubAbility$ TributeMoveToPlay";
            //final String moveToPlay = "DB$ ChangeZone | Origin$ All | Destination$ Battlefield | "
            //        + "Defined$ ReplacedCard | Hidden$ True";
            //hostCard.setSVar("TributeAddCounter", abString);
            //hostCard.setSVar("TributeMoveToPlay", moveToPlay);
            //hostCard.addReplacementEffect(ReplacementHandler.parseReplacement(actualRep, hostCard, isIntrinsic()));
        }

        else if (id == KeywordType.Amplify) {
            final String amplifyMagnitude = getMagnitude();
            final String suffix = !amplifyMagnitude.equals("1") ? "s" : "";
            final String ampTypes = getType();
            String[] refinedTypes = ampTypes.split(",");
            final StringBuilder types = new StringBuilder();
            for (int i = 0; i < refinedTypes.length; i++) {
                types.append("Card.").append(refinedTypes[i]).append("+YouCtrl");
                if (i + 1 != refinedTypes.length) {
                    types.append(",");
                }
            }
            // Setup ETB replacement effects
            final String actualRep = "Event$ Moved | Destination$ Battlefield | ValidCard$ Card.Self |"
                    + " ReplaceWith$ AmplifyReveal | Secondary$ True | Description$ As this creature "
                    + "enters the battlefield, put " + amplifyMagnitude + " +1/+1 counter" + suffix
                    + " on it for each " + ampTypes.replace(",", " and/or ")
                    + " card you reveal in your hand.)";
            final String abString = "DB$ Reveal | AnyNumber$ True | RevealValid$ "
                    + types.toString() + " | RememberRevealed$ True | SubAbility$ AmplifyMoveToPlay";
            final String moveToPlay = "DB$ ChangeZone | Origin$ All | Destination$ Battlefield | "
                    + "Defined$ ReplacedCard | Hidden$ True | SubAbility$ Amplify";
            final String dbString = "DB$ PutCounter | Defined$ ReplacedCard | CounterType$ P1P1 | "
                    + "CounterNum$ AmpMagnitude | References$ Revealed,AmpMagnitude | SubAbility$"
                    + " DBCleanup";
            hostCard.addReplacementEffect(ReplacementHandler.parseReplacement(actualRep, hostCard, isIntrinsic()));
            hostCard.setSVar("AmplifyReveal", abString);
            hostCard.setSVar("AmplifyMoveToPlay", moveToPlay);
            hostCard.setSVar("Amplify", dbString);
            hostCard.setSVar("DBCleanup", "DB$ Cleanup | ClearRemembered$ True");
            hostCard.setSVar("AmpMagnitude", "SVar$Revealed/Times." + amplifyMagnitude);
            hostCard.setSVar("Revealed", "Remembered$Amount");
        }

        else if (id == KeywordType.Equip || id == KeywordType.Fortify) {
            // Create attach ability string
            final StringBuilder abilityStr = new StringBuilder();
            abilityStr.append("AB$ Attach | Cost$ ");
            abilityStr.append(getMyCost());
            abilityStr.append(" | ValidTgts$ " + (id == KeywordType.Equip ? "Creature" : "Land") + ".YouCtrl | TgtPrompt$ Select target creature you control ");
            abilityStr.append("| SorcerySpeed$ True | Equip$ True | AILogic$ Pump | IsPresent$ Card.Self+nonCreature ");
            if (getRawString() != null) {
                abilityStr.append("| ").append(getRawString()).append(" ");
            }
            abilityStr.append("| PrecostDesc$ " + id.toString() + " ");
            Cost cost = getMyCost();
            if (!cost.isOnlyManaCost()) { //Something other than a mana cost
                abilityStr.append("- ");
            }
            abilityStr.append("| CostDesc$ " + cost.toSimpleString() + " ");
            abilityStr.append("| SpellDescription$ (" + cost.toSimpleString() + ": Attach to target creature you control. Equip only as a sorcery.)");
            // instantiate attach ability
            final SpellAbility sa = AbilityFactory.getAbility(abilityStr.toString(), hostCard);
            hostCard.addSpellAbility(sa);
        }

        else if (id == KeywordType.Bestow) {
            final Cost cost = getMyCost();

            final StringBuilder sbAttach = new StringBuilder();
            sbAttach.append("SP$ Attach | Cost$ ");
            sbAttach.append(cost);
            sbAttach.append(" | AILogic$ Pump | Bestow$ True | ValidTgts$ Creature");
            final SpellAbility bestow = AbilityFactory.getAbility(sbAttach.toString(), hostCard);

            bestow.setDescription("Bestow " + cost + " (If you cast this"
                    + " card for its bestow cost, it's an Aura spell with enchant creature. It"
                    + " becomes a creature again if it's not attached to a creature.)");
            bestow.setStackDescription("Bestow - " + hostCard.getName());
            bestow.setBasicSpell(false);
            hostCard.addSpellAbility(bestow);
        }
        else if (id == KeywordType.etbReplacement) {
            ReplacementLayer layer = getReplacementLayer();
            SpellAbility repAb = AbilityFactory.getAbility(getRawString(0), hostCard);
            String desc = repAb.getDescription();
            CardFactoryUtil.setupETBReplacementAbility(repAb);

            final String valid = getType();

            StringBuilder repEffsb = new StringBuilder();
            repEffsb.append("Event$ Moved | ValidCard$ ").append(valid);
            repEffsb.append(" | Destination$ Battlefield | Description$ ").append(desc);
            if (hasBoolean()) {
                if(getMyBoolean()) {
                    repEffsb.append(" | Optional$ True");
                }
            }
            if (true) {
                if (hasZone()) {
                    repEffsb.append(" | ActiveZones$ " + getZone());
                }
            }

            ReplacementEffect re = ReplacementHandler.parseReplacement(repEffsb.toString(), hostCard, isIntrinsic());
            re.setLayer(layer);
            re.setOverridingAbility(repAb);

            hostCard.addReplacementEffect(re);
        }
        else if (id == KeywordType.etbCounter) {

            String desc = getOverridingDescription().equals("no desc") ? "CARDNAME enters the battlefield with " + getMagnitude() + " "
                    + getCounterType() + " counters on it." : getOverridingDescription();
            String extraparams = getRawString();
            String amount = getMagnitude();

            String abStr = "AB$ ChangeZone | Cost$ 0 | Hidden$ True | Origin$ All | Destination$ Battlefield"
                    + "| Defined$ ReplacedCard | SubAbility$ ETBCounterDBSVar";
            String dbStr = "DB$ PutCounter | Defined$ Self | CounterType$ " + getCounterType() + " | CounterNum$ " + amount;
            try {
                Integer.parseInt(amount);
            }
            catch (NumberFormatException ignored) {
                dbStr += " | References$ " + amount;
            }
            hostCard.setSVar("ETBCounterSVar", abStr);
            hostCard.setSVar("ETBCounterDBSVar", dbStr);

            String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield "
                    + "| ReplaceWith$ ETBCounterSVar | Description$ " + desc + (!extraparams.equals("") ? " | " + extraparams : "");

            ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, hostCard, isIntrinsic());
            re.setLayer(ReplacementLayer.Other);

            hostCard.addReplacementEffect(re);
        }
        else if (id == KeywordType.CARDNAME_enters_the_battlefield_tapped) {
            String abStr = "AB$ Tap | Cost$ 0 | Defined$ Self | ETB$ True | SubAbility$ MoveETB";
            String dbStr = "DB$ ChangeZone | Hidden$ True | Origin$ All | Destination$ Battlefield"
                    + "| Defined$ ReplacedCard";

            hostCard.setSVar("ETBTappedSVar", abStr);
            hostCard.setSVar("MoveETB", dbStr);

            String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield "
                    + "| ReplaceWith$ ETBTappedSVar | Description$ CARDNAME enters the battlefield tapped.";

            ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, hostCard, isIntrinsic());
            re.setLayer(ReplacementLayer.Other);

            hostCard.addReplacementEffect(re);
        }
        else if (id == KeywordType.Sunburst) {
            final GameCommand sunburstCIP = new GameCommand() {
                private static final long serialVersionUID = 1489845860231758299L;

                @Override
                public void run() {
                    if (hostCard.isCreature()) {
                        hostCard.addCounter(CounterType.P1P1, hostCard.getSunburstValue(), true);
                    } else {
                        hostCard.addCounter(CounterType.CHARGE, hostCard.getSunburstValue(), true);
                    }

                }
            };

            final GameCommand sunburstLP = new GameCommand() {
                private static final long serialVersionUID = -7564420917490677427L;

                @Override
                public void run() {
                    hostCard.setSunburstValue(0);
                }
            };

            hostCard.addComesIntoPlayCommand(sunburstCIP);
            hostCard.addLeavesPlayCommand(sunburstLP);
        }
        else if (id == KeywordType.Morph) {
            Map<String, String> sVars = hostCard.getSVars();
            final Cost cost = getMyCost();

            hostCard.addSpellAbility(CardFactoryUtil.abilityMorphDown(hostCard));

            hostCard.turnFaceDown();

            hostCard.addSpellAbility(CardFactoryUtil.abilityMorphUp(hostCard, cost));
            hostCard.setSVars(sVars); // for Warbreak Trumpeter.

            hostCard.setState(CardCharacteristicName.Original);
        } // Morph
        else if (id == KeywordType.Unearth) {
            //hostCard.addSpellAbility(CardFactoryUtil.abilityUnearth(hostCard, getMyCost()));
        } // unearth
        else if (id == KeywordType.Madness) {
            // Set Madness Replacement effects
            String repeffstr = "Event$ Discard | ActiveZones$ Hand | ValidCard$ Card.Self | " +
                    "ReplaceWith$ DiscardMadness | Secondary$ True | Description$ If you would" +
                    " discard this card, you discard it, but may exile it instead of putting it" +
                    " into your graveyard";
            ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, hostCard, isIntrinsic());
            hostCard.addReplacementEffect(re);
            String sVarMadness = "DB$ Discard | Defined$ ReplacedPlayer" +
                    " | Mode$ Defined | DefinedCards$ ReplacedCard | Madness$ True";
            hostCard.setSVar("DiscardMadness", sVarMadness);

            String trigStr = "Mode$ Discarded | ValidCard$ Card.Self | IsMadness$ True | " +
                    "Execute$ TrigPlayMadness | Secondary$ True | TriggerDescription$ " +
                    "Play Madness - " + hostCard.getName();
            final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, hostCard, isIntrinsic());
            hostCard.addTrigger(myTrigger);
            String playMadness = "AB$ Play | Cost$ 0 | Defined$ Self | PlayMadness$ " + getMyCost() +
                    " | Optional$ True | SubAbility$ DBWasNotPlayMadness | RememberPlayed$ True";
            String moveToYard = "DB$ ChangeZone | Defined$ Self | Origin$ Exile | " +
                    "Destination$ Graveyard | ConditionDefined$ Remembered | ConditionPresent$" +
                    " Card | ConditionCompare$ EQ0 | SubAbility$ DBMadnessCleanup";
            String cleanUp = "DB$ Cleanup | ClearRemembered$ True";
            hostCard.setSVar("TrigPlayMadness", playMadness);
            hostCard.setSVar("DBWasNotPlayMadness", moveToYard);
            hostCard.setSVar("DBMadnessCleanup", cleanUp);
        } // madness
        else if (id == KeywordType.Miracle) {
            hostCard.setMiracleCost(getMyCost());
        } // miracle
        else if (id == KeywordType.Devour) {
            String abStr = "AB$ ChangeZone | Cost$ 0 | Hidden$ True | Origin$ All | "
                    + "Destination$ Battlefield | Defined$ ReplacedCard | SubAbility$ DevourSac";
            String dbStr = "DB$ Sacrifice | Defined$ You | Amount$ DevourSacX | "
                    + "References$ DevourSacX | SacValid$ Creature.Other | SacMessage$ creature (Devour "
                    + getMagnitude() + ") | RememberSacrificed$ True | Optional$ True | "
                    + "Devour$ True | SubAbility$ DevourCounters";
            String counterStr = "DB$ PutCounter | Defined$ Self | CounterType$ P1P1 | CounterNum$ DevourX"
                    + " | References$ DevourX,DevourSize | SubAbility$ DevourCleanup";

            hostCard.setSVar("DevourETB", abStr);
            hostCard.setSVar("DevourSac", dbStr);
            hostCard.setSVar("DevourSacX", "Count$Valid Creature.YouCtrl+Other");
            hostCard.setSVar("DevourCounters", counterStr);
            hostCard.setSVar("DevourX", "SVar$DevourSize/Times." + getMagnitude());
            hostCard.setSVar("DevourSize", "Count$RememberedSize");
            hostCard.setSVar("DevourCleanup", "DB$ Cleanup | ClearRemembered$ True");

            String repeffstr = "Event$ Moved | ValidCard$ Card.Self | Destination$ Battlefield | ReplaceWith$ DevourETB";

            ReplacementEffect re = ReplacementHandler.parseReplacement(repeffstr, hostCard, isIntrinsic());
            re.setLayer(ReplacementLayer.Other);
            hostCard.addReplacementEffect(re);
        } // Devour
        else if (id == KeywordType.Modular) {
           // hostCard.addKeyword(KeywordType.parse("etbCounter:P1P1:" + getMagnitude() + ":no Condition: " +
           //         "Modular " + getMagnitude() + " (This enters the battlefield with " + getMagnitude() + " +1/+1 counters on it. When it's put into a graveyard, " +
           //         "you may put its +1/+1 counters on target artifact creature.)",hostCard,isIntrinsic()));

            final String abStr = "AB$ PutCounter | Cost$ 0 | References$ ModularX | ValidTgts$ Artifact.Creature | " +
                    "TgtPrompt$ Select target artifact creature | CounterType$ P1P1 | CounterNum$ ModularX";
            hostCard.setSVar("ModularTrig", abStr);
            hostCard.setSVar("ModularX", "TriggeredCard$CardCounters.P1P1");

            String trigStr = "Mode$ ChangesZone | ValidCard$ Card.Self | Origin$ Battlefield | Destination$ Graveyard" +
                    " | OptionalDecider$ TriggeredCardController | TriggerController$ TriggeredCardController | Execute$ ModularTrig | " +
                    "Secondary$ True | TriggerDescription$ When CARDNAME is put into a graveyard from the battlefield, " +
                    "you may put a +1/+1 counter on target artifact creature for each +1/+1 counter on CARDNAME";
            final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, hostCard, isIntrinsic());
            hostCard.addTrigger(myTrigger);
        } // Modular
        else if (id == KeywordType.Graft) {
            final String abStr = "AB$ MoveCounter | Cost$ 0 | Source$ Self | "
                    + "Defined$ TriggeredCard | CounterType$ P1P1 | CounterNum$ 1";
            hostCard.setSVar("GraftTrig", abStr);

            String trigStr = "Mode$ ChangesZone | ValidCard$ Creature.Other | "
                    + "Origin$ Any | Destination$ Battlefield"
                    + " | TriggerZones$ Battlefield | OptionalDecider$ You | "
                    + "IsPresent$ Card.Self+counters_GE1_P1P1 | "
                    + "Execute$ GraftTrig | TriggerDescription$ "
                    + "Whenever another creature enters the battlefield, you "
                    + "may move a +1/+1 counter from this creature onto it.";
            final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, hostCard, isIntrinsic());
            hostCard.addTrigger(myTrigger);

            //hostCard.addKeyword(KeywordType.parse("etbCounter:P1P1:" + getMagnitude(),hostCard,isIntrinsic()));
        }
        else if (id == KeywordType.Bloodthirst) {
            String desc = "Bloodthirst "
                    + getMagnitude() + " (If an opponent was dealt damage this turn, this creature enters the battlefield with "
                    + getMagnitude() + " +1/+1 counters on it.)";
            if (getMagnitude().equals("X")) {
                desc = "Bloodthirst X (This creature enters the battlefield with X +1/+1 counters on it, "
                        + "where X is the damage dealt to your opponents this turn.)";
                hostCard.setSVar("X", "Count$BloodthirstAmount");
            }

            //hostCard.addKeyword(KeywordType.parse("etbCounter:P1P1:" + getMagnitude() + ":Bloodthirst$ True:" + desc,hostCard,isIntrinsic()));
        } // bloodthirst
        else if (id == KeywordType.Storm) {
            final StringBuilder trigScript = new StringBuilder(
                    "Mode$ SpellCast | ValidCard$ Card.Self | Execute$ Storm "
                            + "| TriggerDescription$ Storm (When you cast this spell, "
                            + "copy it for each spell cast before it this turn.)");

            hostCard.setSVar("Storm", "AB$ CopySpellAbility | Cost$ 0 | Defined$ TriggeredSpellAbility | Amount$ StormCount | References$ StormCount");
            hostCard.setSVar("StormCount", "Count$StormCount");
            final Trigger stormTrigger = TriggerHandler.parseTrigger(trigScript.toString(), hostCard, isIntrinsic());

            hostCard.addTrigger(stormTrigger);
        } // Storm
        else if (id == KeywordType.Cascade) {
            final StringBuilder trigScript = new StringBuilder(
                    "Mode$ SpellCast | ValidCard$ Card.Self | Execute$ TrigCascade | Secondary$ " +
                            "True | TriggerDescription$ Cascade - CARDNAME");

            final String abString = "AB$ DigUntil | Cost$ 0 | Defined$ You | Amount$ 1 | Valid$ "
                    + "Card.nonLand+cmcLTCascadeX | FoundDestination$ Exile | RevealedDestination$"
                    + " Exile | References$ CascadeX | ImprintRevealed$ True | RememberFound$ True"
                    + " | SubAbility$ CascadeCast";
            final String dbCascadeCast = "DB$ Play | Defined$ Remembered | WithoutManaCost$ True | "
                    + "Optional$ True | SubAbility$ CascadeMoveToLib";
            final String dbMoveToLib = "DB$ ChangeZoneAll | ChangeType$ Card.IsRemembered,Card.IsImprinted"
                    + " | Origin$ Exile | Destination$ Library | RandomOrder$ True | LibraryPosition$ -1"
                    + " | SubAbility$ CascadeCleanup";
            hostCard.setSVar("TrigCascade", abString);
            hostCard.setSVar("CascadeCast", dbCascadeCast);
            hostCard.setSVar("CascadeMoveToLib", dbMoveToLib);
            hostCard.setSVar("CascadeX", "Count$CardManaCost");
            hostCard.setSVar("CascadeCleanup", "DB$ Cleanup | ClearRemembered$ True | ClearImprinted$ True");
            final Trigger cascadeTrigger = TriggerHandler.parseTrigger(trigScript.toString(), hostCard, isIntrinsic());

            hostCard.addTrigger(cascadeTrigger);
        } // Cascade
        else if (id == KeywordType.Recover) {
            final String abStr = "AB$ ChangeZone | Cost$ 0 | Defined$ Self"
                    + " | Origin$ Graveyard | Destination$ Hand | UnlessCost$ "
                    + getMyCost() + " | UnlessPayer$ You | UnlessSwitched$ True"
                    + " | UnlessResolveSubs$ WhenNotPaid | SubAbility$ RecoverExile";
            hostCard.setSVar("RecoverTrig", abStr);
            hostCard.setSVar("RecoverExile", "DB$ ChangeZone | Defined$ Self"
                    + " | Origin$ Graveyard | Destination$ Exile");
            String trigObject = hostCard.isCreature() ? "Creature.Other+YouOwn" : "Creature.YouOwn";
            String trigArticle = hostCard.isCreature() ? "another" : "a";
            String trigStr = "Mode$ ChangesZone | ValidCard$ " + trigObject
                    + " | Origin$ Battlefield | Destination$ Graveyard | "
                    + "TriggerZones$ Graveyard | Execute$ RecoverTrig | "
                    + "TriggerDescription$ When " + trigArticle + " creature is "
                    + "put into your graveyard from the battlefield, you "
                    + "may pay " + getMyCost() + ". If you do, return "
                    + "CARDNAME from your graveyard to your hand. Otherwise,"
                    + " exile CARDNAME. | Secondary$ True";
            final Trigger myTrigger = TriggerHandler.parseTrigger(trigStr, hostCard, false);
            hostCard.addTrigger(myTrigger);
        } // Recover
        else if (id == KeywordType.Ripple) {
            UUID triggerSvar = UUID.randomUUID();

            final String actualTrigger = "Mode$ SpellCast | ValidCard$ Card.Self | " +
                    "Execute$ " + triggerSvar + " | Secondary$ True | TriggerDescription$" +
                    " Ripple " + getMagnitude() + " - CARDNAME | OptionalDecider$ You";
            final String abString = "AB$ Dig | Cost$ 0 | NoMove$ True | DigNum$ " + getMagnitude() +
                    " | Reveal$ True | RememberRevealed$ True | SubAbility$ DBCastRipple";
            final String dbCast = "DB$ Play | Valid$ Card.IsRemembered+sameName | " +
                    "ValidZone$ Library | WithoutManaCost$ True | Optional$ True | " +
                    "Amount$ All | SubAbility$ RippleMoveToBottom";

            hostCard.setSVar(triggerSvar.toString(), abString);
            hostCard.setSVar("DBCastRipple", dbCast);
            hostCard.setSVar("RippleMoveToBottom", "DB$ ChangeZoneAll | ChangeType$ " +
                    "Card.IsRemembered | Origin$ Library | Destination$ Library | " +
                    "LibraryPosition$ -1 | SubAbility$ RippleCleanup");
            hostCard.setSVar("RippleCleanup", "DB$ Cleanup | ClearRemembered$ True");

            final Trigger parsedTrigger = TriggerHandler.parseTrigger(actualTrigger, hostCard, isIntrinsic());
            hostCard.addTrigger(parsedTrigger);
        } // Ripple
    }

    public KeywordInstance getCopy() {
        KeywordInstance ki = new KeywordInstance(this.getHostCard(),this.isIntrinsic(),this.isHidden,this.getId());
        ki.magnitude.addAll(magnitude);
        ki.myCost.addAll(myCost);
        ki.type.addAll(type);
        ki.replacementLayer.addAll(replacementLayer);
        ki.counterType.addAll(counterType);
        ki.zone.addAll(zone);
        ki.rawString.addAll(rawString);
        ki.myBoolean.addAll(myBoolean);
        ki.overridingDescription.addAll(overridingDescription);

        return ki;
    }

    public KeywordType getId() { return id; }

    public boolean isHidden() {
        return isHidden;
    }

    public boolean hasCost() { return myCost.size() > 0; }

    public Cost getMyCost(int i) { return myCost.get(i); }

    public Cost getMyCost() { return myCost.get(0); }

    public void addCost(Cost c) { myCost.add(c); }

    public boolean hasMagnitude() { return magnitude.size() > 0; }

    public String getMagnitude(int i) { return magnitude.get(i); }

    public String getMagnitude() { return magnitude.get(0); }

    public void addMagnitude(String s) { magnitude.add(s); }

    public boolean hasType() { return type.size() > 0; }

    public String getType(int i) { return type.get(i); }

    public String getType() { return type.get(0); }

    public void addType(String s) { type.add(s);}

    public boolean hasOverridingDescription() { return overridingDescription.size() > 0; }

    public String getOverridingDescription(int i) { return overridingDescription.get(i); }

    public String getOverridingDescription() { return overridingDescription.get(0); }

    public void addOverridingDescription(String s) { overridingDescription.add(s);}

    public boolean hasRawString() { return rawString.size() > 0; }

    public String getRawString(int i) { return rawString.get(i); }

    public String getRawString() { return rawString.get(0); }

    public void addRawString(String s) { rawString.add(s); }

    public boolean hasReplacement() { return replacementLayer.size() > 0; }

    public ReplacementLayer getReplacementLayer(int i) { return replacementLayer.get(i); }

    public ReplacementLayer getReplacementLayer() { return replacementLayer.get(0); }

    public void addReplacementLayer(ReplacementLayer r) { replacementLayer.add(r); }

    public boolean hasCounterType() { return counterType.size() > 0; }

    public CounterType getCounterType(int i) { return counterType.get(i); }

    public CounterType getCounterType() { return counterType.get(0); }

    public void addCounterType(CounterType c) { counterType.add(c); }

    public boolean hasBoolean() { return myBoolean.size() > 0; }

    public Boolean getMyBoolean(int i) { return myBoolean.get(i); }

    public Boolean getMyBoolean() { return myBoolean.get(0); }

    public void addBoolean(Boolean b) { myBoolean.add(b); }

    public boolean hasZone() { return zone.size() > 0; }

    public ZoneType getZone(int i) { return zone.get(i); }

    public ZoneType getZone() { return zone.get(0); }

    public void addZone(ZoneType z) { zone.add(z); }

    public String getScript() { return script; }

    public void setScript(String script) { this.script = script; }

}
