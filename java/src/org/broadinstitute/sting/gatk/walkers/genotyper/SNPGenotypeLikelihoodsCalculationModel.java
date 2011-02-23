/*
 * Copyright (c) 2010.
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

package org.broadinstitute.sting.gatk.walkers.genotyper;

import org.broad.tribble.util.variantcontext.VariantContext;
import org.broadinstitute.sting.utils.*;
import org.broadinstitute.sting.utils.exceptions.StingException;
import org.broadinstitute.sting.utils.genotype.DiploidGenotype;
import org.broadinstitute.sting.utils.pileup.ReadBackedPileup;
import org.broadinstitute.sting.utils.pileup.PileupElement;
import org.broadinstitute.sting.gatk.contexts.StratifiedAlignmentContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.refdata.RefMetaDataTracker;
import org.broad.tribble.util.variantcontext.Allele;
import org.apache.log4j.Logger;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;

import java.util.*;

public class SNPGenotypeLikelihoodsCalculationModel extends GenotypeLikelihoodsCalculationModel {

    // the alternate allele with the largest sum of quality scores
    protected Byte bestAlternateAllele = null;

    private final boolean useAlleleFromVCF;

    protected SNPGenotypeLikelihoodsCalculationModel(UnifiedArgumentCollection UAC, Logger logger) {
        super(UAC, logger);
        useAlleleFromVCF = UAC.GenotypingMode == GENOTYPING_MODE.GENOTYPE_GIVEN_ALLELES;
    }

    public Allele getLikelihoods(RefMetaDataTracker tracker,
                                 ReferenceContext ref,
                                 Map<String, StratifiedAlignmentContext> contexts,
                                 StratifiedAlignmentContext.StratifiedContextType contextType,
                                 GenotypePriors priors,
                                 Map<String, BiallelicGenotypeLikelihoods> GLs,
                                 Allele alternateAlleleToUse) {

        if ( !(priors instanceof DiploidSNPGenotypePriors) )
            throw new StingException("Only diploid-based SNP priors are supported in the SNP GL model");

        byte refBase = ref.getBase();
        Allele refAllele = Allele.create(refBase, true);

        // find the alternate allele with the largest sum of quality scores
        if ( alternateAlleleToUse != null ) {
            bestAlternateAllele = alternateAlleleToUse.getBases()[0];
        } else if ( useAlleleFromVCF ) {
            final VariantContext vcInput = tracker.getVariantContext(ref, "alleles", null, ref.getLocus(), true);
            if ( vcInput == null )
                return null;
            if ( !vcInput.isSNP() ) {
                logger.info("Record at position " + ref.getLocus() + " is not a SNP; skipping...");
                return null;
            }
            if ( !vcInput.isBiallelic() ) {
                logger.info("Record at position " + ref.getLocus() + " is not bi-allelic; choosing the first allele...");
                //return null;
            }
            bestAlternateAllele = vcInput.getAlternateAllele(0).getBases()[0];
        } else {
            initializeBestAlternateAllele(refBase, contexts);
        }

        // if there are no non-ref bases...
        if ( bestAlternateAllele == null ) {
            // if we only want variants, then we don't need to calculate genotype likelihoods
            if ( UAC.OutputMode == UnifiedGenotyperEngine.OUTPUT_MODE.EMIT_VARIANTS_ONLY )
                return refAllele;

            // otherwise, choose any alternate allele (it doesn't really matter)
            bestAlternateAllele = (byte)(refBase != 'A' ? 'A' : 'C');
        }

        Allele altAllele = Allele.create(bestAlternateAllele, false);

        for ( Map.Entry<String, StratifiedAlignmentContext> sample : contexts.entrySet() ) {
            ReadBackedPileup pileup = sample.getValue().getContext(contextType).getBasePileup();

            // create the GenotypeLikelihoods object
            DiploidSNPGenotypeLikelihoods GL = new DiploidSNPGenotypeLikelihoods((DiploidSNPGenotypePriors)priors, UAC.PCR_error);
            int nGoodBases = GL.add(pileup, true, true);
            if ( nGoodBases == 0 )
                continue;

            double[] likelihoods = GL.getLikelihoods();

            DiploidGenotype refGenotype = DiploidGenotype.createHomGenotype(refBase);
            DiploidGenotype hetGenotype = DiploidGenotype.createDiploidGenotype(refBase, bestAlternateAllele);
            DiploidGenotype homGenotype = DiploidGenotype.createHomGenotype(bestAlternateAllele);
            GLs.put(sample.getKey(), new BiallelicGenotypeLikelihoods(sample.getKey(),
                    refAllele,
                    altAllele,
                    likelihoods[refGenotype.ordinal()],
                    likelihoods[hetGenotype.ordinal()],
                    likelihoods[homGenotype.ordinal()],
                    getFilteredDepth(pileup)));
        }

        return refAllele;
    }

    protected void initializeBestAlternateAllele(byte ref, Map<String, StratifiedAlignmentContext> contexts) {
        int[] qualCounts = new int[4];

        for ( Map.Entry<String, StratifiedAlignmentContext> sample : contexts.entrySet() ) {
            // calculate the sum of quality scores for each base
            ReadBackedPileup pileup = sample.getValue().getContext(StratifiedAlignmentContext.StratifiedContextType.COMPLETE).getBasePileup();
            for ( PileupElement p : pileup ) {
                // ignore deletions and filtered bases
                if ( p.isDeletion() ||
                     (p.getRead() instanceof GATKSAMRecord && !((GATKSAMRecord)p.getRead()).isGoodBase(p.getOffset())) )
                    continue;

                int index = BaseUtils.simpleBaseToBaseIndex(p.getBase());
                if ( index >= 0 )
                    qualCounts[index] += p.getQual();
            }
        }

        // set the non-ref base with maximum quality score sum
        int maxCount = 0;
        bestAlternateAllele = null;
        for ( byte altAllele : BaseUtils.BASES ) {
            if ( altAllele == ref )
                continue;
            int index = BaseUtils.simpleBaseToBaseIndex(altAllele);
            if ( qualCounts[index] > maxCount ) {
                maxCount = qualCounts[index];
                bestAlternateAllele = altAllele;
            }
        }
    }
}