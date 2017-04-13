package jmetal.quality;

import Jama.Matrix;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.util.Distance;
import jmetal.util.FileUtils;
import jmetal.util.JMException;

/**
 * Created by Rudy Steiner on 2016/9/18.
 */
public class IGD {
    private static final int DEFAULT_INTERVAL =10;
    private Double[] igd;
    private Matrix referenceSolution;
    private int maxGenerations;
    private int interval= DEFAULT_INTERVAL;
    private int index;
    private int popSize;
    private int M;
    private boolean toNormalize =false;         //to Normalize NonDominatedSolutionSet before calculate IGD ,default false;
    private String problemName;
    public IGD( Matrix referenceSolution,int popSize,int maxGenerations,int interval) {
        this.referenceSolution = referenceSolution;
        this.maxGenerations=maxGenerations;
        this.interval=interval;
        this.popSize=popSize;
        this.igd=new Double[(maxGenerations/this.interval+1)*2];
    }

    public double evaluate(SolutionSet nonDominantSolution) {
        double sum = 0.0;
        double[] nadirPoint=null;
        if(toNormalize)
              nadirPoint=getNadirPoint();
        Solution solution = new Solution(referenceSolution.getColumnDimension());
        for (int i = 0; i < referenceSolution.getRowDimension(); i++) {
            for (int j = 0; j < referenceSolution.getColumnDimension(); j++)
                solution.setObjective(j, referenceSolution.get(i, j));
            try {
               if(toNormalize)
                       sum += new Distance().distanceToSolutionSetInObjectiveSpace(solution, nonDominantSolution,nadirPoint);
                else
                       sum += new Distance().distanceToSolutionSetInObjectiveSpace(solution, nonDominantSolution);
            } catch (JMException e) {
                e.printStackTrace();
            }
        }
        return sum / referenceSolution.getRowDimension();
    }
    public boolean addIGDItem(SolutionSet nonDominantSolution){

           double  functionEvaluation=getLastFunctionEvaluation(index)+popSize*interval;
            igd[index++]=functionEvaluation;
            igd[index++]=evaluate(nonDominantSolution);
        return true;
    }
    public void empty(){
        igd=new Double[(maxGenerations/interval+1)*2];
        index=0;
    }
    private double getLastFunctionEvaluation(int index){
        if(index>=2)
            return  igd[index-2];
        else return popSize;
    }
    public void out(String path,String name){
        FileUtils.saveArrayToFile(this.getClass(),path,name,igd);
    }

    private double[] maxObjectivesInSolutionSet(SolutionSet solutionSet){
      int numberOfObjectives=solutionSet.get(0).getNumberOfObjectives();
      double[] max=new double[numberOfObjectives];

      for(int i=0;i<solutionSet.size();i++){
          for(int j=0;j<numberOfObjectives;j++){
              if(solutionSet.get(i).getObjective(j)>max[j])
                  max[j]=solutionSet.get(i).getObjective(j);
          }
      }
      return max;
  }

   public  double[] getNadirPoint(){

       if(problemName.startsWith("SDTLZ")){
          return generateSDTLZNadirPoint();
       }else if(problemName.startsWith("WFG")){
          return  generateWFGNadirPoint();
       }else
          return  generateSDTLZNadirPoint();
   }

    public  void setProblemName(String problemName) {
        this.problemName = problemName;
    }

    public void setM(int m) {
        M = m;
    }
    public  void setToNormalize(boolean toNormalize) {
        this.toNormalize = toNormalize;
    }

    private double[] generateWFGNadirPoint(){
        double[] nadirPoint=new double[M];
        for(int i=0;i<M;i++){
            nadirPoint[i]=2*(i+1);
        }
        return nadirPoint;
    }
    /**
     *  reference  Paper NSGA-III
     *   parameter base, magnitude
     *
     * */
    private double[] generateSDTLZNadirPoint(){
           double  magnitude=1.0;

           if(M==3||M==5){
              return  scale(M,10,magnitude);
           }else if(M==8){
               return  scale(M,3,magnitude);
           } else if(M==10){
               if(problemName.equals("SDTLZ1"))
                       return  scale(M,2,magnitude);
               else if(problemName.equals("SDTLZ2"))
                       return  scale(M,3,magnitude);
               else   return  scale(M,2,magnitude);
           }else if(M==15){
               if(problemName.equals("SDTLZ1"))
                    return  scale(M,1.2,magnitude);
               else if(problemName.equals("SDTLZ2"))
                    return  scale(M,2,magnitude);
               else  return  scale(M,1.2,magnitude);
           }else{
                   return scale(M,1,magnitude);
           }
    }

    private double[]  scale(int numberOfObjectives,double base,double magnitude){
        double[] nadirPoint=new double[numberOfObjectives];
        for(int i=0;i<numberOfObjectives;i++){
            nadirPoint[i]= magnitude*Math.pow(base,i);
        }
        return nadirPoint;
    }
}


