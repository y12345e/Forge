/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package forge.gui.download;

import forge.properties.NewConstants;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class GuiDownloadPrices extends GuiDownloader {
    public GuiDownloadPrices() {
        super();
    }

    @Override
    protected Map<String, String> getNeededImages() {
        Map<String, String> result = new HashMap<String, String>();
        result.put(NewConstants.QUEST_CARD_PRICE_FILE, NewConstants.URL_PRICE_DOWNLOAD);
        return result;
    }
}
