package jmetal.util;

import Jama.Matrix;
import jmetal.core.Solution;
import jmetal.core.SolutionSet;

import java.util.ArrayList;
import java.util.List;

public class ANiching {
	SolutionSet population;
	SolutionSet lastFront;
	SolutionSet mgPopulation;

	SolutionSet union;

	int obj;
	int remain;

	boolean normalization;

	double[][] lambda;

	double[] zideal;

	double[] zmax;

	double[][] extremePoints;

	double[] intercepts;
	int theta_=5;                     //default theta is 5
	double inf=1E6;

	public ANiching(SolutionSet population, SolutionSet lastFront, int theta,
                    double[][] lambda, int remain, boolean normalization) {

		this.population = population;
		this.lastFront = lastFront;

		this.remain = remain;
		this.lambda = lambda;
        this.theta_=theta;
		this.normalization = normalization;

		this.mgPopulation = population.union(lastFront);

		if (population.size() > 0)
			this.obj = population.get(0).numberOfObjectives();
		else
			this.obj = lastFront.get(0).numberOfObjectives();
	}

	public void execute() {
		SolutionSet pop;

		if(population.size()>0){
			pop=population;
		}else pop=mgPopulation;
		computeIdealPoint(pop);
		
		if (normalization){
			computeMaxPoint(pop);       //轴上最大值
			computeExtremePoints(pop);  //
			computeIntercepts();
			normalizePopulation();
		}
		
		associate();
		assignment();
	}

	void computeIdealPoint(SolutionSet pop) {
		zideal = new double[obj];

		for (int j = 0; j < obj; j++) {
			zideal[j] = Double.MAX_VALUE;

			for (int i = 0; i < pop.size(); i++) {
				if (pop.get(i).getObjective(j) < zideal[j])
					zideal[j] = pop.get(i).getObjective(j);
			}
		}

	}

	void computeMaxPoint(SolutionSet pop) {
		zmax = new double[obj];

		for (int j = 0; j < obj; j++) {
			zmax[j] = Double.MIN_VALUE;

			for (int i = 0; i < pop.size(); i++) {
				if (pop.get(i).getObjective(j) > zmax[j])
					zmax[j] = pop.get(i).getObjective(j);
			}
		}
	}

	void computeExtremePoints(SolutionSet pop) {
		extremePoints = new double[obj][obj];

		for (int j = 0; j < obj; j++) {
			int index = -1;
			double min = Double.MAX_VALUE;

			for (int i = 0; i < pop.size(); i++) {
				double asfValue = asfFunction(pop.get(i), j);
				if (asfValue < min) {
					min = asfValue;
					index = i;
				}
			}

			for (int k = 0; k < obj; k++)
				extremePoints[j][k] = pop.get(index).getObjective(k);
		}
	}

	void computeIntercepts() {

		intercepts = new double[obj];

		double[][] temp = new double[obj][obj];

		for (int i = 0; i < obj; i++) {
			for (int j = 0; j < obj; j++) {
				double val = extremePoints[i][j] - zideal[j];
				temp[i][j] = val;
			}
		}

		Matrix EX = new Matrix(temp);

		if (EX.rank() == EX.getRowDimension()) {
			double[] u = new double[obj];
			for (int j = 0; j < obj; j++)
				u[j] = 1;

			Matrix UM = new Matrix(u, obj);

			Matrix AL = EX.inverse().times(UM);

			int j = 0;
			for (j = 0; j < obj; j++) {

				double aj = 1.0 / AL.get(j, 0) + zideal[j];

				if ((aj > zideal[j]) && (!Double.isInfinite(aj)) && (!Double.isNaN(aj)))
					intercepts[j] = aj;
				else
					break;
			}
			if (j != obj) {
				for (int k = 0; k < obj; k++)
					intercepts[k] = zmax[k];
			}

		} else {
			for (int k = 0; k < obj; k++)
				intercepts[k] = zmax[k];
		}

	}

	void normalizePopulation() {
		for (int i = 0; i < mgPopulation.size(); i++) {
			Solution sol = mgPopulation.get(i);

			for (int j = 0; j < obj; j++) {

				double val = (sol.getObjective(j) - zideal[j])
						/ (intercepts[j] - zideal[j]);

				sol.setNormalizedObjective(j, val);
			}
		}
	}

	public void associate() {

		for (int k = 0; k < mgPopulation.size(); k++) {

			Solution sol = mgPopulation.get(k);

			double[] dists = calVDistance(sol, lambda[0]);
			double d1=dists[0];
			double d2=dists[1];
			int index = 0;

			for (int j = 1; j < lambda.length; j++) {
				   dists = calVDistance(sol, lambda[j]);
				if (dists[1] < d2) {
					d1 = dists[0];
					d2=dists[1];
					index = j;
				}
			}
			sol.setClusterID(index);
			//sol.setVDistance(min);
			//sol.setFitness(d1);              //同一参考点距离理想点越近越好
			setPBIFitness(sol,index,d1,d2);
			//setInvertLambdaFitness(sol,index,d1,d2);
		}

	}

	public void assignment() {
		int[] ro = new int[lambda.length];
		boolean[] flag = new boolean[lambda.length];

		for (int k = 0; k < population.size(); k++) {
			ro[population.get(k).getClusterID()]++;
		}

		int num = 0;

		while (num < remain) {
			int[] perm = new Permutation().intPermutation(ro.length);

			int min = Integer.MAX_VALUE;
			int id = -1;

			for (int i = 0; i < perm.length; i++) {
				if ((!flag[perm[i]]) && (ro[perm[i]] < min)) {                                                          //有多个参考点都有最小个体数时，随机选取一个
					min = ro[perm[i]];
					id = perm[i];
				}
			}

			List<Integer> list = new ArrayList<Integer>();

			for (int k = 0; k < lastFront.size(); k++) {
				if (lastFront.get(k).getClusterID() == id)
					list.add(k);
			}

			if (list.size() != 0) {
				int index = 0;
//				if (ro[id] == 0) {
					double minDist = Double.MAX_VALUE;

					for (int j = 0; j < list.size(); j++) {
						if (lastFront.get(list.get(j)).getFitness() < minDist) {
							minDist = lastFront.get(list.get(j)).getFitness();
							index = j;
						}
					}
//				} else {
//					index = PseudoRandom.randInt(0, list.size() - 1);
//				}

				population.add(lastFront.get(list.get(index)));
				ro[id]++;           //参考点niche count+1

				lastFront.remove(list.get(index));
				num++;
			} else {
				flag[id] = true;
			}

		}
	}

	double asfFunction(Solution sol, int j) {
		double max = Double.MIN_VALUE;
		double epsilon = 1.0E-6;

		for (int i = 0; i < obj; i++) {

			double val = Math.abs(sol.getObjective(i) - zideal[i]);

			if (j != i)
				val = val / epsilon;

			if (val > max)
				max = val;
		}

		return max;
	}

	
	
	
	public double[] calVDistance(Solution sol, double[] ref){
		if (normalization)
			return calNormlizedVDistance(sol, ref);
		else
			return calUnNormalizedVDistance(sol, ref);
	}
	
	
	public double[] calNormlizedVDistance(Solution sol, double[] ref) {
		double[] d = new double[2];
		double ip = 0;
		double refLenSQ = 0;

		for (int j = 0; j < obj; j++) {

			ip += sol.getNormalizedObjective(j) * ref[j];
			refLenSQ += (ref[j] * ref[j]);
		}
		refLenSQ = Math.sqrt(refLenSQ);

		double d1 = Math.abs(ip) / refLenSQ;

		double d2 = 0;
		for (int i = 0; i < sol.numberOfObjectives(); i++) {
			d2 += (sol.getNormalizedObjective(i) - d1 * (ref[i] / refLenSQ))
					* (sol.getNormalizedObjective(i) - d1 * (ref[i] / refLenSQ));
		}
		d2 = Math.sqrt(d2);
         d[0]=d1;
		 d[1]=d2;
		return d;
	}
	
	
	double[] calUnNormalizedVDistance(Solution sol, double[] ref){
		double[] d = new double[2];
		double d1, d2, nl;

		d1 = d2 = nl = 0.0;

		for (int i = 0; i < sol.numberOfObjectives(); i++) {
			d1 += (sol.getObjective(i) - zideal[i]) * ref[i];
			nl += (ref[i] * ref[i]);
		}
		nl = Math.sqrt(nl);
		d1 = Math.abs(d1) / nl;
		
	
		d2 =0;
		for (int i = 0; i < sol.numberOfObjectives(); i++) {
			
			d2 += ((sol.getObjective(i) - zideal[i]) - d1
					* (ref[i] / nl)) * ((sol.getObjective(i) - zideal[i]) - d1
							* (ref[i] / nl));
		}
		d2 = Math.sqrt(d2);

		d[0]=d1;
		d[1]=d2;
		return d;
	}

	void setInvertLambdaFitness(Solution sol, int index,double d1,double d2){
		double maxFitness=0;

			for (int j = 0; j < obj; j++) {
				double fit;
				if(lambda[index][j]!=0)
					fit = sol.getNormalizedObjective(j) *lambda[index][j];
				else fit=sol.getNormalizedObjective(j)*0.00001;
				if (fit > maxFitness)
					maxFitness = fit;
			}
			sol.setFitness(maxFitness);

	}
	void setPBIFitness(Solution sol, int index, double d1, double d2){
		if (this.normalization) {
			if (!isObjAxis(index))
				sol.setFitness(d1 + theta_ * d2);
			else
				sol.setFitness(d1 + inf * d2);
		}
		else
			sol.setFitness(d1 + theta_ * d2);
	}
	boolean isObjAxis(int index){
		for (int j = 0; j < obj; j++){
			if (lambda[index][j] != 0 && lambda[index][j] != 1)
				return false;
		}
		return true;
	}
}
