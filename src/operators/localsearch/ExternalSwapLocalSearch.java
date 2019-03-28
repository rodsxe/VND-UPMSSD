package operators.localsearch;

import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import model.MachineContainer;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;
import model.Job;

public class ExternalSwapLocalSearch implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	private boolean firstImprovement = false;
	
	
	public ExternalSwapLocalSearch(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	public ExternalSwapLocalSearch setFirstImprovement(boolean firstImprovement) {
		this.firstImprovement = firstImprovement;
		return this;
	}

	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		boolean foundNewBest = false;
		List<MachineContainer> machinesNaoExploradas = s.arrayToList(s.getMachines());
		
		int indexMachine = evaluator.getLSMachineIndex(rand, s, machinesNaoExploradas);
		if(indexMachine < 0)return false;
		
		MachineContainer machineTardiness = machinesNaoExploradas.remove(indexMachine);
				
		while(!foundNewBest && machinesNaoExploradas.size() > 0){
				
			indexMachine = rand.nextInt(machinesNaoExploradas.size());
			MachineContainer m2 = machinesNaoExploradas.remove(indexMachine);
 			
			float costM1 = evaluator.getObjectFunctionValue(s, machineTardiness);
			float costM2 =  evaluator.getObjectFunctionValue(s, m2);
			
			Pair<Integer, Integer> indice = searchImprovement(s, machineTardiness, m2, rand, costM1, costM2);
			
			if (indice != null) {
				
				Job t1 = machineTardiness.getJob(indice.fst);
				Job t2 = m2.getJob(indice.snd);
				machineTardiness.replaceJob(indice.fst, t2);
				m2.replaceJob(indice.snd, t1);
				foundNewBest = true;
				
			}
			
		}
		
		return foundNewBest;
		
	}
	
	private Pair<Integer, Integer> searchImprovement(Solution solution, MachineContainer m1, MachineContainer m2, Random rand, float costM1, float costM2) {
		
		boolean stopping = false;
		
		float actualCost = evaluator.getLSCost(costM1, costM2);//costM1 + costM2;
		
		Pair<Integer, Integer> indice = null;
		Job jobM1, jobM2;
		
		for (int i = 0; i < m1.getJobs().size() && !stopping; i++) {
			
			jobM1 = m1.getJob(i);
			
			for (int j = 0; j < m2.getJobs().size() && !stopping; j++) {
				
				jobM2 = m2.getJob(j);
				m1.replaceJob(i, jobM2);
				m2.replaceJob(j, jobM1);
				
				costM1 = evaluator.getObjectFunctionValue(solution, m1);
				costM2 = evaluator.getObjectFunctionValue(solution, m2);				
				
				if (evaluator.getLSCost(costM1, costM2) < actualCost) {
					
					actualCost = evaluator.getLSCost(costM1, costM2);
					indice = new Pair<Integer, Integer>(i, j);
					if (firstImprovement) stopping = true;
					
				}
				
				m1.replaceJob(i, jobM1);
				m2.replaceJob(j, jobM2);
							
			}
			
		}
		
		return indice;
		
	}	
	
	
	
}
