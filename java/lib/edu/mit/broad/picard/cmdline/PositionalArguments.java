/*
* The Broad Institute
* SOFTWARE COPYRIGHT NOTICE AGREEMENT
* This software and its documentation are copyright 2008 by the
* Broad Institute/Massachusetts Institute of Technology. All rights are reserved.
*
* This software is supplied without any warranty or guaranteed support whatsoever. Neither
* the Broad Institute nor MIT can be responsible for its use, misuse, or functionality.
*/
package edu.mit.broad.picard.cmdline;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to annotate which field of a CommandLineProgram should store parameters given at the 
 * command line which are not options. Fields with this annotation must be a Collection
 * (and probably should be a List if order is important).
 * If a command line call looks like "cmd option=foo x=y bar baz" the values "bar" and "baz"
 * would be added to the collection with this annotation. The java type of the arguments
 * will be inferred from the generic type of the collection. The type must be an enum or
 * have a constructor with a single String parameter.
 *
 * @author Alec Wysoker
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface PositionalArguments {
    /** The minimum number of arguments required. */
    int minElements() default 0;
    
    /** The maximum number of arguments allowed. */
    int maxElements() default Integer.MAX_VALUE;
}
