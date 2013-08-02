package forge.gui.cardseteditor.controllers;

import forge.Command;
import forge.gui.framework.ICDoc;


/** 
 * Controls the "analysis" panel in the cardset editor UI.
 * 
 * <br><br><i>(C at beginning of class name denotes a control class.)</i>
 *
 */
public enum CCardSetGen implements ICDoc {
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
    /*@SuppressWarnings("serial")*/
    @Override
    public void initialize() {
        /*((FLabel) VSetgen.SINGLETON_INSTANCE.getBtnRandCardpool()).setCommand(new Command() {
            @Override
            public void run() {
                newRandomConstructed();
            }
        });

        ((FLabel) VSetgen.SINGLETON_INSTANCE.getBtnRandCardSet2()).setCommand(new Command() {
            @Override  public void run() { newGenerateConstructed(2); } });

        ((FLabel) VSetgen.SINGLETON_INSTANCE.getBtnRandCardSet3()).setCommand(new Command() {
            @Override  public void run() { newGenerateConstructed(3); } });

        ((FLabel) VSetgen.SINGLETON_INSTANCE.getBtnRandCardSet5()).setCommand(new Command() {
            @Override  public void run() { newGenerateConstructed(5); } });*/
    }

    /* (non-Javadoc)
     * @see forge.gui.framework.ICDoc#update()
     */
    @Override
    public void update() {
    }

    //========== Other methods
    /*@SuppressWarnings("unchecked")
    private <TItem extends InventoryItem, TModel extends CardSetBase> void newRandomConstructed() {
        if (!SEditorIO.confirmSaveChanges()) { return; }

        final CardSet randomCardSet = new CardSet();

        Predicate<PaperCard> notBasicLand = Predicates.not(Predicates.compose(CardRulesPredicates.Presets.IS_BASIC_LAND, PaperCard.FN_GET_RULES));
        Iterable<PaperCard> source = Iterables.filter(CardDb.instance().getUniqueCards(), notBasicLand);
        randomCardSet.getMain().addAllFlat(Aggregates.random(source, 15 * 5));

        for(String landName : Constant.Color.BASIC_LANDS) { 
            randomCardSet.getMain().add(landName, 1);
        }
        randomCardSet.getMain().add("Terramorphic Expanse", 1);

        final ACEditorBase<TItem, TModel> ed = (ACEditorBase<TItem, TModel>)
                CCardSetEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        ed.getCardSetController().setModel((TModel) randomCardSet);
    }

    @SuppressWarnings("unchecked")
    private <TItem extends InventoryItem, TModel extends CardSetBase> void newGenerateConstructed(final int colorCount0) {
        if (!SEditorIO.confirmSaveChanges()) { return; }

        final CardSet genConstructed = new CardSet();

        switch (colorCount0) {
            case 2:
                genConstructed.getMain().addAll((new Generate2ColorCardSet(null, null)).getCardSet(60, false));
                break;
            case 3:
                genConstructed.getMain().addAll((new Generate3ColorCardSet(null, null, null)).getCardSet(60, false));
                break;
            case 5:
                genConstructed.getMain().addAll((new Generate5ColorCardSet()).getCardSet(60, false));
                break;
            default:
        }

        final ACEditorBase<TItem, TModel> ed = (ACEditorBase<TItem, TModel>)
                CCardSetEditorUI.SINGLETON_INSTANCE.getCurrentEditorController();

        ed.getCardSetController().setModel((TModel) genConstructed);
    }*/
}
