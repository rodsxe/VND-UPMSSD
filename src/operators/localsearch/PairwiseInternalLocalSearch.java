package operators.localsearch;

import java.util.Random;

import model.Solution;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class PairwiseInternalLocalSearch implements LocalSearch {

	private ObjectFunctionEvaluator evaluator;
	
	public PairwiseInternalLocalSearch(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		MachineContainer[] machines = s.getMachines();
		
		for (MachineContainer machine : machines) {
			
			boolean improvement = true;
			int numTarefasAlocadas = machine.getJobs().size();
			float minCost = evaluator.getObjectFunctionValue(s, machine);
			
			while (improvement && minCost > 0) {
			
				int bestI = -1;
				int bestJ = -1;
				float newCost;
				boolean find = false;		
				
				for (int i = 0; i < numTarefasAlocadas - 1 && !find; i++) {
					
					for (int j = i + 1; j < numTarefasAlocadas && !find; j++) {
						//int j = i + 1;
						machine.swapJobs(i, j);
						newCost = evaluator.getObjectFunctionValue(s, machine);
						
						if(newCost < minCost){
							
							minCost = newCost;
							bestI = i;
							bestJ = j;
							find = true;
						}
						
						machine.swapJobs(i, j);
												
					}
					
				}
				
				if (bestI != -1) machine.swapJobs(bestI, bestJ); 
				else improvement = false;
								
			}
		
		}
		
		return false;
		
	}

}
