package forge.gui.cardseteditor.controllers;

import java.util.Map.Entry;

import javax.swing.JLabel;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import forge.Command;
import forge.card.CardRules;
import forge.card.CardRulesPredicates;
import forge.card.MagicColor;
import forge.cardset.CardSetBase;
import forge.gui.cardseteditor.CCardSetEditorUI;
import forge.gui.cardseteditor.SEditorUtil;
import forge.gui.cardseteditor.views.VStatistics;
import forge.gui.framework.ICDoc;
import forge.item.PaperCard;
import forge.item.InventoryItem;
import forge.item.ItemPool;
import forge.item.ItemPoolView;


/** 
 * Controls the "analysis" panel in the cardset editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CStatistics implements ICDoc {
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
    public void initialize() {
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
        analyze();
    }

    private void setLabelValue(JLabel label, ItemPoolView<PaperCard> cardset, Predicate<CardRules> predicate, int total) {
        int tmp = cardset.countAll(Predicates.compose(predicate, PaperCard.FN_GET_RULES));
        label.setText(tmp + " (" + SEditorUtil.calculatePercentage(tmp, total) + "%)");

    }

    //========== Other methods
    @SuppressWarnings("unchecked")
    private <T extends InventoryItem, TModel extends CardSetBase> void analyze() {
        final ACEditorBase<T, TModel> ed = (ACEditorBase<T, TModel>)
                CCardSetEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        if (ed == null) { return; }

        final ItemPoolView<PaperCard> cardset = ItemPool.createFrom(ed.getTableCardSet().getCards(), PaperCard.class);

        int total = cardset.countAll();

        // Hack-ish: avoid /0 cases, but still populate labels :)
        if (total == 0) { total = 1; }


        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblCreature(), cardset, CardRulesPredicates.Presets.IS_CREATURE, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblLand(), cardset, CardRulesPredicates.Presets.IS_LAND, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblEnchantment(), cardset, CardRulesPredicates.Presets.IS_ENCHANTMENT, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblArtifact(), cardset, CardRulesPredicates.Presets.IS_ARTIFACT, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblInstant(), cardset, CardRulesPredicates.Presets.IS_INSTANT, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblSorcery(), cardset, CardRulesPredicates.Presets.IS_SORCERY, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblPlaneswalker(), cardset, CardRulesPredicates.Presets.IS_PLANESWALKER, total);

        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblMulti(), cardset, CardRulesPredicates.Presets.IS_MULTICOLOR, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblColorless(), cardset, CardRulesPredicates.Presets.IS_COLORLESS, total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlack(), cardset, CardRulesPredicates.isMonoColor(MagicColor.BLACK), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblBlue(), cardset, CardRulesPredicates.isMonoColor(MagicColor.BLUE), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblGreen(), cardset, CardRulesPredicates.isMonoColor(MagicColor.GREEN), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblRed(), cardset, CardRulesPredicates.isMonoColor(MagicColor.RED), total);
        setLabelValue(VStatistics.SINGLETON_INSTANCE.getLblWhite(), cardset, CardRulesPredicates.isMonoColor(MagicColor.WHITE), total);

        int cmc0 = 0, cmc1 = 0, cmc2 = 0, cmc3 = 0, cmc4 = 0, cmc5 = 0, cmc6 = 0;
        int tmc = 0;

        for (final Entry<PaperCard, Integer> e : cardset) {
            final CardRules cardRules = e.getKey().getRules();
            final int count = e.getValue();
            final int cmc = cardRules.getManaCost().getCMC();

            if (cmc == 0)       { cmc0 += count; }
            else if (cmc == 1)  { cmc1 += count; }
            else if (cmc == 2)  { cmc2 += count; }
            else if (cmc == 3)  { cmc3 += count; }
            else if (cmc == 4)  { cmc4 += count; }
            else if (cmc == 5)  { cmc5 += count; }
            else if (cmc >= 6)  { cmc6 += count; }

            tmc += (cmc * count);
        }

        VStatistics.SINGLETON_INSTANCE.getLblCMC0().setText(
                cmc0 + " (" + SEditorUtil.calculatePercentage(cmc0, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC1().setText(
                cmc1 + " (" + SEditorUtil.calculatePercentage(cmc1, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC2().setText(
                cmc2 + " (" + SEditorUtil.calculatePercentage(cmc2, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC3().setText(
                cmc3 + " (" + SEditorUtil.calculatePercentage(cmc3, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC4().setText(
                cmc4 + " (" + SEditorUtil.calculatePercentage(cmc4, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC5().setText(
                cmc5 + " (" + SEditorUtil.calculatePercentage(cmc5, total) + "%)");
        VStatistics.SINGLETON_INSTANCE.getLblCMC6().setText(
                cmc6 + " (" + SEditorUtil.calculatePercentage(cmc6, total) + "%)");

        double amc = Math.round((double) tmc / (double) total * 100) / 100.0d;

        VStatistics.SINGLETON_INSTANCE.getLblTotal().setText("TOTAL CARDS: " + cardset.countAll());
        VStatistics.SINGLETON_INSTANCE.getLblTMC().setText("TOTAL MANA COST: " + tmc);
        VStatistics.SINGLETON_INSTANCE.getLblAMC().setText("AVERAGE MANA COST: " + amc);
    }
}
