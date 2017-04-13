package jmetal.metaheuristics;


import jmetal.core.Problem;
import jmetal.problems.ProblemFactory;
import jmetal.util.JMException;

public class Config {

	public static int[] setDivs(int obj) {

		int divs[] = new int[2];

		if (obj == 3) {
			divs[0] = 12;
			divs[1] = 0;
		} else if (obj == 5) {
			divs[0] = 6;
			divs[1] = 0;
		} else if (obj == 8) {
			divs[0] = 3;
			divs[1] = 2;
		} else if (obj == 10) {
			divs[0] = 3;
			divs[1] = 2;
		} else if (obj == 15) {
			divs[0] = 2;
			divs[1] = 1;
		} else {
			divs[0] = 3;
			divs[1] = 0;
		}
		return divs;
	}

	public static Problem setProblem(String name, int obj) throws JMException {
		Problem problem = null;
		if (name.equals("DTLZ1") || name.equals("SDTLZ1")) {
			Object[] params = { "Real", obj + 4, obj };
			problem = (new ProblemFactory()).getProblem(name, params);
		} else if (name.equals("DTLZ7")) {
			Object[] params = { "Real", obj + 19, obj };
			problem = (new ProblemFactory()).getProblem(name, params);
		} else if (name.startsWith("DTLZ") || name.equals("SDTLZ2")) {
			Object[] params = { "Real", obj + 9, obj };
			problem = (new ProblemFactory()).getProblem(name, params);
		} else if (name.startsWith("WFG")) {
			Object[] params = { "Real", obj - 1, 25 - obj, obj };
			problem = (new ProblemFactory()).getProblem(name, params);
		} else {
			System.out.println("Error: function type " + name + " invalid");
			System.exit(-1);
		}
		return problem;
	}

	public static int setMaxGenerations(String name, int obj) {
		if (name.equals("DTLZ1") || name.equals("SDTLZ1")) {
			if (obj == 3)
				return 400;
			else if (obj == 5)
				return 600;
			else if (obj == 8)
				return 750;
			else if (obj == 10)
				return 1000;
			else if (obj == 15)
				return 1500;
			else
				return 1500;
		} else if (name.equals("DTLZ2") || name.equals("SDTLZ2")) {
			if (obj == 3)
				return 250;
			else if (obj == 5)
				return 350;
			else if (obj == 8)
				return 500;
			else if (obj == 10)
				return 750;
			else if (obj == 15)
				return 1000;
			else
				return 1000;
		} else if (name.equals("DTLZ3")) {
			if (obj == 3)
				return 1000;
			else if (obj == 5)
				return 1000;
			else if (obj == 8)
				return 1000;
			else if (obj == 10)
				return 1500;
			else if (obj == 15)
				return 2000;
			else
				return 1000;
		} else if (name.equals("DTLZ4")) {
			if (obj == 3)
				return 600;
			else if (obj == 5)
				return 1000;
			else if (obj == 8)
				return 1250;
			else if (obj == 10)
				return 2000;
			else if (obj == 15)
				return 3000;
			else
				return 1000;
		} else if (name.startsWith("WFG")) {
			if (obj == 3)
				return 400;
			else if (obj == 5)
				return 750;
			else if (obj == 8)
				return 1500;
			else if (obj == 10)
				return 2000;
			else if (obj == 15)
				return 3000;
			else
				return 2000;
		} else
			return 1000;

	}
	
}
