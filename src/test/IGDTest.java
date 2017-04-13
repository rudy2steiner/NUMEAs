package test;

import Jama.Matrix;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.metaheuristics.Config;
import jmetal.quality.IGD;
import jmetal.util.*;
import org.junit.Test;

/**
 * Created by Rudy Steiner on 2016/9/19.
 */
public class IGDTest {
    @Test
    public void saveIGDArrayTest(){
        Double[] data={1.2 ,1.3 ,1.4};

          System.out.println(PseudoRandom.randDouble());
        //FileUtils.saveArrayToFile(this.getClass(),"src/resources/igd/","ddd.dat",data);
        Object[] objs=new Object[2];
        Solution current=new Solution();
        Solution[] solutions=new Solution[2];
        objs[0]=current;
        objs[1]=solutions;
       // while (true)
            System.out.println( PRNG.nextGaussian(0.1,0.15));
    }

    @Test
    public void evaluateIGD() throws JMException{
        Problem problem_;
        String probName = "SDTLZ1";
        int nobj =8;
        int populationSize_;
        problem_ = Config.setProblem(probName, nobj);

        int[] divs = Config.setDivs(nobj);
        int maxGenerations_ = Config.setMaxGenerations(probName, nobj);
        String  R="R7";
        switch(nobj){
            case 3:
                populationSize_=92;
                break;
            case 5:
                populationSize_=212;
                break;
            case 8:
                populationSize_=156;
                break;
            case 10:
                populationSize_=276;
                break;
            case 15:
                populationSize_=136;
                break;
            default:
                populationSize_=92;
        }
        Matrix PF= ReadMatrix.readMatrix("/resources/pf/"+problem_.getName()+"("+ problem_.getNumberOfObjectives()+").dat", problem_.getNumberOfObjectives());
        Matrix solMatrix= ReadMatrix.readMatrix("/resources/pf_solution/PFs_"+problem_.getName()+"("+ problem_.getNumberOfObjectives()+")_"+R+".dat", problem_.getNumberOfObjectives());
        Solution solution;
        SolutionSet solutionSet=new SolutionSet(solMatrix.getRowDimension());

        IGD igd=new IGD( PF,populationSize_,maxGenerations_,10);
        if(problem_.getName().startsWith("WFG")||problem_.getName().startsWith("SDTLZ")){
            igd.setToNormalize(false);
            igd.setProblemName(problem_.getName());
            igd.setM(problem_.getNumberOfObjectives());
        }
        double[] nadir=igd.getNadirPoint();
        for (int i = 0; i < solMatrix.getRowDimension(); i++) {
            solution = new Solution(solMatrix.getColumnDimension());
            for (int j = 0; j < solMatrix.getColumnDimension(); j++) {
                solution.setObjective(j, solMatrix.get(i, j)/nadir[j]);
            }
            solutionSet.add(solution);
        }
         System.out.println( igd.evaluate(solutionSet));
    }



}
