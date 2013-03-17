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

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;

import forge.item.CardDb;
import forge.item.CardPrinted;
import forge.properties.NewConstants;

@SuppressWarnings("serial")
public class GuiDownloadSetPicturesLQ extends GuiDownloader {
    public GuiDownloadSetPicturesLQ() {
        super();
    }

    @Override
    protected final ArrayList<DownloadObject> getNeededImages() {
        ArrayList<DownloadObject> downloads = new ArrayList<DownloadObject>();

        for (final CardPrinted c : Iterables.concat(CardDb.instance().getAllCards(), CardDb.variants().getAllCards())) {
            final String setCode3 = c.getEdition();
            if (StringUtils.isBlank(setCode3) || "???".equals(setCode3)) {
             // we don't want cards from unknown sets
                continue;
            }
            
            addDLObject(c.getImageUrlPath(false), c.getImageKey(), downloads);

            String backFaceImage = c.getImageKey(true);
            if (backFaceImage != null) {
                addDLObject(c.getImageUrlPath(true), backFaceImage, downloads);
            }
        }

        // Add missing tokens to the list of things to download.
        addMissingItems(downloads, NewConstants.IMAGE_LIST_TOKENS_FILE, NewConstants.CACHE_TOKEN_PICS_DIR);

        return downloads;
    }

    private void addDLObject(String urlPath, String filename, ArrayList<DownloadObject> downloads) {
        File destFile = new File(NewConstants.CACHE_CARD_PICS_DIR, filename + ".jpg");
        if (!destFile.exists()) {
            downloads.add(new DownloadObject(NewConstants.URL_PIC_DOWNLOAD + urlPath, destFile));
        }
    }
}
