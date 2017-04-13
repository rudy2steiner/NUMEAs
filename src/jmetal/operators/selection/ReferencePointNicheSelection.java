package jmetal.operators.selection;

import jmetal.core.Solution;
import jmetal.core.SolutionSet;
import jmetal.encodings.variable.Int;
import jmetal.util.JMException;
import jmetal.util.NicheSelection;
import jmetal.util.PseudoRandom;
import jmetal.util.SortUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Rudy Steiner on 2016/10/2.
 */
public class ReferencePointNicheSelection extends  Selection {

    public ReferencePointNicheSelection(HashMap<String, Object> parameters) {
        super(parameters);
    }

    @Override
    public Object execute(Object object) throws JMException {
        NicheSelection  ns=(NicheSelection)object;
        double rand=0.50;
        SolutionSet population=ns.getPopulation_();
        int pos1, pos2,pos3,pos4;
        pos1 = PseudoRandom.randInt(0,population.size()-1);
//        if (PseudoRandom.randDouble()<=rand){
//            pos2 = PseudoRandom.randInt(0,population.size()-1);
//            while ((pos1 == pos2) && (population.size()>1)) {
//                pos2 = PseudoRandom.randInt(0,population.size()-1);
//            }
//            //System.out.println("random select");
//        }else {
            //System.out.println("random rp");
            int[][] neighbours = ns.getNeighbours_();
            int[] neighbour;
            List matePool = new ArrayList<Integer>();

            pos2 = pos1;
            int rpIndex = population.get(pos1).getClusterID();
            neighbour = neighbours[rpIndex];
            for (int i = 0; i < population.size(); i++) {
                boolean isContain = isContain(neighbour, population.get(i).getClusterID());
                if (isContain&&i!=pos1) matePool.add(new Integer(i));
            }
//            if (!matePool.isEmpty() && matePool.size() > 1) {
//                while (pos2 == pos1) {
//                    int select = PseudoRandom.randInt(0, matePool.size() - 1);
//                    Integer ind = (Integer) matePool.get(select);
//                    pos2 = ind.intValue();
//                }
//            } else pos2 = PseudoRandom.randInt(0, population.size() - 1);
        if(!matePool.isEmpty()) {
            if (matePool.size() >= 4) {
                int matePoolSize = matePool.size();
                double[] dist = new double[matePoolSize];
                int[] index = new int[matePoolSize];
                for (int i = 0; i < matePoolSize; i++) {
                    Integer ind = (Integer) matePool.get(i);
                    int pos = ind.intValue();
                    dist[i] = SortUtil.distBetweenNormalizedSolution(population.get(pos1), population.get(pos));
                    index[i] = i;
                }
                SortUtil.minFastSort(dist, index, matePoolSize, 3);                                                     //距离最小的个体
                Integer ind = (Integer) matePool.get(index[0]);
                pos2 = ind.intValue();
                ind = (Integer) matePool.get(index[1]);
                pos3 = ind.intValue();
                ind = (Integer) matePool.get(index[2]);
                pos4 = ind.intValue();
            } else{
                do {
                    pos2 = (int)(PseudoRandom.randInt(0,population.size()-1));
                } while( pos2==pos1 );
                do {
                    pos3 = (int)(PseudoRandom.randInt(0,population.size()-1));
                } while( pos3==pos2 ||  pos3==pos1);
                do {
                    pos4 = (int)(PseudoRandom.randInt(0,population.size()-1));
                } while( pos4==pos3 || pos4==pos2 ||pos4==pos1);
            }
        }else{
            do {
                pos2 = (int)(PseudoRandom.randInt(0,population.size()-1));
            } while( pos2==pos1 );
            do {
                pos3 = (int)(PseudoRandom.randInt(0,population.size()-1));
            } while( pos3==pos2 ||  pos3==pos1);
            do {
                pos4 = (int)(PseudoRandom.randInt(0,population.size()-1));
            } while( pos4==pos3 || pos4==pos2 ||pos4==pos1);
        }
       // }
        Object[] objs=new Object[2];


        Solution[] parents = new Solution[3];
        parents[0] = population.get(pos2);
        parents[1] = population.get(pos3);
        parents[2] = population.get(pos4);
        objs[0]=population.get(pos1);
        objs[1]=parents;
        return objs;
    }
    public static boolean isContain(int[] pool,int rp){
        for(int i=0;i<pool.length;i++){
            if (pool[i]==rp) return true;
        }
        return false;
    }
}
