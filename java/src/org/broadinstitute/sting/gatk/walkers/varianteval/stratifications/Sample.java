package org.broadinstitute.sting.gatk.walkers.varianteval.stratifications;

import org.broad.tribble.util.variantcontext.VariantContext;
import org.broadinstitute.sting.gatk.contexts.ReferenceContext;
import org.broadinstitute.sting.gatk.contexts.variantcontext.VariantContextUtils;
import org.broadinstitute.sting.gatk.walkers.varianteval.VariantEvalWalker;

import java.util.ArrayList;
import java.util.Set;

public class Sample extends VariantStratifier {
    // needs the sample names
    private ArrayList<String> samples;

    @Override
    public void initialize(Set<VariantContextUtils.JexlVCMatchExp> jexlExpressions, Set<String> compNames, Set<String> knownNames, Set<String> evalNames, Set<String> sampleNames) {
        samples = new ArrayList<String>();
        samples.addAll(sampleNames);
    }

    public ArrayList<String> getAllStates() {
        return samples;
    }

    public ArrayList<String> getRelevantStates(ReferenceContext ref, VariantContext comp, String compName, VariantContext eval, String sampleName) {
        ArrayList<String> relevantStates = new ArrayList<String>();
        relevantStates.add(sampleName);

        return relevantStates;
    }
}
