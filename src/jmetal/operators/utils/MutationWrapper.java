package jmetal.operators.utils;

import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;

/**
 * Created by Rudy Steiner on 2017/4/14.
 */
public class MutationWrapper {

    public static void NonUniformMutation(SolutionSet parentPop, SolutionSet offSpringPop,Operator mutation,Problem pro,
                                          int current, int curGeneration){
        Solution sol = new Solution(parentPop.get(current));
        mutation.setParameter("currentIteration",curGeneration);
        try {
            mutation.execute(sol);
            pro.evaluate(sol);
            offSpringPop.add(sol);
        }catch (JMException e){
            e.printStackTrace();
        }
    }

}
