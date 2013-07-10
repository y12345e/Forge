package forge.game.event;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public interface IGameEventVisitor<T> {
    T visit(GameEventAnteCardsSelected event);
    T visit(GameEventAttackersDeclared event);
    T visit(GameEventBlockersDeclared event);
    T visit(GameEventCardDamaged event);
    T visit(GameEventCardDestroyed event);
    T visit(GameEventCardAttachment event);
    T visit(GameEventCardChangeZone event);
    T visit(GameEventCardRegenerated event);
    T visit(GameEventCardSacrificed event);
    T visit(GameEventCardTapped event);
    T visit(GameEventCardStatsChanged event);
    T visit(GameEventCardCounters event);
    T visit(GameEventCombatEnded event);
    T visit(GameEventGameFinished event);
    T visit(GameEventGameOutcome event);
    T visit(GameEventFlipCoin event);
    T visit(GameEventGameStarted event);
    T visit(GameEventGameRestarted event);
    T visit(GameEventLandPlayed event);
    T visit(GameEventPlayerLivesChanged event);
    T visit(GameEventManaPool event);
    T visit(GameEventManaBurn event);
    T visit(GameEventMulligan event);
    T visit(GameEventPlayerControl event);
    T visit(GameEventPlayerDamaged gameEventPlayerDamaged);
    T visit(GameEventPlayerPoisoned event);
    T visit(GameEventPlayerPriority event);
    T visit(GameEventShuffle event);
    T visit(GameEventSpellAbilityCast gameEventSpellAbilityCast);
    T visit(GameEventSpellResolved event);
    T visit(GameEventSpellRemovedFromStack event);
    T visit(GameEventTokenCreated event);
    T visit(GameEventTurnBegan gameEventTurnBegan);
    T visit(GameEventTurnEnded event);
    T visit(GameEventTurnPhase event);
    T visit(GameEventZone event);


    // This is base class for all visitors.
    public static class Base<T> implements IGameEventVisitor<T>{
        public T visit(GameEventAnteCardsSelected event) { return null; }
        public T visit(GameEventAttackersDeclared event) { return null; }
        public T visit(GameEventBlockersDeclared event) { return null; }
        public T visit(GameEventCardDamaged event) { return null; }
        public T visit(GameEventCardDestroyed event) { return null; }
        public T visit(GameEventCardAttachment event) { return null; }
        public T visit(GameEventCardChangeZone event) { return null; }
        public T visit(GameEventCardRegenerated event) { return null; }
        public T visit(GameEventCardSacrificed event) { return null; }
        public T visit(GameEventCardTapped event) { return null; }
        public T visit(GameEventCardStatsChanged event) { return null; }
        public T visit(GameEventCardCounters event) { return null; }
        public T visit(GameEventGameFinished event) { return null; }
        public T visit(GameEventGameOutcome event) { return null; }
        public T visit(GameEventFlipCoin event) { return null; }
        public T visit(GameEventGameStarted event) { return null; }
        public T visit(GameEventGameRestarted event) { return null; }
        public T visit(GameEventLandPlayed event) { return null; }
        public T visit(GameEventPlayerLivesChanged event) { return null; }
        public T visit(GameEventManaPool event) { return null; }
        public T visit(GameEventManaBurn event) { return null; }
        public T visit(GameEventMulligan event) { return null; }
        public T visit(GameEventPlayerControl event) { return null; }
        public T visit(GameEventPlayerPoisoned event) { return null; }
        public T visit(GameEventPlayerPriority event) { return null; }
        public T visit(GameEventShuffle event) { return null; }
        public T visit(GameEventSpellResolved event) { return null; }
        public T visit(GameEventSpellAbilityCast event) { return null; }
        public T visit(GameEventSpellRemovedFromStack event) { return null; }
        public T visit(GameEventTokenCreated event) { return null; }
        public T visit(GameEventTurnBegan event) { return null; }
        public T visit(GameEventTurnEnded event) { return null; }
        public T visit(GameEventTurnPhase event) { return null; }
        public T visit(GameEventPlayerDamaged event) { return null; }
        public T visit(GameEventZone event) { return null; }
        public T visit(GameEventCombatEnded event) { return null; }
    }





}

