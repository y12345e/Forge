package forge.gui.cardseteditor.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import forge.Command;
import forge.cardset.CardSetBase;
import forge.gui.cardseteditor.CCardSetEditorUI;
import forge.gui.cardseteditor.views.VCardSetProbabilities;
import forge.gui.framework.ICDoc;
import forge.gui.toolbox.FLabel;
import forge.item.PaperCard;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.ItemPoolView;
import forge.util.MyRandom;

/** 
 * Controls the "analysis" panel in the cardset editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CCardSetProbabilities implements ICDoc {
    /** */
    SINGLETON_INSTANCE;

    //========== Overridden methods

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#getCommandOnSelect()
     */
    @Override
    public Command getCommandOnSelect() {
        return null;
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#initialize()
     */
    @Override
    @SuppressWarnings("serial")
    public void initialize() {
        ((FLabel) VCardSetProbabilities.SINGLETON_INSTANCE.getLblReshuffle()).setCommand(
            new Command() { @Override  public void run() { update(); } });
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        VCardSetProbabilities.SINGLETON_INSTANCE.rebuildLabels(analyze());
    }

    //========== Other methods
    @SuppressWarnings("unchecked")
    private <T extends InventoryItem, TModel extends CardSetBase> List<String> analyze() {
        final ACEditorBase<T, TModel> ed = (ACEditorBase<T, TModel>)
                CCardSetEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        if (ed == null) { return new ArrayList<String>(); }
        
        final ItemPoolView<PaperCard> cardset = ItemPool.createFrom(ed.getTableCardSet().getCards(), PaperCard.class);

        final List<String> cardProbabilities = new ArrayList<String>();

        final List<PaperCard> shuffled = cardset.toFlatList();
        Collections.shuffle(shuffled, MyRandom.getRandom());

        // Log totals of each card for decrementing
        final Map<PaperCard, Integer> cardTotals = new HashMap<PaperCard, Integer>();
        for (final PaperCard c : shuffled) {
            if (cardTotals.containsKey(c)) { cardTotals.put(c, cardTotals.get(c) + 1); }
            else { cardTotals.put(c, 1); }
        }

        // Run through shuffled cardset and calculate probabilities.
        // Formulas is (remaining instances of this card / total cards remaining)
        final Iterator<PaperCard> itr = shuffled.iterator();
        PaperCard tmp;
       // int prob;
        while (itr.hasNext()) {
            tmp = itr.next();

           // prob = SEditorUtil.calculatePercentage(
             //       cardTotals.get(tmp), shuffled.size());

            cardTotals.put(tmp, cardTotals.get(tmp) - 1);
            cardProbabilities.add(tmp.getName()); // + " (" + prob + "%)");
            itr.remove();
        }

        return cardProbabilities;
    }
}
