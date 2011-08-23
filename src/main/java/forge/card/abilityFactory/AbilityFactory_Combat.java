package forge.card.abilityFactory;

import java.util.ArrayList;
import java.util.HashMap;

import forge.AllZone;
import forge.Card;
import forge.CombatUtil;
import forge.ComputerUtil;
import forge.Constant;
import forge.Player;
import forge.card.spellability.Ability_Activated;
import forge.card.spellability.Ability_Sub;
import forge.card.spellability.Spell;
import forge.card.spellability.SpellAbility;
import forge.card.spellability.Target;

/**
 * <p>AbilityFactory_Combat class.</p>
 *
 * @author Forge
 * @version $Id$
 */
public class AbilityFactory_Combat {
    //**************************************************************
    // ****************************** FOG **************************
    //**************************************************************

    /**
     * <p>createAbilityFog.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createAbilityFog(final AbilityFactory AF) {
        final SpellAbility abFog = new Ability_Activated(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
            private static final long serialVersionUID = -1933592438783630254L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return fogStackDescription(af, this);
            }

            public boolean canPlayAI() {
                return fogCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                fogResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return fogDoTriggerAI(af, this, mandatory);
            }

        };
        return abFog;
    }

    /**
     * <p>createSpellFog.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellFog(final AbilityFactory AF) {
        final SpellAbility spFog = new Spell(AF.getHostCard(), AF.getAbCost(), AF.getAbTgt()) {
            private static final long serialVersionUID = -5141246507533353605L;

            final AbilityFactory af = AF;

            @Override
            public String getStackDescription() {
                // when getStackDesc is called, just build exactly what is happening
                return fogStackDescription(af, this);
            }

            public boolean canPlayAI() {
                return fogCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                fogResolve(af, this);
            }

        };
        return spFog;
    }

    /**
     * <p>createDrawbackFog.</p>
     *
     * @param AF a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackFog(final AbilityFactory AF) {
        final SpellAbility dbFog = new Ability_Sub(AF.getHostCard(), AF.getAbTgt()) {
            private static final long serialVersionUID = -5141246507533353605L;

            final AbilityFactory af = AF;

            @Override
            public void resolve() {
                fogResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return fogPlayDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return fogDoTriggerAI(af, this, mandatory);
            }

        };
        return dbFog;
    }

    /**
     * <p>fogStackDescription.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a {@link java.lang.String} object.
     */
    public static String fogStackDescription(AbilityFactory af, SpellAbility sa) {
        StringBuilder sb = new StringBuilder();

        if (!(sa instanceof Ability_Sub))
            sb.append(sa.getSourceCard().getName()).append(" - ");
        else
            sb.append(" ");

        sb.append(sa.getSourceCard().getController());
        sb.append(" prevents all combat damage this turn.");

        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }

    /**
     * <p>fogCanPlayAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean fogCanPlayAI(final AbilityFactory af, SpellAbility sa) {
        // AI should only activate this during Human's Declare Blockers phase
        if (AllZone.getPhase().isPlayerTurn(sa.getActivatingPlayer())) return false;
        if (!AllZone.getPhase().is(Constant.Phase.Combat_Declare_Blockers_InstantAbility)) return false;

        // Only cast when Stack is empty, so Human uses spells/abilities first
        if (AllZone.getStack().size() != 0) return false;

        // Don't cast it, if the effect is already in place
        if (AllZone.getPhase().isPreventCombatDamageThisTurn()) return false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            if (!subAb.chkAI_Drawback()) return false;

        // Cast it if life is in danger
        return CombatUtil.lifeInDanger(AllZone.getCombat());
    }

    /**
     * <p>fogPlayDrawbackAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @return a boolean.
     */
    public static boolean fogPlayDrawbackAI(final AbilityFactory af, SpellAbility sa) {
        // AI should only activate this during Human's turn
        boolean chance;
        if (AllZone.getPhase().isPlayerTurn(sa.getActivatingPlayer().getOpponent()))
            chance = AllZone.getPhase().isBefore(Constant.Phase.Combat_FirstStrikeDamage);
        else
            chance = AllZone.getPhase().isAfter(Constant.Phase.Combat_Damage);

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance;
    }

    /**
     * <p>fogDoTriggerAI.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     * @param mandatory a boolean.
     * @return a boolean.
     */
    public static boolean fogDoTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa) && !mandatory)    // If there is a cost payment it's usually not mandatory
            return false;

        boolean chance;
        if (AllZone.getPhase().isPlayerTurn(sa.getActivatingPlayer().getOpponent()))
            chance = AllZone.getPhase().isBefore(Constant.Phase.Combat_FirstStrikeDamage);
        else
            chance = AllZone.getPhase().isAfter(Constant.Phase.Combat_Damage);

        // check SubAbilities DoTrigger?
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            return chance && abSub.doTrigger(mandatory);
        }

        return chance;
    }

    /**
     * <p>fogResolve.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @param sa a {@link forge.card.spellability.SpellAbility} object.
     */
    public static void fogResolve(final AbilityFactory af, final SpellAbility sa) {

        // Expand Fog keyword here depending on what we need out of it.
        AllZone.getPhase().setPreventCombatDamageThisTurn(true);
    }
    
    //**************************************************************
    //*********************** MUSTATTACK ***************************
    //**************************************************************
    
    //AB$ MustAttack | Cost$ R T | ValidTgts$ Opponent | TgtPrompt$ Select target opponent | Defender$ Self | SpellDescription$ ...

    /**
     * <p>createAbilityMustAttack</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     * 
     * @since 1.1.01
     */
    public static SpellAbility createAbilityMustAttack(final AbilityFactory af) {
        final SpellAbility abMustAttack = new Ability_Activated(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
			private static final long serialVersionUID = 4559154732470225755L;

			@Override
            public String getStackDescription() {
                return mustAttackStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return mustAttackCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                mustAttackResolve(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return mustAttackDoTriggerAI(af, this, mandatory);
            }

        };
        return abMustAttack;
    }

    /**
     * <p>createSpellMustAttack.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createSpellMustAttack(final AbilityFactory af) {
        final SpellAbility spMustAttack = new Spell(af.getHostCard(), af.getAbCost(), af.getAbTgt()) {
			private static final long serialVersionUID = 4103945257601008403L;

			@Override
            public String getStackDescription() {
                return mustAttackStackDescription(af, this);
            }

            @Override
            public boolean canPlayAI() {
                return mustAttackCanPlayAI(af, this);
            }

            @Override
            public void resolve() {
                mustAttackResolve(af, this);
            }

        };
        return spMustAttack;
    }

    /**
     * <p>createDrawbackMustAttack.</p>
     *
     * @param af a {@link forge.card.abilityFactory.AbilityFactory} object.
     * @return a {@link forge.card.spellability.SpellAbility} object.
     */
    public static SpellAbility createDrawbackMustAttack(final AbilityFactory af) {
        final SpellAbility dbMustAttack = new Ability_Sub(af.getHostCard(), af.getAbTgt()) {
			private static final long serialVersionUID = 1294949210616598158L;

			@Override
            public void resolve() {
                mustAttackResolve(af, this);
            }

            @Override
            public boolean chkAI_Drawback() {
                return mustAttackPlayDrawbackAI(af, this);
            }

            @Override
            public boolean doTrigger(boolean mandatory) {
                return mustAttackDoTriggerAI(af, this, mandatory);
            }

        };
        return dbMustAttack;
    }
    
    private static String mustAttackStackDescription(AbilityFactory af, SpellAbility sa) {
    	HashMap<String,String> params = af.getMapParams();
    	Card host = af.getHostCard();
        StringBuilder sb = new StringBuilder();

        if(sa instanceof Ability_Sub)
        	sb.append(" ");
        else
        	sb.append(sa.getSourceCard()).append(" - ");
        
        //end standard pre-
        
        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null)
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);
        
        String defender = null;
        if(params.get("Defender").equals("Self")) {
        	defender = host.toString();
        }
        else {
        	//TODO - if more needs arise in the future
        }

        for(Player player : tgtPlayers) {
            sb.append("Creatures ").append(player).append(" controls attack ").append(defender).append(" during his or her next turn.");
        }
        
        //begin standard post-
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            sb.append(abSub.getStackDescription());
        }

        return sb.toString();
    }
    
    private static boolean mustAttackCanPlayAI(final AbilityFactory af, SpellAbility sa) {
    	//disabled for the AI for now.  Only for Gideon Jura at this time.
        return false;
    }
    
    private static boolean mustAttackPlayDrawbackAI(final AbilityFactory af, SpellAbility sa) {
        // AI should only activate this during Human's turn
        boolean chance;
        
        //TODO - implement AI
        chance = false;

        Ability_Sub subAb = sa.getSubAbility();
        if (subAb != null)
            chance &= subAb.chkAI_Drawback();

        return chance;
    }
    
    private static boolean mustAttackDoTriggerAI(AbilityFactory af, SpellAbility sa, boolean mandatory) {
        if (!ComputerUtil.canPayCost(sa) && !mandatory)    // If there is a cost payment it's usually not mandatory
            return false;

        boolean chance;
        
        //TODO - implement AI
        chance = false;

        // check SubAbilities DoTrigger?
        Ability_Sub abSub = sa.getSubAbility();
        if (abSub != null) {
            return chance && abSub.doTrigger(mandatory);
        }

        return chance;
    }
    
    private static void mustAttackResolve(final AbilityFactory af, final SpellAbility sa) {
    	HashMap<String, String> params = af.getMapParams();

        ArrayList<Player> tgtPlayers;

        Target tgt = af.getAbTgt();
        if (tgt != null && !params.containsKey("Defined"))
            tgtPlayers = tgt.getTargetPlayers();
        else
            tgtPlayers = AbilityFactory.getDefinedPlayers(sa.getSourceCard(), params.get("Defined"), sa);

        for (final Player p : tgtPlayers) {
            if (tgt == null || p.canTarget(sa)) {
            	Object entity;
            	if(params.get("Defender").equals("Self")) {
                	entity = af.getHostCard();
                }
                else {
                	entity = p.getOpponent();
                }
            	//System.out.println("Setting mustAttackEntity to: "+entity);
                p.setMustAttackEntity(entity);
            }
        }
    	
    }//mustAttackResolve()
    
}//end class AbilityFactory_Combat
