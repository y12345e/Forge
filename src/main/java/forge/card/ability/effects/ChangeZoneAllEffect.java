package forge.card.ability.effects;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Iterables;

import forge.Card;
import forge.CardCharacteristicName;
import forge.CardPredicates;
import forge.card.ability.AbilityUtils;
import forge.card.ability.SpellAbilityEffect;
import forge.card.spellability.SpellAbility;
import forge.game.GameState;
import forge.game.player.Player;
import forge.game.zone.ZoneType;
import forge.gui.GuiChoose;

public class ChangeZoneAllEffect extends SpellAbilityEffect {


    @Override
    protected String getStackDescription(SpellAbility sa) {
        // TODO build Stack Description will need expansion as more cards are added

        final String[] desc = sa.getDescription().split(":");

        if (desc.length > 1) {
            return desc[1];
        } else {
            return desc[0];
        }
    }

    @Override
    public void resolve(SpellAbility sa) {
        final ZoneType destination = ZoneType.smartValueOf(sa.getParam("Destination"));
        final List<ZoneType> origin = ZoneType.listValueOf(sa.getParam("Origin"));

        List<Card> cards = new ArrayList<Card>();

        List<Player> tgtPlayers = getTargetPlayersEmptyAsDefault(sa);
        final GameState game = sa.getActivatingPlayer().getGame();

        if ((tgtPlayers == null) || tgtPlayers.isEmpty() || sa.hasParam("UseAllOriginZones")) {
            cards = game.getCardsIn(origin);
        } else {
            for (final Player p : tgtPlayers) {
                cards.addAll(p.getCardsIn(origin));
            }
        }

        cards = AbilityUtils.filterListByType(cards, sa.getParam("ChangeType"), sa);

        if (sa.hasParam("ForgetOtherRemembered")) {
            sa.getSourceCard().clearRemembered();
        }

        final String remember = sa.getParam("RememberChanged");
        final String forget = sa.getParam("ForgetChanged");
        final String imprint = sa.getParam("Imprint");

        final int libraryPos = sa.hasParam("LibraryPosition") ? Integer.parseInt(sa.getParam("LibraryPosition")) : 0;

        if (sa.getActivatingPlayer().isHuman() && destination.equals(ZoneType.Library) && !sa.hasParam("Shuffle")
                && cards.size() >= 2) {
            cards = GuiChoose.order("Choose order of cards to put into the library", "Put first", 0, cards, null, null);
        }

        for (final Card c : cards) {
            if (destination.equals(ZoneType.Battlefield)) {
                // Auras without Candidates stay in their current location
                if (c.isAura()) {
                    final SpellAbility saAura = AttachEffect.getAttachSpellAbility(c);
                    if (!saAura.getTarget().hasCandidates(saAura, false)) {
                        continue;
                    }
                }
                if (sa.hasParam("Tapped")) {
                    c.setTapped(true);
                }
            }

            if (sa.hasParam("GainControl")) {
                c.setController(sa.getActivatingPlayer(), game.getNextTimestamp());
                game.getAction().moveToPlay(c, sa.getActivatingPlayer());
            } else {
                final Card movedCard = game.getAction().moveTo(destination, c, libraryPos);
                if (sa.hasParam("ExileFaceDown")) {
                    movedCard.setState(CardCharacteristicName.FaceDown);
                }
                if (sa.hasParam("Tapped")) {
                    movedCard.setTapped(true);
                }
            }

            if (remember != null) {
                game.getCardState(sa.getSourceCard()).addRemembered(c);
            }
            if (forget != null) {
                game.getCardState(sa.getSourceCard()).removeRemembered(c);
            }
            if (imprint != null) {
                game.getCardState(sa.getSourceCard()).addImprinted(c);
            }
        }

        // if Shuffle parameter exists, and any amount of cards were owned by
        // that player, then shuffle that library
        if (sa.hasParam("Shuffle")) {
            for (Player p : game.getPlayers()) {
                if (Iterables.any(cards, CardPredicates.isOwner(p))) {
                    p.shuffle();
                }
            }
        }
    }

}
