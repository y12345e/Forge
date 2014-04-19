package forge.game.card;

import forge.game.cost.Cost;
import forge.game.replacement.ReplacementLayer;
import forge.game.zone.ZoneType;

import java.util.*;

/**
 * Created by Hellfish on 2014-02-09.
 */
public enum KeywordType {
    Absorb("If a source would deal damage to this creature, prevent {magnitude} of that damage.", KwParamType.Magnitude),
    Affinity("This spell costs {1} less to cast for each artifact you control."),
    Amplify("As this creature enters the battlefield, put {magnitude} +1/+1 counter(s) on it for each Beast card you reveal in your hand.", KwParamType.Magnitude),
    Annihilator("Whenever this creature attacks, defending player sacrifices {magnitude} permanent(s).", KwParamType.Magnitude),
    Aura_swap("{cost}: Exchange this Aura with an Aura card in your hand.", KwParamType.Cost),
    Banding("Any creatures with banding, and up to one without, can attack in a band. Bands are blocked as a group. If any creatures with banding you control are blocking or being blocked by a creature, you divide that creature's combat damage, not its controller, among any of the creatures it's being blocked by or is blocking."),
    Bands_with_Other_Creatures_named_Wolves_of_the_Hunt("Any creatures with banding, and up to one without, can attack in a band. Bands are blocked as a group. If any creatures with banding you control are blocking or being blocked by a creature, you divide that creature's combat damage, not its controller, among any of the creatures it's being blocked by or is blocking."),
    Bands_with_Other_Legendary_Creatures("Any creatures with banding, and up to one without, can attack in a band. Bands are blocked as a group. If any creatures with banding you control are blocking or being blocked by a creature, you divide that creature's combat damage, not its controller, among any of the creatures it's being blocked by or is blocking."),
    Bands_with_Other_Dinosaurs("Any creatures with banding, and up to one without, can attack in a band. Bands are blocked as a group. If any creatures with banding you control are blocking or being blocked by a creature, you divide that creature's combat damage, not its controller, among any of the creatures it's being blocked by or is blocking."),
    Battle_Cry("Whenever this creature attacks, each other attacking creature gets +1/+0 until end of turn."),
    Bestow("If you cast this card for its bestow cost, it's an Aura spell with enchant creature. It becomes a creature again if it's not attached to a creature.", KwParamType.Cost),
    Bloodthirst("If an opponent was dealt damage this turn, this creature enters the battlefield with {magnitude} +1/+1 counter(s) on it.", KwParamType.Magnitude),
    Bushido("When this blocks or becomes blocked, it gets +{magnitude}/+{magnitude} until end of turn.", KwParamType.Magnitude),
    Buyback("You may pay an additional {cost} as you cast this spell. If you do, put this card into your hand as it resolves.", KwParamType.Cost),
    Cascade("When you cast this spell, exile cards from the top of your library until you exile a nonland card that costs less. You may cast it without paying its mana cost. Put the exiled cards on the bottom in a random order."),
    Champion("When this enters the battlefield, sacrifice it unless you exile another {type} you control. When this leaves the battlefield, that card returns to the battlefield.", KwParamType.Type, KwParamType.OverridingDescription),
    Changeling("This card is every creature type at all times."),
    Cipher("Then you may exile this spell card encoded on a creature you control. Whenever that creature deals combat damage to a player, its controller may cast a copy of this card without paying its mana cost."),
    Conspire("As you cast this spell, you may tap two untapped creatures you control that share a color with it. When you do, copy it and you may choose a new target for the copy."),
    Convoke("Each creature you tap while casting this spell reduces its cost by {1} or by one mana of that creature's color."),
    Cumulative_Upkeep("At the beginning of your upkeep, put an age counter on this permanent, then sacrifice it unless you pay its upkeep cost for each age counter on it.", KwParamType.Cost),
    Cycling("{cost}: Draw a card.", KwParamType.Cost),
    Deathtouch("Any amount of damage this deals to a creature is enough to destroy it."),
    Defender("This creature can't attack."),
    Delve("You may exile any number of cards from your graveyard as you cast this spell. It costs {1} less to cast for each card exiled this way."),
    Devour("As this enters the battlefield, you may sacrifice any number of creatures. This creature enters the battlefield with {magnitude} +1/+1 counter(s) on it for each of those creatures.", KwParamType.Magnitude, KwParamType.OverridingDescription),
    Double_strike("This creature deals both first-strike and regular combat damage."),
    Dredge("If you would draw a card, instead you may put exactly {magnitude} cards from the top of your library into your graveyard. If you do, return this card from your graveyard to your hand. Otherwise, draw a card.", KwParamType.Magnitude),
    Echo("At the beginning of your upkeep, if this came under your control since the beginning of your last upkeep, sacrifice it unless you pay its echo cost.", KwParamType.Type, KwParamType.OverridingDescription),
    Enchant("Target a {type} as you cast this. This card enters the battlefield attached to that {type}.", KwParamType.Type, KwParamType.OverridingDescription),
    Enchant_creature("Target a creature as you cast this. This card enters the battlefield attached to that creature.", KwParamType.OverridingDescription),
    Entwine("Choose both if you pay the entwine cost.", KwParamType.Cost),
    Epic("For the rest of the game, you can't cast spells. At the beginning of each of your upkeeps, copy this spell except for its epic ability."),
    Equip("{cost}: Attach to target creature you control. Equip only as a sorcery.", KwParamType.Cost, KwParamType.String, KwParamType.OverridingDescription),
    Evoke("You may cast this spell for its evoke cost. If you do, it's sacrificed when it enters the battlefield.", KwParamType.Cost),
    Evolve("Whenever a creature enters the battlefield under your control, if that creature has greater power or toughness than this creature, put a +1/+1 counter on this creature."),
    Exalted("Whenever a creature you control attacks alone, that creature gets +1/+1 until end of turn."),
    Extort("Whenever you cast a spell, you may pay {W/B}. If you do, each opponent loses 1 life and you gain that much life."),
    Fading("This creature enters the battlefield with {magnitude} fade counters on it. At the beginning of your upkeep, remove a fade counter from it. If you can't, sacrifice it.", KwParamType.Magnitude),
    Fear("This creature can?t be blocked except by artifact creatures and/or black creatures."),
    First_strike("This creature deals combat damage before creatures without first strike."),
    Flanking("Whenever a creature without flanking blocks this creature, the blocking creature gets -1/-1 until end of turn."),
    Flash("You may cast this spell any time you could cast an instant."),
    Flashback("You may cast this card from your graveyard for its flashback cost. Then exile it.", KwParamType.Cost),
    Flying("This creature can't be blocked except by creatures with flying or reach."),
    Forecast("Play this ability only during your upkeep and only once each turn.", KwParamType.Cost),
    Fortify("{cost}: Attach to target land you control. Fortify only as a sorcery. This card enters the battlefield unattached and stays on the battlefield if the land leaves.", KwParamType.Cost),
    Frenzy("Whenever this creature attacks and isn't blocked, it gets +{magnitude}/+0 until end of turn.", KwParamType.Magnitude),
    Fuse("You may cast one or both halves of this card from your hand."),
    Graft("This creature comes into play with {magnitude} +1/+1 counter(s) on it. Whenever another creature comes into play, you may move a +1/+1 counter from this creature onto it.", KwParamType.Magnitude),
    Gravestorm("When you cast this spell, copy it for each permanent put into a graveyard this turn. You may choose new targets for the copies."),
    Haste("This creature can attack and Tap as soon as it comes under your control."),
    Haunt("When this creature dies, exile it haunting target creature.", KwParamType.String, KwParamType.OverridingDescription),
    Hexproof("This creature can't be the target of spells or abilities your opponents control."),
    Hideaway("This land enters the battlefield tapped. When it does, look at the top four cards of your library, exile one face down, then put the rest on the bottom of your library."),
    Horsemanship("This creature can't be blocked except by creatures with horsemanship."),
    Indestructible("Effects that say \"destroy\" don't destroy this."),
    Infect("This creature deals damage to creatures in the form of -1/-1 counters and to players in the form of poison counters."),
    Intimidate("This creature can't be blocked except by artifact creatures and/or creatures that share a color with it."),
    Kicker("You may pay an additional {cost] as you cast this spell.", KwParamType.Cost),
    Landwalk("This creature is unblockable as long as defending player controls a {type}.", KwParamType.Type, KwParamType.OverridingDescription),
    Level_up("{cost}: Put a level counter on this. Level up only as a sorcery.", KwParamType.Cost),
    Lifelink( "Damage dealt by this creature also causes you to gain that much life."),
    Living_weapon("When this Equipment enters the battlefield, put a 0/0 black Germ creature token onto the battlefield, then attach this to it."),
    Madness("If you discard this card, you may cast it for its madness cost instead of putting it into your graveyard.", KwParamType.Cost),
    Miracle("You may cast this card for its Miracle cost when you draw it if it's the first card you drew this turn.", KwParamType.Cost),
    Modular("This comes into play with {magnitude} +1/+1 counters on it. When it's put into a graveyard, you may put its +1/+1 counters on target artifact creature.", KwParamType.Magnitude),
    Monstrosity("If this creature isn't monstrous, put {magnitude} +1/+1 counters on it and it becomes monstrous.", KwParamType.Magnitude, KwParamType.Cost),
    Morph("You may cast this face down as a 2/2 creature for {3}. Turn it face up any time for its morph cost.", KwParamType.Cost),
    Multikicker("You may pay an additional {cost} any number of times as you cast this spell.", KwParamType.Cost),
    Ninjutsu("{cost}, Return an unblocked attacker you control to hand: Put this card onto the battlefield from your hand tapped and attacking.", KwParamType.Cost),
    Offering("You may cast this card any time you could cast an instant by sacrificing a {type} and paying the difference in mana costs between this and the sacrificed {type}. Mana cost includes color.", KwParamType.Type),
    Overload("You may cast this spell for its overload cost. If you do, change its text by replacing all instances of \"target\" with \"each\".", KwParamType.Cost),
    Persist("When this creature dies, if it had no -1/-1 counters on it, return it to the battlefield under its owner's control with a -1/-1 counter on it."),
    Phasing("This phases in or out before you untap during each of your untap steps. While it's phased out, it's treated as though it doesn't exist."),
    Poisonous("Whenever this creature deals combat damage to a player, that player gets a poison counter. A player with ten or more poison counters loses the game.", KwParamType.Magnitude),
    Protection("This creature can't be blocked, targeted, dealt damage, or enchanted by any {type} card.", KwParamType.Type, KwParamType.OverridingDescription),
    Protection_from_white("This creature can't be blocked, targeted, dealt damage, or enchanted by any white card."),
    Protection_from_blue("This creature can't be blocked, targeted, dealt damage, or enchanted by any blue card."),
    Protection_from_black( "This creature can't be blocked, targeted, dealt damage, or enchanted by any black card."),
    Protection_from_red("This creature can't be blocked, targeted, dealt damage, or enchanted by any red card."),
    Protection_from_green("This creature can't be blocked, targeted, dealt damage, or enchanted by any green card."),
    Protection_from_creatures("This creature can't be blocked, targeted, dealt damage, or enchanted by any creature card."),
    Protection_from_artifacts("This creature can't be blocked, targeted, dealt damage, or enchanted by any artifact card."),
    Protection_from_enchantments("This creature can't be blocked, targeted, dealt damage, or enchanted by any enchantment card."),
    Protection_from_everything("This creature can't be blocked, targeted, dealt damage, or enchanted by any card."),
    Protection_from_colored_spells("This creature can't be blocked, targeted, or dealt damage by any colored spell."),
    Protection_from_Dragons("This creature can't be blocked, targeted, dealt damage, or enchanted by any Dragon card."),
    Protection_from_Demons("This creature can't be blocked, targeted, dealt damage, or enchanted by any Demon card."),
    Protection_from_Goblins("This creature can't be blocked, targeted, dealt damage, or enchanted by any Goblin card."),
    Protection_from_Clerics("This creature can't be blocked, targeted, dealt damage, or enchanted by any Cleric card."),
    Protection_from_Gorgons("This creature can't be blocked, targeted, dealt damage, or enchanted by any Gorgon card."),
    Protection_from_the_chosen_player("This creature can't be targeted, or dealt damage by the chosen player."),
    Provoke("When this attacks, you may have target creature defending player controls untap and block it if able."),
    Prowl("You may cast this for its prowl cost if you dealt combat damage to a player this turn with a Rogue.", KwParamType.Cost),
    Rampage("Whenever this creature becomes blocked, it gets +{magnitude}/+{magnitude} until end of turn for each creature blocking it beyond the first.", KwParamType.Magnitude),
    Reach("This creature can block creatures with flying."),
    Rebound("If you cast this spell from your hand, exile it as it resolves. At the beginning of your next upkeep, you may cast this card from exile without paying its mana cost."),
    Recover("When a creature is put into your graveyard from the battlefield, you may pay {cost}. If you do, return this card from your graveyard to your hand. Otherwise, exile this card.", KwParamType.Cost),
    Reinforce("{cost}, Discard this card: Put a +1/+1 counter on target creature.", KwParamType.Cost),
    Replicate("When you cast this spell, copy it for each time you paid its replicate cost. You may choose new targets for the copies.", KwParamType.Cost),
    Retrace("You may cast this card from your graveyard by discarding a land card in addition to paying its other costs"),
    Ripple("When you cast this spell, you may reveal the top {magnitude} cards of your library. You may cast any revealed cards with the same name as this spell without paying their mana costs. Put the rest on the bottom of your library.", KwParamType.Magnitude),
    Scavenge("{cost}, Exile this card from your graveyard: Put a number of +1/+1 counters equal to this card's power on target creature. Scavenge only as a sorcery.", KwParamType.Cost),
    Shadow("This creature can block or be blocked by only creatures with shadow"),
    Shroud("This creature can't be the target of spells or abilities."),
    Soulbond("You may pair this creature with another unpaired creature when either enters the battlefield. They remain paired for as long as you control both of them."),
    Soulshift("When this creature dies, you may return target Spirit card with converted mana cost {magnitude} or less from your graveyard to your hand.", KwParamType.Magnitude),
    Splice_onto_Arcane("As you cast an Arcane spell, you may reveal this card from your hand and pay its splice cost. If you do, add this card's effects to that spell.", KwParamType.Cost),
    Split_second("As long as this spell is on the stack, players can't cast spells or activate abilities that aren't mana abilities."),
    Storm("When you cast this spell, copy it for each spell cast before it this turn. You may choose new targets for the copies."),
    Sunburst("This enters the battlefield with a charge or +1/+1 counter on it for each color of mana spent to cast it."),
    Suspend("Rather than cast this card from your hand, pay {cost} and remove it from the game with {magnitude} time counters on it. At the beginning of your upkeep, remove a time counter. When you remove the last, cast it without paying its mana cost.", KwParamType.Magnitude, KwParamType.Cost),
    Totem_armor("If enchanted creature would be destroyed, instead remove all damage from it and destroy this Aura."),
    Trample("If this creature would assign enough damage to its blockers to destroy them, you may have it assign the rest of its damage to defending player or planeswalker."),
    Transfigure("{cost}, Sacrifice this creature: Search your library for a creature card with the same converted mana cost as this creature and put that card onto the battlefield. Then shuffle your library. Transfigure only as a sorcery.", KwParamType.Cost),
    Transmute("{cost}, Discard this card: Search your library for a card with the same converted mana cost as this card, reveal it, and put it into your hand. Then shuffle your library. Transmute only as a sorcery.", KwParamType.Cost),
    Tribute("(As this creature enters the battlefield, an opponent of your choice may put {magnitude} +1/+1 counters on it.", KwParamType.Magnitude),
    TypeCycling("{cost}: Search your library for a {type} card, reveal it, and put it into your hand. Then shuffle your library.", KwParamType.Cost, KwParamType.Type, KwParamType.OverridingDescription),
    Undying("When this creature dies, if it had no +1/+1 counters on it, return it to the battlefield under its owner's control with a +1/+1 counter on it."),
    Unearth("{cost}: Return this card from your graveyard to the battlefield. It gains haste. Exile it at the beginning of the next end step or if it would leave the battlefield. Unearth only as a sorcery.", KwParamType.Cost),
    Unleash("You may have this creature enter the battlefield with a +1/+1 counter on it. It can't block as long as it has a +1/+1 counter on it."),
    Vanishing("This permanent enters the battlefield with {magnitude} time counter(s) on it. At the beginning of your upkeep, remove a time counter from it. When the last is removed, sacrifice it.", KwParamType.Magnitude),
    Vigilance("Attacking doesn't cause this creature to tap."),
    Wither("This deals damage to creatures in the form of -1/-1 counters."),

    AdjustLandPlays(null,KwParamType.Magnitude),
    All_creatures_able_to_block_CARDNAME_do_so(null),
    All_creatures_with_flying_able_to_block_CARDNAME_do_so(null),
    All_damage_is_dealt_to_you_as_though_its_source_had_infect(null),
    All_Walls_able_to_block_CARDNAME_do_so(null),
    Alternative_Cost(null,KwParamType.Cost),
    AlternateAdditionalCost(null,KwParamType.Cost,KwParamType.Cost),
    As_an_additional_cost_to_cast_creature_spells_you_may_pay_any_amount_of_mana_if_you_do_that_creature_enters_the_battlefield_with_that_many_additional_P1P1_counters_on_it(null),
    At_the_beginning_of_the_end_step_destroy_CARDNAME_if_it_attacked_this_turn(null),
    At_the_beginning_of_the_end_step_destroy_CARDNAME(null),
    At_the_beginning_of_the_end_step_exile_CARDNAME(null),
    At_the_beginning_of_the_end_step_sacrifice_CARDNAME(null),
    At_the_beginning_of_this_turns_end_step_you_lose_the_game(null),
    At_the_beginning_of_your_upkeep_sacrifice_CARDNAME_unless_you_pay(null, KwParamType.Cost),
    Attacking_doesnt_cause_CARDNAME_to_tap(null),
    CantBlock(null, KwParamType.Type),
    CantBlockCardUID(null, KwParamType.String),
    CantBeBlockedBy(null, KwParamType.Type),
    CantBeBlockedByAmount(null, KwParamType.String),
    CantEquip(null, KwParamType.Type, KwParamType.OverridingDescription),
    CantSearchLibrary(null, KwParamType.OverridingDescription),
    CARDNAME_assigns_no_combat_damage(null),
    CARDNAME_attacks_each_turn_if_able(null),
    CARDNAME_attacks_specific_player_each_combat_if_able(null, KwParamType.String),
    CARDNAME_blocks_each_turn_if_able(null),
    CARDNAME_can_attack_as_though_it_didnt_have_defender(null),
    CARDNAME_can_attack_as_though_it_had_haste(null),
    CARDNAME_can_block_an_additional_creature(null),
    CARDNAME_can_block_an_additional_ninetyMnine_creatures(null), //TODO:Fix that M
    CARDNAME_can_block_any_number_of_creatures(null),
    CARDNAME_can_block_as_though_it_were_untapped(null),
    CARDNAME_can_block_creatures_with_shadow_as_though_they_didnt_have_shadow(null),
    CARDNAME_can_block_only_creatures_with_flying(null),
    CARDNAME_can_only_attack_alone(null),
    CARDNAME_cant_attack(null),
    CARDNAME_cant_attack_during_extra_turns(null),
    CARDNAME_cant_attack_if_defending_player_controls_an_untapped_creature_with_power(null, KwParamType.String),
    CARDNAME_cant_attack_or_block(null),
    CARDNAME_cant_attack_or_block_alone(null),
    CARDNAME_cant_attack_unless_defending_player_controls_a(null, KwParamType.Type, KwParamType.OverridingDescription),
    CARDNAME_cant_block(null),
    CARDNAME_cant_be_blocked_unless_all_creatures_defending_player_controls_block_it(null),
    CARDNAME_cant_be_countered(null),
    CARDNAME_cant_be_countered_by_blue_or_black_spells(null),
    CARDNAME_cant_be_enchanted(null),
    CARDNAME_cant_be_equipped(null),
    CARDNAME_cant_be_regenerated(null),
    CARDNAME_cant_be_the_target_of_Aura_spells(null),
    CARDNAME_cant_be_the_target_of_black_spells(null),
    CARDNAME_cant_be_the_target_of_blue_spells(null),
    CARDNAME_cant_be_the_target_of_red_spells_or_abilities_from_red_sources(null),
    CARDNAME_cant_be_the_target_of_spells(null),
    CARDNAME_cant_have_counters_put_on_it(null),
    CARDNAME_cant_have_more_than_seven_dream_counters_on_it(null),
    CARDNAME_cant_have_or_gain(null, KwParamType.Type),
    CARDNAME_cant_phase_out(null),
    CARDNAME_cant_transform(null),
    CARDNAME_doesnt_untap_during_your_untap_step(null),
    CARDNAME_doesnt_untap_during_your_next_untap_step(null),
    CARDNAME_doesnt_untap_during_your_next_two_untap_steps(null),
    CARDNAME_enters_the_battlefield_tapped(null),
    CARDNAME_may_activate_abilities_as_though_it_has_haste(null),
    CARDNAME_must_be_blocked_if_able(null),
    CARDNAME_untaps_during_each_other_players_untap_step(null),
    CARDNAMEs_activated_abilities_cant_be_activated(null),
    CARDNAMEs_power_and_toughness_are_switched(null),
    Creatures_with_power_less_than_CARDNAMEs_power_cant_block_it(null),
    Creatures_you_control_cant_have_M1M1_counters_placed_on_them(null),
    Damage_that_would_be_dealt_by_CARDNAME_cant_be_prevented(null),
    Damage_that_would_reduce_your_life_total_to_less_than_1_reduces_it_to_1_instead(null),
    During_your_next_untap_step_as_you_untap_your_permanents_return_CARDNAME_to_its_owners_hand(null),
    Each_instant_and_sorcery_spell_you_cast_has_replicate_The_replicate_cost_is_equal_to_its_mana_cost(null),
    Echo_unpaid(null),
    etbCounter(null, KwParamType.CounterType,KwParamType.Magnitude,KwParamType.String,KwParamType.OverridingDescription),
    etbReplacement(null, KwParamType.ReplacementLayer, KwParamType.String, KwParamType.Boolean, KwParamType.String, KwParamType.Type),
    Green_mana_doesnt_empty_from_your_mana_pool_as_steps_and_phases_end(null),
    If_a_spell_or_ability_an_opponent_controls_causes_you_to_discard_CARDNAME_put_it_onto_the_battlefield_instead_of_putting_it_into_your_graveyard(null),
    If_CARDNAME_would_be_destroyed_regenerate_it(null),
    If_CARDNAME_would_be_put_into_a_graveyard_exile_it_instead(null),
    If_CARDNAME_would_leave_the_battlefield_exile_it_instead_of_putting_it_anywhere_else(null),
    If_damage_would_be_dealt_to_CARDNAME_prevent_that_damage_Remove_a_P1P1_counter_from_CARDNAME(null),
    If_you_would_begin_an_extra_turn_skip_that_turn_instead(null),
    If_you_would_flip_a_coin_instead_flip_two_coins_and_ignore_one(null),
    Legend_rule_doesnt_apply_to_CARDNAME(null),
    LimitSearchLibrary(null),
    May_be_blocked_as_though_it_doesnt_have_forestwalk(null),
    May_be_blocked_as_though_it_doesnt_have_islandwalk(null),
    May_be_blocked_as_though_it_doesnt_have_landwalk(null),
    May_be_blocked_as_though_it_doesnt_have_mountainwalk(null),
    May_be_blocked_as_though_it_doesnt_have_plainswalk(null),
    May_be_blocked_as_though_it_doesnt_have_swampwalk(null),
    May_be_played(null),
    May_be_played_by_your_opponent(null),
    May_be_played_by_your_opponent_without_paying_its_mana_cost(null),
    May_be_played_without_paying_its_mana_cost(null),
    May_be_played_without_paying_its_mana_cost_and_as_though_it_has_flash(null),
    MayEffectFromOpeningHand(null, KwParamType.Type,KwParamType.String),
    No_more_than_two_creatures_can_attack_each_combat(null),
    No_more_than_two_creatures_can_attack_you_each_combat(null),
    OnlyUntapChosen(null,KwParamType.String),
    Permanents_dont_untap_during_their_controllers_untap_steps(null, KwParamType.Type),
    Play_with_your_hand_revealed(null),
    PreventAllDamageBy(null, KwParamType.Type, KwParamType.OverridingDescription),
    Prevent_all_combat_damage_that_would_be_dealt_by_CARDNAME(null),
    Prevent_all_combat_damage_that_would_be_dealt_to_and_dealt_by_CARDNAME(null),
    Prevent_all_combat_damage_that_would_be_dealt_to_CARDNAME(null),
    Prevent_all_damage_that_would_be_dealt_by_CARDNAME(null),
    Prevent_all_damage_that_would_be_dealt_to_and_dealt_by_CARDNAME(null),
    Prevent_all_damage_that_would_be_dealt_to_CARDNAME(null),
    Reveal_the_first_card_you_draw_each_turn(null),
    Schemes_cant_be_set_in_motion_this_turn(null),
    Skip_all_combat_phases_of_this_turn(null),
    Skip_all_combat_phases_of_your_next_turn(null),
    Skip_the_untap_step_of_this_turn(null),
    Skip_your_combat_phase(null),
    Skip_your_draw_step(null),
    Skip_your_next_combat_phase(null),
    Skip_your_next_draw_step(null),
    Skip_your_next_untap_step(null),
    Skip_your_next_turn(null),
    Skip_your_untap_step(null),
    Skip_your_upkeep_step(null),
    SpellCantTarget(null, KwParamType.Type, KwParamType.OverridingDescription),
    Spells_and_abilities_your_opponents_control_cant_cause_you_to_sacrifice_permanents(null),
    This_card_doesnt_untap_during_your_next_untap_step(null),
    This_card_doesnt_untap_during_your_untap_step(null),
    TokenDoubler(null),
    Unblockable("CARDNAME is unblockable."),
    UntapAdjust(null, KwParamType.Magnitude, KwParamType.String),
    You_assign_combat_damage_of_each_creature_attacking_you(null),
    You_cant_draw_cards(null),
    You_cant_draw_more_than_one_card_each_turn(null),
    You_cant_gain_life(null),
    You_cant_get_poison_counters(null),
    You_cant_pay_life_to_cast_spells_or_activate_abilities(null),
    You_cant_sacrifice_creatures_to_cast_spells_or_activate_abilities(null),
    You_may_cast_CARDNAME_any_time_you_could_cast_an_instant_if_you_pay_2_more_to_cast_it(null),
    You_may_cast_CARDNAME_as_though_it_had_flash_If_you_cast_it_any_time_a_sorcery_couldnt_have_been_cast_the_controller_of_the_permanent_it_becoms_sacrifices_it_at_the_beginning_of_the_next_cleanup_step(null),
    You_may_cast_nonland_cards_as_though_they_had_flash(null),
    You_may_choose_not_to_untap_CARDNAME_during_your_untap_step(null),
    You_may_have_CARDNAME_assign_its_combat_damage_as_though_it_werent_blocked(null),
    You_may_look_at_this_card(null),
    You_cant_lose_the_game(null),
    You_cant_win_the_game(null),
    You_dont_lose_the_game_for_having_0_or_less_life(null),
    You_may_play_any_number_of_additional_lands_on_each_of_your_turns(null),
    Your_life_total_cant_change(null),
    Your_opponent_may_look_at_this_card(null);
    // list all KWs

    List<KwParamType> paramTypes;

    String reminderText;

    private KeywordType(String hint, KwParamType... params) {
        reminderText = hint;
        paramTypes = Arrays.asList(params);
    }

    public KeywordInstance getInstance(Card host,boolean intr,boolean hid,Cost cost,String magnitude,String type,ReplacementLayer repLayer, String rawParams, CounterType ctype, Boolean myBool, ZoneType z, String overridingDescription) {
        KeywordInstance ki = new KeywordInstance(host,intr,hid,this);
        if(cost != null) {
            ki.addCost(cost);
        }
        if(magnitude != null) {
            ki.addMagnitude(magnitude);
        }
        if(type != null) {
            ki.addType(type);
        }
        if(repLayer != null) {
            ki.addReplacementLayer(repLayer);
        }
        if(rawParams != null) {
            ki.addRawString(rawParams);
        }
        if(ctype != null) {
            ki.addCounterType(ctype);
        }
        if(myBool != null) {
            ki.addBoolean(myBool);
        }
        if(z != null) {
            ki.addZone(z);
        }
        if(overridingDescription != null) {
            ki.addOverridingDescription(overridingDescription);
        }

        return ki;
    }

    public KeywordInstance getInstance(Card host,boolean intr,boolean hid) {
        return new KeywordInstance(host,intr,hid,this);
    }

    public static List<KeywordInstance> parseAll(List<String> scripts, Card host, boolean intrinsic) {
        List<KeywordInstance> res = new ArrayList<KeywordInstance>();
        for(String kw : scripts) {
            res.add(KeywordType.parse(kw,host,intrinsic));
        }

        return res;
    }

    public static KeywordInstance parse(String script,Card host,boolean intrinsic) {
        String[] workingCopy = script.split(":");
        boolean h = false;
        if(workingCopy[0].startsWith("HIDDEN ")) {
            h = true;
            workingCopy[0] = workingCopy[0].substring("HIDDEN ".length());
        }

        KeywordType kwType = KeywordType.smartValueOf(KeywordType.getCleanString(workingCopy[0]));

        KeywordInstance ki = new KeywordInstance(host,intrinsic,h,kwType);
        ki.setScript(script);

        int i = 1;
        for(KwParamType param : kwType.paramTypes) {
            switch(param) {
                case Magnitude: ki.addMagnitude(workingCopy[i]); break;
                case Cost: ki.addCost(new Cost(workingCopy[i],false)); break;
                case Type: ki.addType(workingCopy[i]); break;
                case ReplacementLayer: ki.addReplacementLayer(ReplacementLayer.smartValueOf(workingCopy[i])); break;
                case CounterType: ki.addCounterType(CounterType.valueOf(workingCopy[i])); break;
                case ZoneType: ki.addZone(ZoneType.smartValueOf(workingCopy[i])); break;
                case String: break;
                case Boolean: ki.addBoolean(workingCopy[i].equalsIgnoreCase("true")); break;
                case OverridingDescription: ki.addOverridingDescription(workingCopy[i]); break;
            }

            if(i > workingCopy.length)
                break;

            i++;
        }
        return ki;
    }

    /**
     * TODO: Write javadoc for this method.
     * @param value the value to parse
     * @return
     */
    public static KeywordType smartValueOf(String value) {

        final String valToCompate = value.trim();
        for (final KeywordType v : KeywordType.values()) {
            if (v.name().compareToIgnoreCase(valToCompate) == 0) {
                return v;
            }
        }

        throw new RuntimeException("Element " + value + " not found in KeywordType enum");
    }

    public static List<KeywordType> smartValuesOf(List<String> values) {
        List<KeywordType> res = new ArrayList<KeywordType>();
        for(String v : values) {
            res.add(KeywordType.smartValueOf(v));
        }

        return res;
    }

    public static String getCleanString(String s) {
        return s.replaceAll(" ","_").replaceAll("('|\\.)","").replaceAll("\\+","P").replaceAll("-","M");
    }
}