/*
 * The MIT License
 *
 * Copyright (c) 2009 Samuel Sjoberg
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package forge.gui;

import java.awt.Rectangle;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

/**
 * A {@link JLabel} with support for multi-line text that wraps when the line
 * doesn't fit in the available width. Multi-line text support is handled by the
 * {@link MultiLineLabelUI}, the default UI delegate of this component. The text
 * in the label can be horizontally and vertically aligned, relative to the
 * bounds of the component.
 * 
 * @author Samuel Sjoberg, http://samuelsjoberg.com
 * @version 1.0.0
 */
public class MultiLineLabel extends JLabel {

    /**
     * Default serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Horizontal text alignment.
     */
    private int halign = SwingConstants.LEFT;

    /**
     * Vertical text alignment.
     */
    private int valign = SwingConstants.CENTER;

    /**
     * Cache to save heap allocations.
     */
    private Rectangle bounds;

    /**
     * Creates a new empty label.
     */
    public MultiLineLabel() {
        super();
        this.setUI(MultiLineLabelUI.getLabelUI());
    }

    /**
     * Creates a new label with <code>text</code> value.
     * 
     * @param text
     *            the value of the label
     */
    public MultiLineLabel(final String text) {
        this();
        this.setText(text);
    }

    /**
     * {@inheritDoc}
     * 
     * @return a {@link java.awt.Rectangle} object.
     */
    @Override
    public Rectangle getBounds() {
        if (this.bounds == null) {
            this.bounds = new Rectangle();
        }
        return super.getBounds(this.bounds);
    }

    /**
     * Set the vertical text alignment.
     * 
     * @param alignment
     *            vertical alignment
     */
    public void setVerticalTextAlignment(final int alignment) {
        this.firePropertyChange("verticalTextAlignment", this.valign, alignment);
        this.valign = alignment;
    }

    /**
     * Set the horizontal text alignment.
     * 
     * @param alignment
     *            horizontal alignment
     */
    public void setHorizontalTextAlignment(final int alignment) {
        this.firePropertyChange("horizontalTextAlignment", this.halign, alignment);
        this.halign = alignment;
    }

    /**
     * Get the vertical text alignment.
     * 
     * @return vertical text alignment
     */
    public int getVerticalTextAlignment() {
        return this.valign;
    }

    /**
     * Get the horizontal text alignment.
     * 
     * @return horizontal text alignment
     */
    public int getHorizontalTextAlignment() {
        return this.halign;
    }
}
