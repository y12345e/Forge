package forge.toolbox;

import com.badlogic.gdx.graphics.g2d.BitmapFont.HAlignment;
import forge.Forge.Graphics;
import forge.assets.FImage;
import forge.assets.FSkinColor;
import forge.assets.FSkinColor.Colors;

public class FRadioButton extends FLabel {
    private static final FSkinColor CHECK_COLOR = FSkinColor.get(Colors.CLR_TEXT);
    private static final FSkinColor BOX_COLOR = CHECK_COLOR.alphaColor(0.5f);

    public FRadioButton() {
        this("");
    }
    public FRadioButton(String text0) {
        super(new Builder().align(HAlignment.LEFT).selectable());
        this.setIcon(new CheckBoxIcon());
    }

    private class CheckBoxIcon implements FImage {
        @Override
        public float getWidth() {
            return FRadioButton.this.getHeight();
        }

        @Override
        public float getHeight() {
            return FRadioButton.this.getHeight();
        }

        @Override
        public void draw(Graphics g, float x, float y, float w, float h) {
            g.drawRect(1, BOX_COLOR, x, y, w, h);
            if (isSelected()) {
                //draw check mark
                x += 3;
                y++;
                w -= 6;
                h -= 3;
                g.drawLine(2, CHECK_COLOR, x, y + h / 2, x + w / 2, y + h);
                g.drawLine(2, CHECK_COLOR, x + w / 2, y + h, x + w, y);
            }
        }
    }
}
