package jmetal.util;

import jmetal.core.SolutionSet;

/**
 * Created by Rudy Steiner on 2016/10/2.
 */
public class NicheSelection {
     private SolutionSet population_;
     private int[][]   neighbours_;
     private int generation;
     private int maxGen;

    public int getGeneration() {
        return generation;
    }

    public void setGeneration(int generation) {
        this.generation = generation;
    }

    public int getMaxGen() {
        return maxGen;
    }

    public void setMaxGen(int maxGen) {
        this.maxGen = maxGen;
    }

    public NicheSelection(SolutionSet pop, int[][] neighbours){
        this.population_=pop;
        this.neighbours_=neighbours;
    }

    public SolutionSet getPopulation_() {
        return population_;
    }

    public void setPopulation_(SolutionSet population_) {
        this.population_ = population_;
    }

    public int[][] getNeighbours_() {
        return neighbours_;
    }

    public void setNeighbours_(int[][] neighbours_) {
        this.neighbours_ = neighbours_;
    }
}
