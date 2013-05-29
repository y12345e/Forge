package forge.control;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.eventbus.Subscribe;

import forge.Card;
import forge.FThreads;
import forge.game.event.GameEvent;
import forge.game.event.GameEventAnteCardsSelected;
import forge.game.event.GameEventDuelFinished;
import forge.game.event.GameEventDuelOutcome;
import forge.game.event.GameEventPlayerControl;
import forge.game.event.GameEventTurnPhase;
import forge.game.event.IGameEventVisitor;
import forge.game.phase.PhaseUtil;
import forge.game.player.Player;
import forge.gui.GuiDialog;
import forge.gui.SOverlayUtils;
import forge.gui.match.CMatchUI;
import forge.gui.match.VMatchUI;
import forge.gui.match.ViewWinLose;
import forge.gui.match.nonsingleton.VHand;

public class FControlGameEventHandler extends IGameEventVisitor.Base<Void, Void> {
    public final FControl fc;
    public FControlGameEventHandler(FControl fc ) {
        this.fc = fc;
    }
    
    @Subscribe
    public void receiveGameEvent(final GameEvent ev) { ev.visit(this, null); }
    
    @Override
    public Void visit(final GameEventTurnPhase ev, Void params) {
        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            PhaseUtil.visuallyActivatePhase(ev.playerTurn, ev.phase);
        } });
        return null;
    }
    
    @Override
    public Void visit(GameEventAnteCardsSelected ev, Void params) {
        // Require EDT here?
        final String nl = System.getProperty("line.separator");
        final StringBuilder msg = new StringBuilder();
        for (final Pair<Player, Card> kv : ((GameEventAnteCardsSelected) ev).cards) {
            msg.append(kv.getKey().getName()).append(" ante: ").append(kv.getValue()).append(nl);
        }
        GuiDialog.message(msg.toString(), "Ante");
        return null;
    }
    
    @Override
    public Void visit(GameEventPlayerControl ev, Void params) {
        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            CMatchUI.SINGLETON_INSTANCE.initHandViews(fc.getLobby().getGuiPlayer());
            VMatchUI.SINGLETON_INSTANCE.populate();
            for(VHand h : VMatchUI.SINGLETON_INSTANCE.getHands()) {
                h.getLayoutControl().updateHand();
            }
        } });
        return null;
    }
    
    @Override
    public Void visit(GameEventDuelOutcome ev, Void params) {
        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            fc.getInputQueue().onGameOver(); // this will unlock any game threads waiting for inputs to complete
        } });
        return null;
    }    
    
    @Override
    public Void visit(GameEventDuelFinished ev, Void params) {
        FThreads.invokeInEdtNowOrLater(new Runnable() { @Override public void run() {
            new ViewWinLose(fc.getObservedGame().getMatch());
            SOverlayUtils.showOverlay();
        } });
        return null;
    }
    
    
}