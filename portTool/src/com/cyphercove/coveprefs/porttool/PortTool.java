/*
 * Copyright (C) 2017 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cyphercove.coveprefs.porttool;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Copies source files verbatim into the support module, changing only the package name.
 */
public class PortTool {

    public static void main(String[] args) {
        String baseSrcPath = "library/src/main/java/com/cyphercove/coveprefs/";
        String baseDstPath = "support/src/main/java/com/cyphercove/coveprefs/support/";
        String srcPackage = "package com.cyphercove.coveprefs;";
        String dstPackage = "package com.cyphercove.coveprefs.support;";

        String[] directPorts = {
                "AboutPreference",
                "ColorPreference",
                "ImageListPreference",
                "MultiColorPreference",
                "RotaryPreference",
                "SeekBarPreference"
        };

        for (String portName : directPorts){
            File srcFile = new File(baseSrcPath + portName + ".java");
            File dstFile = new File(baseDstPath + portName + ".java");
            try {
                String fileText = FileUtils.readFileToString(srcFile);
                fileText = fileText.replace(srcPackage, dstPackage);
                FileUtils.writeStringToFile(dstFile, fileText);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
