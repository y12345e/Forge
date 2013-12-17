package forge.gui.toolbox.itemmanager.filters;

import javax.swing.JPanel;
import javax.swing.SwingConstants;

import forge.gui.toolbox.FLabel;
import forge.gui.toolbox.FTextField;
import forge.gui.toolbox.LayoutHelper;
import forge.gui.toolbox.itemmanager.ItemManager;
import forge.item.InventoryItem;
import forge.util.TextUtil;

/** 
 * TODO: Write javadoc for this type.
 *
 */
public abstract class ListLabelFilter<T extends InventoryItem> extends ItemFilter<T> {
    private FLabel label;
    
    protected ListLabelFilter(ItemManager<? super T> itemManager0) {
        super(itemManager0);
    }

    protected abstract String getCaption();
    protected abstract Iterable<String> getList();
    protected abstract String getTooltip();
    protected abstract int getCount();

    @Override
    public final boolean isEmpty() {
        return getCount() == 0;
    }

    @Override
    protected final void buildWidget(JPanel widget) {
        StringBuilder labelBuilder = new StringBuilder();
        labelBuilder.append(getCaption());
        switch (getCount()) {
        case 0:
            labelBuilder.append(": All");
            break;
        case 1:
            labelBuilder.append(": " + getList().iterator().next());
            break;
        default:
            labelBuilder.append("s: " + TextUtil.join(getList(), ", "));
            break;
        }
        label = new FLabel.Builder()
            .text(labelBuilder.toString())
            .tooltip(getTooltip())
            .fontAlign(SwingConstants.LEFT)
            .fontSize(12)
            .build();
        widget.add(label);
    }

    @Override
    protected void doWidgetLayout(LayoutHelper helper) {
        helper.fillLine(label, FTextField.HEIGHT);
    }
}
