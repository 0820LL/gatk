/*
* Copyright (c) 2012 The Broad Institute
* 
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
* 
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
* 
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package org.broadinstitute.gatk.utils.R;

import org.broadinstitute.gatk.utils.io.IOUtils;
import org.broadinstitute.gatk.utils.io.Resource;

import java.io.File;

/**
 * Libraries embedded in the StingUtils package.
 */
public enum RScriptLibrary {
    GSALIB("gsalib");

    private final String name;

    private RScriptLibrary(String name) {
        this.name = name;
    }

    public String getLibraryName() {
        return this.name;
    }

    public String getResourcePath() {
        return name + ".tar.gz";
    }

    /**
     * Writes the library source code to a temporary tar.gz file and returns the path.
     * @return The path to the library source code. The caller must delete the code when done.
     */
    public File writeTemp() {
        return IOUtils.writeTempResource(new Resource(getResourcePath(), RScriptLibrary.class));
    }

    public File writeLibrary(File tempDir) {
        File libraryFile = new File(tempDir, getLibraryName());
        IOUtils.writeResource(new Resource(getResourcePath(), RScriptLibrary.class), libraryFile);
        return libraryFile;
    }
}