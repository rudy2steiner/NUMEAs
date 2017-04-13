package jmetal.metaheuristics.NUMTheta;



import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import Jama.Matrix;
import jmetal.core.Algorithm;
import jmetal.core.Operator;
import jmetal.core.Problem;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.operators.mutation.Mutation;
import jmetal.operators.mutation.NonUniformMutation;
import jmetal.operators.selection.BinaryTournament3;
import jmetal.quality.IGD;
import jmetal.util.*;
import jmetal.util.comparators.CrowdingComparator;
import jmetal.util.comparators.DominanceComparator;
import jmetal.util.comparators.FitnessComparator;
import jmetal.util.ranking.NondominatedRanking;
import jmetal.util.ranking.Ranking;
import jmetal.util.ranking.ThetaRanking;
import jmetal.util.vector.NeighbourGenerator;
import jmetal.util.vector.TwoLevelWeightVectorGenerator;
import jmetal.util.vector.VectorGenerator;

public class NUMThetaDEA extends Algorithm {
	
	private int populationSize_;   // population size
	private SolutionSet population_;   // current population
	private SolutionSet offspringPopulation_;  // offspring population
	private SolutionSet union_;    // the union of current population and offspring population
	private int generations_;   // generations
	/* if only one layer is adopted, div2_=0 */
	private int div1_;  // divisions in the boundary layer
	private int div2_;  // divisions in the inside layer
	private double theta_;     // parameter theta
	private Operator crossover_; // crossover
	private Operator polyMutation_;   // mutation operator
	private Operator nonuniformMutation_;   // mutation operator
	private Operator cauchyMutation_;   // mutation operator
	private Operator binarySelection;
	private boolean normalize_;  // normalization or not
	private double[][] lambda_; // reference points
	private double[] zideal_;   // ideal point
	private double[] znadir_;   // nadir point
	private double[][] extremePoints_; // extreme points
	private int[][] neighbour_;
	private int nrun_=20;
	private String algorithmName;

	public NUMThetaDEA(Problem problem) {
		super(problem);
	} // NUMThetaDEA

	public SolutionSet execute() throws JMException, ClassNotFoundException {
		int maxGenerations;  // maximum number of generations
		generations_ = 0;
		/* set parameters */
		maxGenerations = ((Integer) this.getInputParameter("maxGenerations"))
				.intValue();
		theta_ =  ((Double)this.getInputParameter("theta")).doubleValue();
		div1_ = ((Integer) this.getInputParameter("div1")).intValue();
		div2_ = ((Integer) this.getInputParameter("div2")).intValue();
		normalize_ = ((Boolean) this.getInputParameter("normalize"))
				.booleanValue();
		/* generate two-layer weight vectors */
		VectorGenerator vg = new TwoLevelWeightVectorGenerator(div1_, div2_,
				problem_.getNumberOfObjectives());
		lambda_ = vg.getVectors();
		populationSize_ = vg.getVectors().length;
		Matrix PF= ReadMatrix.readMatrix("/resources/pf/"+problem_.getName()+"("+ problem_.getNumberOfObjectives()+").dat", problem_.getNumberOfObjectives());
		IGD igd=new IGD( PF,populationSize_,maxGenerations,10);
		if(problem_.getName().startsWith("WFG")||problem_.getName().startsWith("SDTLZ")){
			igd.setToNormalize(true);
			igd.setProblemName(problem_.getName());
			igd.setM(problem_.getNumberOfObjectives());
		}
		crossover_ = operators_.get("crossover"); // set the crossover operator
		polyMutation_ = operators_.get("PM");  // set the mutation operator
		nonuniformMutation_ = operators_.get("NUM");  // set the mutation operator
		cauchyMutation_ = operators_.get("CAUCHY");  // set the mutation operator
		binarySelection=operators_.get("BS");
		int run=0;
		System.out.println(getAlgName()+" running:"+problem_.getName()+",number of obj: "+problem_.getNumberOfObjectives()+",theta:"+theta_);
		while(run<nrun_) {
			System.out.println(getAlgName()+" run:" + run);
			initPopulation();   // initialize the population;
			initIdealPoint();  // initialize the ideal point
			initNadirPoint();    // initialize the nadir point
			initExtremePoints(); // initialize the extreme points
			Ranking ranking = new NondominatedRanking(population_);
			igd.addIGDItem(ranking.getSubfront(0));
			generations_ = 0;
			while (generations_ < maxGenerations) {
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
				if ((generations_ +1)% 10 == 0) {
					ranking = new NondominatedRanking(population_);
					igd.addIGDItem(ranking.getSubfront(0));
				}
				generations_++;
			}
			ranking = new NondominatedRanking(population_);
			String classNanme=getAlgName()+"/";
			String solutionPath="src/resources/pf_solution/"+classNanme;
			ranking.getSubfront(0).printObjectivesToFile(solutionPath+getOutPutName("PFs",run+1));
			String igdPath="src/resources/igd/"+classNanme;
			igd.out(igdPath,getOutPutName("IGD",run+1));
			igd.empty();
			run++;
		}
		Ranking ranking = new NondominatedRanking(population_);
		return ranking.getSubfront(0);
		
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
		SolutionSet front = null;
		population_.clear();

		// Obtain the next front
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
		//System.out.println(remain);
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
		SolutionSet front = null;
		SolutionSet mgPopulation = new SolutionSet();

		front = ranking.getSubfront(index);

		sets[0] = front;

		while ((remain > 0) && (remain >= front.size())) {

			for (int k = 0; k < front.size(); k++) {
				mgPopulation.add(front.get(k));
			} // for

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
		
		for (int i = 0; i < populationSize_; i++) {
			Solution newSolution = new Solution(problem_);

			problem_.evaluate(newSolution);
			problem_.evaluateConstraints(newSolution);
			population_.add(newSolution);
		}
	} 

	
	void createOffSpringPopulation(int maxGeneration) throws JMException {
		offspringPopulation_ = new SolutionSet(populationSize_);

		for (int i = 0; i < populationSize_; i++) 
			doCrossover(i,maxGeneration);
	}
	
	
	void doCrossover(int i,int maxGeneration) throws JMException{
		int r;
		do {
			r = PseudoRandom.randInt(0, populationSize_ - 1);
		} while (r == i);
		Solution[] parents = new Solution[2];
		parents[0] = population_.get(i);
		parents[1] = population_.get(r);
		Solution[] offSpring = (Solution[]) crossover_
				.execute(parents);

//		nonuniformMutation_.setParameter("currentIteration", generations_);
//		nonuniformMutation_.execute(offSpring[0]);
        cauchyMutation_.execute(offSpring[0]);
		problem_.evaluate(offSpring[0]);
		offspringPopulation_.add(offSpring[0]);
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
