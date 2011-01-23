/*
 * Copyright (c) 2010, The Broad Institute
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
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.utils.interval;

import org.broadinstitute.sting.WalkerTest;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * Test the GATK core interval parsing mechanism.
 */
public class IntervalIntegrationTest extends WalkerTest {
    @Test(enabled = true)
    public void testAllImplicitIntervalParsing() {
        String md5 = "7821db9e14d4f8e07029ff1959cd5a99";
        WalkerTest.WalkerTestSpec spec = new WalkerTest.WalkerTestSpec(
                "-T CountLoci" +
                        " -I " + validationDataLocation + "OV-0930.normal.chunk.bam" +
                        " -R " + hg18Reference +
                        " -o %s",
                        1, // just one output file
                        Arrays.asList(md5));
        executeTest("testAllIntervalsImplicit",spec);
    }

    @Test(enabled = true)
    public void testAllExplicitIntervalParsing() {
        String md5 = "7821db9e14d4f8e07029ff1959cd5a99";
        WalkerTest.WalkerTestSpec spec = new WalkerTest.WalkerTestSpec(
                "-T CountLoci" +
                        " -I " + validationDataLocation + "OV-0930.normal.chunk.bam" +
                        " -R " + hg18Reference +
                        " -L all" +
                        " -o %s",
                        1, // just one output file
                        Arrays.asList(md5));                        
        executeTest("testAllIntervalsExplicit",spec);
    }

    @Test
    public void testUnmappedReadInclusion() {
        String md5 = "fcd11cfa8474472c617d400623a30fcd";
        WalkerTest.WalkerTestSpec spec = new WalkerTest.WalkerTestSpec(
                "-T PrintReads" +
                        " -I " + validationDataLocation + "MV1994.bam" +
                        " -R " + validationDataLocation + "Escherichia_coli_K12_MG1655.fasta" +
                        " -L unmapped" +
                        " -o %s" +
                        " -U",
                        1, // just one output file
                        Arrays.asList(md5));
        executeTest("testUnmappedReadInclusion",spec);
    }

    @Test(enabled = true)
    public void testUnmappedReadExclusion() {
        String md5 = "3153593c9f9ff80a8551fff5655e65ec";
        WalkerTest.WalkerTestSpec spec = new WalkerTest.WalkerTestSpec(
                "-T PrintReads" +
                        " -I " + validationDataLocation + "MV1994.bam" +
                        " -R " + validationDataLocation + "Escherichia_coli_K12_MG1655.fasta" +
                        " -XL unmapped" +
                        " -o %s" +
                        " -U",
                        1, // just one output file
                        Arrays.asList(md5));
        executeTest("testUnmappedReadExclusion",spec);
    }


}
