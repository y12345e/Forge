package forge.card.trigger;

import java.util.HashMap;

import forge.Card;
import forge.card.spellability.SpellAbility;

/**
 * <p>
 * Trigger_Discarded class.
 * </p>
 * 
 * @author Forge
 * @version $Id$
 */
public class Trigger_Discarded extends Trigger {

    /**
     * <p>
     * Constructor for Trigger_Discarded.
     * </p>
     * 
     * @param params
     *            a {@link java.util.HashMap} object.
     * @param host
     *            a {@link forge.Card} object.
     * @param intrinsic
     *            the intrinsic
     */
    public Trigger_Discarded(final HashMap<String, String> params, final Card host, final boolean intrinsic) {
        super(params, host, intrinsic);
    }

    /** {@inheritDoc} */
    @Override
    public final boolean performTest(final java.util.Map<String, Object> runParams2) {
        if (mapParams.containsKey("ValidCard")) {
            if (!matchesValid(runParams2.get("Card"), mapParams.get("ValidCard").split(","), hostCard)) {
                return false;
            }
        }

        if (mapParams.containsKey("ValidPlayer")) {
            if (!matchesValid(runParams2.get("Player"), mapParams.get("ValidPlayer").split(","), hostCard)) {
                return false;
            }
        }

        if (mapParams.containsKey("ValidCause")) {
            if (runParams2.get("Cause") == null) {
                return false;
            }
            if (!matchesValid(runParams2.get("Cause"), mapParams.get("ValidCause").split(","), hostCard)) {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final Trigger getCopy() {
        Trigger copy = new Trigger_Discarded(mapParams, hostCard, isIntrinsic);
        if (overridingAbility != null) {
            copy.setOverridingAbility(overridingAbility);
        }
        copy.setName(name);
        copy.setID(ID);

        return copy;
    }

    /** {@inheritDoc} */
    @Override
    public final void setTriggeringObjects(final SpellAbility sa) {
        sa.setTriggeringObject("Card", runParams.get("Card"));
    }
}
