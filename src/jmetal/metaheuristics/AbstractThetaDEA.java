package jmetal.metaheuristics;

import Jama.Matrix;
import jmetal.core.*;
import jmetal.quality.IGD;
import jmetal.util.*;
import jmetal.util.ranking.NondominatedRanking;
import jmetal.util.ranking.Ranking;
import jmetal.util.ranking.ThetaRanking;
import jmetal.util.vector.TwoLevelWeightVectorGenerator;
import jmetal.util.vector.VectorGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static jmetal.util.StringUtils.combine;

/**
 * Created by Rudy Steiner on 2017/4/13.
 */
public abstract class AbstractThetaDEA extends Algorithm{
    private static Map<String,Matrix> paretoFrontCache=new ConcurrentHashMap<String,Matrix>();
    private static final int DEFAULT_INTERVAL =10;
    private int populationSize_;   // population size
    private SolutionSet population_;   // current population
    private SolutionSet offspringPopulation_;  // offspring population
    private SolutionSet union_;    // the union of current population and offspring population
    private int generations_;   // generations
    /* if only one layer is adopted, div2_=0 */
    private int div1_;  // divisions in the boundary layer
    private int div2_;  // divisions in the inside layer
    private double theta_;     // parameter theta
    private boolean normalize_;  // normalization or not
    private double[][] lambda_; // reference points
    private double[] zideal_;   // ideal point
    private double[] znadir_;   // nadir point
    private double[][] extremePoints_; // extreme points
    int maxGenerations;
    int interval=DEFAULT_INTERVAL;
    private String algorithmName;
    private IGD igd;
    private String pfRoot;
    private String pfSuffix;
    private int run;
    public AbstractThetaDEA(Problem problem,String pfRoot,String pfSuffix,int curRun){
        super(problem);
        this.pfRoot=pfRoot;
        this.pfSuffix=pfSuffix;
        this.run=curRun;
    }
    @Override
    public SolutionSet execute() throws JMException, ClassNotFoundException {
        init(pfRoot,pfSuffix,run);
        Configuration.logger_.info(combine(getAlgName()," running:",problem_.getName(),",number of obj: ",
                String.valueOf(problem_.getNumberOfObjectives()),",theta:",String.valueOf(theta_)));
        Configuration.logger_.info(combine(getAlgName()," run:",String.valueOf( run)));
        initPopulation();   // initialize the population;
        initIdealPoint();  // initialize the ideal point
        initNadirPoint();    // initialize the nadir point
        initExtremePoints(); // initialize the extreme points
        Ranking ranking = new NondominatedRanking(population_);
        igd.addIGDItem(ranking.getSubfront(0));
        while (generations_ <=maxGenerations) {
            offspringPopulation_=new SolutionSet(populationSize_);
            createOffSpringPopulation(population_,offspringPopulation_,problem_,generations_,maxGenerations);  // create the offspring population
            union_ = population_.union(offspringPopulation_);
            SolutionSet[] sets = getParetoFronts();
            SolutionSet firstFront = sets[0];   // the first non-dominated front
            SolutionSet stPopulation = sets[1]; // the population used in theta-non-dominated ranking
            updateIdealPoint(firstFront);  // update the ideal point
            if (normalize_) {
                updateNadirPoint(firstFront);  // update the nadir point
                normalizePopulation(stPopulation);  // normalize the population using ideal point and nadir point
            }
            getNextPopulation(stPopulation);  // select the next population using theta-non-dominated ranking
            if ((generations_ )% interval == 0) {
                ranking = new NondominatedRanking(population_);
                igd.addIGDItem(ranking.getSubfront(0));
            }
            generations_++;

        }
        ranking = new NondominatedRanking(population_);
        String classNanme=getAlgName()+"/";
        String solutionPath= Environment.DEFAULT_PF_OBJECTIVE_SOLUTION+classNanme;
        ranking.getSubfront(0).printObjectivesToFile(solutionPath+getOutPutName("PFs",run));
        String igdPath=Environment.DEFAULT_IDG_ROOT+classNanme;
        igd.out(igdPath,getOutPutName(Environment.DEFAULT_PERFORMANCE_NAME,run));
        //igd.empty();
        return ranking.getSubfront(0);
    }
    public void init(String pfRoot,String pfSuffix,int curRun) {
        // maximum number of generations
        String pfName;
        Matrix pf;
        run=curRun;
        generations_ = 1;  //first generation
		/* set parameters */
        maxGenerations = ((Integer) this.getInputParameter("maxGenerations")).intValue();
        theta_ =  ((Double)this.getInputParameter("theta")).doubleValue();
        div1_ = ((Integer) this.getInputParameter("div1")).intValue();
        div2_ = ((Integer) this.getInputParameter("div2")).intValue();
        normalize_ = ((Boolean) this.getInputParameter("normalize")).booleanValue();
		/* generate two-layer weight vectors */
        VectorGenerator vg = new TwoLevelWeightVectorGenerator(div1_, div2_,
                problem_.getNumberOfObjectives());
        lambda_ = vg.getVectors();
		/*the population size is the same with the number of weight vectors*/
        populationSize_ = vg.getVectors().length;
        pfName=combine(pfRoot,problem_.getName(),"(", String.valueOf(problem_.getNumberOfObjectives()),
                ")",pfSuffix);
        //cache pareto front for 20 independent experiments
        if((pf=paretoFrontCache.get(pfName))==null){
            pf= ReadMatrix.readMatrix(pfName,problem_.getNumberOfObjectives());
            Configuration.logger_.info(pfName+" cache not exist,reading");
            if(paretoFrontCache.get(pfName)==null) {
                paretoFrontCache.put(pfName,pf);
                Configuration.logger_.info(pfName+" cache not exist,adding ");
            }
        }
        //Matrix PF= ReadMatrix.readMatrix(pfName,problem_.getNumberOfObjectives());
        igd=new IGD(pf,populationSize_,maxGenerations,interval);
        if(problem_.getName().startsWith("WFG")||problem_.getName().startsWith("SDTLZ")){
            igd.setToNormalize(true);
            igd.setProblemName(problem_.getName());
            igd.setM(problem_.getNumberOfObjectives());
        }
        initOperators();
    }

    public abstract void initOperators();
    public void initExtremePoints() {
        int obj = problem_.getNumberOfObjectives();
        extremePoints_ = new double[obj][obj];
        for (int i = 0; i < obj; i++){
            for (int j = 0; j < obj; j++){
                extremePoints_[i][j] = 1.0e+30;
            }
        }
    }
    void getNextPopulation(SolutionSet pop){
        Ranking ranking = new ThetaRanking(pop, lambda_, zideal_,
                theta_, normalize_);
        int remain = populationSize_;
        int index = 0;
        SolutionSet front;
        population_.clear();
        front = ranking.getSubfront(index);
        while ((remain > 0) && (remain >= front.size())) {
            for (int k = 0; k < front.size(); k++) {
                population_.add(front.get(k));
            } // for
            // Decrement remain
            remain = remain - front.size();
            // Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        } // while
        if (remain > 0) { // front contains individuals to insert
            int[] perm = new Permutation().intPermutation(front.size());
            for (int k = 0; k < remain; k++) {
                population_.add(front.get(perm[k]));
            } // for
            remain = 0;
        } // if
    }

    SolutionSet[] getParetoFronts() {
        SolutionSet[] sets = new SolutionSet[2];
        Ranking ranking = new NondominatedRanking(union_);
        int remain = populationSize_;
        int index = 0;
        SolutionSet front;
        SolutionSet mgPopulation = new SolutionSet();
        front = ranking.getSubfront(index);
        sets[0] = front;
        while ((remain > 0) && (remain >= front.size())) {
            for (int k = 0; k < front.size(); k++) {
                mgPopulation.add(front.get(k));
            }
            // Decrement remain
            remain = remain - front.size();
            // Obtain the next front
            index++;
            if (remain > 0) {
                front = ranking.getSubfront(index);
            } // if
        }
        if (remain > 0) { // front contains individuals to insert
            for (int k = 0; k < front.size(); k++) {
                mgPopulation.add(front.get(k));
            }
        }
        sets[1] = mgPopulation;
        return sets;
    }

    void initPopulation() throws JMException, ClassNotFoundException {
        population_= new SolutionSet(populationSize_);
        for (int i = 0; i < populationSize_; i++){
            Solution newSolution = new Solution(problem_);
            problem_.evaluate(newSolution);
            problem_.evaluateConstraints(newSolution);
            population_.add(newSolution);
        }
    }
    void copyObjectiveValues(double[] array, Solution individual) {
        for (int i = 0; i < individual.numberOfObjectives(); i++) {
            array[i] = individual.getObjective(i);
        }
    }


    double asfFunction(Solution sol, int j) {
        double max = Double.MIN_VALUE;
        double epsilon = 1.0E-6;

        int obj = problem_.getNumberOfObjectives();

        for (int i = 0; i < obj; i++) {

            double val = Math.abs((sol.getObjective(i) - zideal_[i])
                    / (znadir_[i] - zideal_[i]));

            if (j != i)
                val = val / epsilon;

            if (val > max)
                max = val;
        }

        return max;
    }

    double asfFunction(double[] ref, int j) {
        double max = Double.MIN_VALUE;
        double epsilon = 1.0E-6;

        int obj = problem_.getNumberOfObjectives();

        for (int i = 0; i < obj; i++) {

            double val = Math.abs((ref[i] - zideal_[i])
                    / (znadir_[i] - zideal_[i]));
            if (j != i)
                val = val / epsilon;
            if (val > max)
                max = val;
        }
        return max;
    }

    void initIdealPoint() {
        int obj = problem_.getNumberOfObjectives();
        zideal_ = new double[obj];
        for (int j = 0; j < obj; j++) {
            zideal_[j] = Double.MAX_VALUE;

            for (int i = 0; i < population_.size(); i++) {
                if (population_.get(i).getObjective(j) < zideal_[j])
                    zideal_[j] = population_.get(i).getObjective(j);
            }
        }
    }


    void updateIdealPoint(SolutionSet pop){
        for (int j = 0; j < problem_.getNumberOfObjectives(); j++) {
            for (int i = 0; i < pop.size(); i++) {
                if (pop.get(i).getObjective(j) < zideal_[j])
                    zideal_[j] = pop.get(i).getObjective(j);
            }
        }
    }

    void initNadirPoint() {
        int obj = problem_.getNumberOfObjectives();
        znadir_ = new double[obj];
        for (int j = 0; j < obj; j++) {
            znadir_[j] = Double.MIN_VALUE;

            for (int i = 0; i < population_.size(); i++) {
                if (population_.get(i).getObjective(j) > znadir_[j])
                    znadir_[j] = population_.get(i).getObjective(j);
            }
        }
    }
    //first front pop
    void updateNadirPoint(SolutionSet pop){

        updateExtremePoints(pop);


        int obj = problem_.getNumberOfObjectives();
        double[][] temp = new double[obj][obj];

        for (int i = 0; i < obj; i++) {
            for (int j = 0; j < obj; j++) {
                double val = extremePoints_[i][j] - zideal_[j];
                temp[i][j] = val;
            }
        }

        Matrix EX = new Matrix(temp);

        boolean sucess = true;

        if (EX.rank() == EX.getRowDimension()) {
            double[] u = new double[obj];
            for (int j = 0; j < obj; j++)
                u[j] = 1;

            Matrix UM = new Matrix(u, obj);

            Matrix AL = EX.inverse().times(UM);

            int j = 0;
            for (j = 0; j < obj; j++) {

                double aj = 1.0 / AL.get(j, 0) + zideal_[j];


                if ((aj > zideal_[j]) && (!Double.isInfinite(aj)) && (!Double.isNaN(aj)))
                    znadir_[j] = aj;
                else {
                    sucess = false;
                    break;
                }
            }
        }
        else
            sucess = false;


        if (!sucess){
            double[] zmax = computeMaxPoint(pop);
            for (int j = 0; j < obj; j++) {
                znadir_[j] = zmax[j];
            }
        }
    }
    public void updateExtremePoints(SolutionSet pop){
        for (int i = 0; i < pop.size(); i++)
            updateExtremePoints(pop.get(i));
    }

    public void updateExtremePoints(Solution individual){
        int obj = problem_.getNumberOfObjectives();
        for (int i = 0; i < obj; i++){
            double asf1 = asfFunction(individual, i);
            double asf2 = asfFunction(extremePoints_[i], i);

            if (asf1 < asf2){
                copyObjectiveValues(extremePoints_[i], individual);
            }
        }
    }


    double[] computeMaxPoint(SolutionSet pop){
        int obj = problem_.getNumberOfObjectives();
        double zmax[] = new double[obj];
        for (int j = 0; j < obj; j++) {
            zmax[j] = Double.MIN_VALUE;

            for (int i = 0; i < pop.size(); i++) {
                if (pop.get(i).getObjective(j) > zmax[j])
                    zmax[j] = pop.get(i).getObjective(j);
            }
        }
        return zmax;
    }

    void normalizePopulation(SolutionSet pop) {

        int obj = problem_.getNumberOfObjectives();

        for (int i = 0; i < pop.size(); i++) {
            Solution sol = pop.get(i);

            for (int j = 0; j < obj; j++) {

                double val = (sol.getObjective(j) - zideal_[j])
                        / (znadir_[j] - zideal_[j]);

                sol.setNormalizedObjective(j, val);
            }
        }
    }
    public String getOutPutName(String metric,int nrun){

        return  metric+"_"+problem_.getName()+"("+problem_.getNumberOfObjectives()+")_"+"R"+nrun+".dat";
    }
    public String getAlgName(){
        if(algorithmName==null){
            String[] strs=getClass().getName().split("\\.");
            algorithmName=strs[strs.length-1];
        }
        return algorithmName;
    }
    public abstract void  createOffSpringPopulation(SolutionSet population,SolutionSet offSpring,Problem problem,int curGeneration,int maxGeneration);


}
