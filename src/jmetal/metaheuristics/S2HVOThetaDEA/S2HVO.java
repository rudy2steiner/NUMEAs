package jmetal.metaheuristics.S2HVOThetaDEA;
import Jama.Matrix;
import jmetal.core.*;
import jmetal.metaheuristics.Environment;
import jmetal.quality.IGD;
import jmetal.util.*;
import jmetal.util.ranking.NondominatedRanking;
import jmetal.util.ranking.Ranking;
import jmetal.util.ranking.ThetaRanking;
import jmetal.util.vector.TwoLevelWeightVectorGenerator;
import jmetal.util.vector.VectorGenerator;
import java.util.ArrayList;
import java.util.List;

import static jmetal.util.StringUtils.combine;

/**
 *  create by rudy2steiner
 *  @email rudy_steiner@163.com
 *  implement 2SHVOThetaDEA  based on thetaDEA framework
 **/
public class S2HVO extends Algorithm {
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
	private Operator SBXCrossover_; // crossover
	private Operator DECrossover_; // crossover
	private Operator polyMutation_;   // mutation operator
	private Operator nonuniformMutation_;   // mutation operator
	private Operator binarySelection;
	private Operator DESelection_;
	private boolean normalize_;  // normalization or not
	private double[][] lambda_; // reference points
	private double[] zideal_;   // ideal point
	private double[] znadir_;   // nadir point
	private double[][] extremePoints_; // extreme points
	private int[][] neighbour_;
	int maxGenerations;
	int interval=DEFAULT_INTERVAL;
//	private int nrun_=20;
	//private int run;
	private String algorithmName;
	private IGD igd;
	private String pfRoot;
	private String pfSuffix;
	private int run;
	public S2HVO(Problem problem,String pfRoot,String pfSuffix,int curRun) {
		super(problem);
		this.pfRoot=pfRoot;
		this.pfSuffix=pfSuffix;
		this.run=curRun;
	} // S2HVO
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
				createOffSpringPopulation(maxGenerations);  // create the offspring population
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
    /**
     * @param pfRoot  root path of real pareto front
	 * @param pfSuffix suffix of real pareto front file
     **/
	public void init(String pfRoot,String pfSuffix,int curRun) {
		// maximum number of generations
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
		Matrix PF= ReadMatrix.readMatrix(combine(pfRoot,problem_.getName(),"(", String.valueOf(problem_.getNumberOfObjectives()),
				                         ")",pfSuffix),problem_.getNumberOfObjectives());
		igd=new IGD(PF,populationSize_,maxGenerations,interval);
		if(problem_.getName().startsWith("WFG")||problem_.getName().startsWith("SDTLZ")){
			igd.setToNormalize(true);
			igd.setProblemName(problem_.getName());
			igd.setM(problem_.getNumberOfObjectives());
		}
		SBXCrossover_ = operators_.get("SBX"); // set the crossover operator
		polyMutation_ = operators_.get("PM");  // set the mutation operator
		nonuniformMutation_ = operators_.get("NUMNSGAIII");  // set the mutation operator
		binarySelection=operators_.get("BS");
		DECrossover_ = operators_.get("DE");
		DESelection_ = operators_.get("DES");
	}
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

	
	void createOffSpringPopulation(int maxGeneration) throws JMException {
		offspringPopulation_ = new SolutionSet(populationSize_);

		for (int i = 0; i < populationSize_; i++) 
		{
			int current = i;
             if(generations_<maxGeneration/2) {
				 int k = PseudoRandom.randInt(1, 3);
				 if (k == 1) {
					 SBXCrossover(current, false);
				 } else if (k == 2) {
					 DEOperator(current, false);
				 } else {
					 NonUniformMutation(current);
				 }
			 }else{
				 int k = PseudoRandom.randInt(1,5);
				 if(k<=4) SBXCrossover(current, false);
				 else     DEOperator(current, false);
			 }
		}
	}
	public void HybridizeMutation(int current) throws JMException{

		int k = PseudoRandom.randInt(1,3);
		Solution sol = new Solution(population_.get(current));
		if(k==1) {
			  polyMutation_.execute(sol);
		}  else  {
			nonuniformMutation_.setParameter("currentIteration",generations_);
			nonuniformMutation_.execute(sol);
		}

		problem_.evaluate(sol);
		offspringPopulation_.add(sol);

	}
	public void PolynomialMutation(int current) throws JMException{

		Solution sol = new Solution(population_.get(current));
		polyMutation_.execute(sol);
		problem_.evaluate(sol);
		offspringPopulation_.add(sol);

	}
	public void NonUniformMutation(int current) throws JMException{

		Solution sol = new Solution(population_.get(current));
		nonuniformMutation_.setParameter("currentIteration",generations_);
		nonuniformMutation_.execute(sol);
		problem_.evaluate(sol);
		offspringPopulation_.add(sol);

	}
	public void SBXCrossover(int current,boolean mutation) throws JMException{
		int r;
		do {
			r = PseudoRandom.randInt(0, populationSize_ - 1);
		} while (r == current);

		Solution[] parents = new Solution[2];
		parents[0] = population_.get(current);
		parents[1] = population_.get(r);
		Solution[] offSprings = (Solution[]) SBXCrossover_.execute(parents);
		if(mutation) {
			nonuniformMutation_.setParameter("currentIteration",generations_);
			nonuniformMutation_.execute(offSprings[0]);
		}
		problem_.evaluate(offSprings[0]);
		offspringPopulation_.add(offSprings[0]);
	}

	public void DEOperator(int current,boolean mutation) throws  JMException{
		//DE
		Object[] parameters = new Object[2];
		parameters[0] = population_;
		parameters[1] = new Integer(current);
		Solution [] parents = (Solution[]) DESelection_.execute(parameters);
		parameters[0] = population_.get(current);
		parameters[1] = parents;
		Solution offSpring = (Solution) DECrossover_
				.execute(parameters);
		if(mutation) {
			nonuniformMutation_.setParameter("currentIteration",generations_);
			nonuniformMutation_.execute(offSpring);
		}
		problem_.evaluate(offSpring);
		offspringPopulation_.add(offSpring);
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


   public Solution neighbourhoodSelection(int r,int[][] neighbours){
   	   int pos1;
	   int[] neighbour;
	   int rpIndex = population_.get(r).getClusterID();
	   neighbour = neighbours[rpIndex];
	   List matePool = new ArrayList<Integer>();
	   for (int i = 0; i < population_.size(); i++) {
		   boolean isContain = isContain(neighbour, population_.get(i).getClusterID());
		   if (isContain&&i!=r) matePool.add(new Integer(i));
	   }

            if (!matePool.isEmpty() && matePool.size() > 1) {
                do{
                    int select = PseudoRandom.randInt(0, matePool.size() - 1);
                    Integer ind = (Integer) matePool.get(select);
                    pos1 = ind.intValue();
                }while(pos1==r);
            } else pos1 = PseudoRandom.randInt(0, population_.size() - 1);

   	return population_.get(pos1);
   }
	public static boolean isContain(int[] pool,int rp){
		for(int i=0;i<pool.length;i++){
			if (pool[i]==rp) return true;
		}
		return false;
	}
	public String getAlgName(){
		if(algorithmName==null){
			String[] strs=getClass().getName().split("\\.");
			algorithmName=strs[strs.length-1];
		}
		return algorithmName;
	}
}
