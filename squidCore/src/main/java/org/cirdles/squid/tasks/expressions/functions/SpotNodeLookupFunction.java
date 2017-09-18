/*
 * Copyright 2017 CIRDLES.org.
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
package org.cirdles.squid.tasks.expressions.functions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.List;
import org.cirdles.squid.exceptions.SquidException;
import org.cirdles.squid.shrimp.ShrimpFractionExpressionInterface;
import org.cirdles.squid.tasks.TaskInterface;
import org.cirdles.squid.tasks.expressions.expressionTrees.ExpressionTreeInterface;
import org.cirdles.squid.tasks.expressions.spots.SpotNode;

/**
 *
 * @author James F. Bowring
 */
public class SpotNodeLookupFunction extends Function {
    //    private static final long serialVersionUID = 6522574920235718028L;
    private void readObject(
            ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        ObjectStreamClass myObject = ObjectStreamClass.lookup(Class.forName(SpotNodeLookupFunction.class.getCanonicalName()));
        long theSUID = myObject.getSerialVersionUID();
        System.out.println("Customized De-serialization of SpotNodeLookupFunction " + theSUID);
    }
    private String methodNameForShrimpFraction;
    private SpotNode spotNode;

    public SpotNodeLookupFunction() {
        name = "lookup";
        argumentCount = 1;
        precedence = 4;
        rowCount = 1;
        colCount = 1;
        
        methodNameForShrimpFraction = "";
        spotNode = null;
    }

    @Override
    public String[][] getLabelsForOutputValues() {
        labelsForOutputValues = new String[][]{{"Lookup Field: " + methodNameForShrimpFraction.replace("get", "")}};
        return super.getLabelsForOutputValues(); 
    }
    
    

    /**
     * Only child is a SpotNode specifying the lookup method for a shrimpfraction (spot).
     *
     * @param childrenET
     * @param shrimpFractions
     * @param task
     * @return
     * @throws SquidException
     */
    @Override
    public Object[][] eval(List<ExpressionTreeInterface> childrenET, List<ShrimpFractionExpressionInterface> shrimpFractions, TaskInterface task) throws SquidException {
        //TODO refactor duplicate code
        spotNode = ((SpotNode) childrenET.get(0));
        methodNameForShrimpFraction = spotNode.getMethodNameForShrimpFraction();        

        Object[][] results = spotNode.eval(shrimpFractions, task);

        return results;
    }

    @Override
    public String toStringMathML(List<ExpressionTreeInterface> childrenET) {
        spotNode = ((SpotNode) childrenET.get(0));
        methodNameForShrimpFraction = spotNode.getMethodNameForShrimpFraction();

        String retVal
                = "<mrow>"
                + "<mi>" + name + "</mi>"
                + "<mfenced>"
                + spotNode.toStringMathML()
                + "</mfenced></mrow>\n";

        return retVal;
    }

    /**
     * @return the methodNameForShrimpFraction
     */
    public String getMethodNameForShrimpFraction() {
        return methodNameForShrimpFraction;
    }

}
