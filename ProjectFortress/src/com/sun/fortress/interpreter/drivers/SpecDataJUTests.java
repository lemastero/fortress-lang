/*******************************************************************************
    Copyright 2007 Sun Microsystems, Inc.,
    4150 Network Circle, Santa Clara, California 95054, U.S.A.
    All rights reserved.

    U.S. Government Rights - Commercial software.
    Government users are subject to the Sun Microsystems, Inc. standard
    license agreement and applicable provisions of the FAR and its supplements.

    Use is subject to license terms.

    This distribution may include materials developed by third parties.

    Sun, Sun Microsystems, the Sun logo and Java are trademarks or registered
    trademarks of Sun Microsystems, Inc. in the U.S. and other countries.
 ******************************************************************************/
package com.sun.fortress.interpreter.drivers;

import junit.framework.Test;
import junit.framework.TestSuite;

public class SpecDataJUTests {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        String testDir1 = System.getenv("FORTRESS_HOME") + "/SpecData/examples/basic";
        String testDir2 = System.getenv("FORTRESS_HOME") + 
            "/SpecData/examples/preliminaries";
        String testDir3 = System.getenv("FORTRESS_HOME") + 
            "/SpecData/examples/advanced";
        boolean failsOnly = ! ("1".equals(System.getenv("FORTRESS_JUNIT_VERBOSE")));
        TestSuite suite = new TestSuite("Test all .fss files in 'SpecData/examples'.");
        //$JUnit-BEGIN$
        suite.addTest(FileTests.suite(testDir1, failsOnly, false));
        suite.addTest(FileTests.suite(testDir2, failsOnly, false));
        suite.addTest(FileTests.suite(testDir3, failsOnly, false));
        //$JUnit-END$
        return suite;
    }

}
