/*******************************************************************************
 Copyright 2009 Sun Microsystems, Inc.,
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

package com.sun.fortress.interpreter.evaluator.tasks;

import com.sun.fortress.interpreter.evaluator.Environment;
import com.sun.fortress.interpreter.evaluator.Evaluator;
import com.sun.fortress.interpreter.evaluator.values.FValue;
import com.sun.fortress.interpreter.evaluator.values.SingleFcn;
import com.sun.fortress.useful.HasAt;

import java.util.ArrayList;
import java.util.List;

public class SpawnTask extends BaseTask {

    SingleFcn fcn;
    Evaluator eval;
    private volatile FValue val;
    private volatile Boolean resultIsReady;

    public void compute() {
        FortressTaskRunner.setCurrentTask(this);
        List<FValue> args = new ArrayList<FValue>();
        HasAt site = new HasAt.FromString("SpawnTask");
        Environment e = eval.e;
        val = fcn.functionInvocation(args, site);
        resultIsReady = true;
        fcn = null;
        eval = null;
    }

    public SpawnTask(SingleFcn sf, Evaluator e) {
        super();
        fcn = sf;
        eval = e;
        resultIsReady = false;
    }

    public void print() {
        System.out.println("Spawn Task: Function = " + fcn + " eval = " + eval + " val = " + val);
    }

    private Boolean ready() {
        return resultIsReady;
    }

    public FValue result() {
        while (!resultIsReady) {
            ;
        }
        return val;
    }

    public void waitForResult() {
        int i = 0;
        while (!resultIsReady) {
            ;
        }
    }
}
