package forge.interfaces;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Function;

import forge.LobbyPlayer;
import forge.assets.FSkinProp;
import forge.assets.ISkinImage;
import forge.deck.CardPool;
import forge.events.UiEvent;
import forge.game.GameType;
import forge.game.Match;
import forge.game.phase.PhaseType;
import forge.game.player.IHasIcon;
import forge.game.player.RegisteredPlayer;
import forge.game.zone.ZoneType;
import forge.item.PaperCard;
import forge.match.input.InputQueue;
import forge.sound.IAudioClip;
import forge.sound.IAudioMusic;
import forge.util.ITriggerEvent;
import forge.view.CardView;
import forge.view.CombatView;
import forge.view.GameEntityView;
import forge.view.PlayerView;
import forge.view.SpellAbilityView;

public interface IGuiBase {
    boolean isRunningOnDesktop();
    String getCurrentVersion();
    void invokeInEdtLater(Runnable runnable);
    void invokeInEdtAndWait(final Runnable proc);
    boolean isGuiThread();
    String getAssetsDir();
    ISkinImage getSkinIcon(FSkinProp skinProp);
    ISkinImage getUnskinnedIcon(String path);
    void showBugReportDialog(String title, String text, boolean showExitAppBtn);
    int showOptionDialog(String message, String title, FSkinProp icon, String[] options, int defaultOption);
    int showCardOptionDialog(CardView card, String message, String title, FSkinProp icon, String[] options, int defaultOption);
    <T> T showInputDialog(String message, String title, FSkinProp icon, T initialInput, T[] inputOptions);
    <T> List<T> getChoices(final String message, final int min, final int max, final Collection<T> choices, final T selected, final Function<T, String> display);
    <T> List<T> order(final String title, final String top, final int remainingObjectsMin, final int remainingObjectsMax,
            final List<T> sourceChoices, final List<T> destChoices, final CardView referenceCard, final boolean sideboardingMode);
    List<PaperCard> sideboard(CardPool sideboard, CardPool main);
    String showFileDialog(String title, String defaultDir);
    File getSaveFile(File defaultFile);
    void showCardList(final String title, final String message, final List<PaperCard> list);
    boolean showBoxedProduct(final String title, final String message, final List<PaperCard> list);
    void fireEvent(UiEvent e);
    void setCard(CardView card);
    void showCombat(CombatView combat);
    void setUsedToPay(CardView card, boolean b);
    void setHighlighted(PlayerView player, boolean b);
    void showPromptMessage(String message);
    boolean stopAtPhase(PlayerView playerTurn, PhaseType phase);
    InputQueue getInputQueue();
    IButton getBtnOK();
    IButton getBtnCancel();
    void focusButton(IButton button);
    void flashIncorrectAction();
    void updatePhase();
    void updateTurn(PlayerView player);
    void updatePlayerControl();
    void enableOverlay();
    void disableOverlay();
    void finishGame();
    Object showManaPool(PlayerView player);
    void hideManaPool(PlayerView player, Object zoneToRestore);
    boolean openZones(Collection<ZoneType> zones, Map<PlayerView, Object> players);
    void restoreOldZones(Map<PlayerView, Object> playersToRestoreZonesFor);
    void updateStack();
    void updateZones(List<Pair<PlayerView, ZoneType>> zonesToUpdate);
    void updateCards(Set<CardView> cardsToUpdate);
    void refreshCardDetails(Iterable<CardView> cards);
    void updateManaPool(List<PlayerView> manaPoolUpdate);
    void updateLives(List<PlayerView> livesUpdate);
    void endCurrentGame();
    void startMatch(GameType gauntletType, List<RegisteredPlayer> starter);
    void setPanelSelection(CardView hostCard);
    Map<CardView, Integer> getDamageToAssign(CardView attacker, List<CardView> blockers,
            int damageDealt, GameEntityView defender, boolean overrideOrder);
    SpellAbilityView getAbilityToPlay(List<SpellAbilityView> abilities, ITriggerEvent triggerEvent);
    void hear(LobbyPlayer player, String message);
    int getAvatarCount();
    void copyToClipboard(String text);
    void browseToUrl(String url) throws Exception;
	LobbyPlayer getGuiPlayer();
    LobbyPlayer getAiPlayer(String name);
	LobbyPlayer createAiPlayer();
	LobbyPlayer createAiPlayer(String name, int avatarIndex);
	LobbyPlayer getQuestPlayer();
    IAudioClip createAudioClip(String filename);
    IAudioMusic createAudioMusic(String filename);
    void startAltSoundSystem(String filename, boolean isSynchronized);
    void clearImageCache();
    void startGame(Match match);
    void continueMatch(Match match);
    void showSpellShop();
    void showBazaar();
    void setPlayerAvatar(LobbyPlayer player, IHasIcon ihi);
}