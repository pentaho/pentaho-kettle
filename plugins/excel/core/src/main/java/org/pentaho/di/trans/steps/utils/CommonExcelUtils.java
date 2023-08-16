/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2023 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.di.trans.steps.utils;

import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.util.EnvUtil;

public class CommonExcelUtils {

    /**
     * This method is responsible for setting the configuration values that control how the ZipSecureFile class behaves
     * when trying to detect zipbombs (check PDI-17586 for more details).
     */
    public static void setZipBombConfiguration() {

        // The minimum allowed ratio between de- and inflated bytes to detect a zipbomb.
        String minInflateRatioVariable =
                EnvUtil
                        .getSystemProperty( Const.KETTLE_ZIP_MIN_INFLATE_RATIO, Const.KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT_STRING );
        double minInflateRatio;
        try {
            minInflateRatio = Const.checkXlsxZipBomb() ? Double.parseDouble( minInflateRatioVariable )
                    : Const.KETTLE_ZIP_NEGATIVE_MIN_INFLATE;
        } catch ( NullPointerException | NumberFormatException e ) {
            minInflateRatio = Const.KETTLE_ZIP_MIN_INFLATE_RATIO_DEFAULT;
        }
        ZipSecureFile.setMinInflateRatio( minInflateRatio );

        // The maximum file size of a single zip entry.
        String maxEntrySizeVariable =
                EnvUtil.getSystemProperty( Const.KETTLE_ZIP_MAX_ENTRY_SIZE, Const.KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT_STRING );
        long maxEntrySize;
        try {
            maxEntrySize = Long.parseLong( maxEntrySizeVariable );
        } catch ( NullPointerException | NumberFormatException e ) {
            maxEntrySize = Const.KETTLE_ZIP_MAX_ENTRY_SIZE_DEFAULT;
        }
        ZipSecureFile.setMaxEntrySize( maxEntrySize );

        // The maximum number of characters of text that are extracted before an exception is thrown during extracting
        // text from documents.
        String maxTextSizeVariable =
                EnvUtil.getSystemProperty( Const.KETTLE_ZIP_MAX_TEXT_SIZE, Const.KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT_STRING );
        long maxTextSize;
        try {
            maxTextSize = Long.parseLong( maxTextSizeVariable );
        } catch ( NullPointerException | NumberFormatException e ) {
            maxTextSize = Const.KETTLE_ZIP_MAX_TEXT_SIZE_DEFAULT;
        }
        ZipSecureFile.setMaxTextSize( maxTextSize );
    }
}
