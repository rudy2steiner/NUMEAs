package jmetal.operators.selection;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.util.HashMap;

/**
 * Created by Rudy Steiner on 2016/11/9.
 */
public class BinaryTournament3 extends  Selection{
    @Override
    public Object execute(Object object) throws JMException {
        Object[] objects=(Object[])object;
        int current=((Integer)objects[0]).intValue();

        SolutionSet solutionSet = (SolutionSet) objects[1];
       int r1,r2;
        Solution solution1, solution2;
        do {
            r1 = PseudoRandom.randInt(0,solutionSet.size() - 1);
        } while (r1 == current);
        do {
            r2= PseudoRandom.randInt(0,solutionSet.size() - 1);
        } while (r2== r1||r2== current);
         solution1=solutionSet.get(r1);
         solution2=solutionSet.get(r2);
        if(solution1.getRank()>solution2.getRank()) return solution2;
        else if(solution1.getRank()<solution2.getRank()) return solution1;
        else {
            if(solution1.getClusterID()!=solution2.getClusterID()){
                int count1=0,count2=0;
                for(int i=0;i<solutionSet.size();i++){
                     if(solutionSet.get(i).getClusterID()==solution1.getClusterID()) count1++;
                     else if(solutionSet.get(i).getClusterID()==solution2.getClusterID()) count2++;
                }
                if(count1<=count2) return solution1;
                else return solution2;
            }else{
                if(solution1.getFitness()<=solution2.getFitness()) return solution1;
                else return solution2;
            }
        }
    }

    public BinaryTournament3(HashMap<String, Object> parameters) {
        super(parameters);

    }
}
