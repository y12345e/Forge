package forge.gui.game;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by IntelliJ IDEA.
 * User: dhudson
 */
@Test(groups = {"UnitTest"})
public class CardDetailPanelTest {
    /**
     *
     *
     */
    @Test(groups = {"UnitTest", "fast"})
    public void cardDetailPanelTest1() {
        try {
            CardDetailPanel dialog = new CardDetailPanel(null);
            //dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
            Assert.assertNotNull(dialog);
            dialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
