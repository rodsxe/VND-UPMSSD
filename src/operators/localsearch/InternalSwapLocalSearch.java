package operators.localsearch;

import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import model.MachineContainer;
import model.Solution;
import model.Job;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class InternalSwapLocalSearch implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	
	private boolean firstImprovement = false;
	
	public InternalSwapLocalSearch(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	public InternalSwapLocalSearch setFirstImprovement(boolean firstImprovement) {
		this.firstImprovement = firstImprovement;
		return this;
	}
	
	@Override
	public boolean run(Random random, Solution s, long totalTime, long initialTime) {
		
		boolean stopping = false;
		
		List<MachineContainer> machinesNaoExploradas = s.arrayToList(s.getMachines());
		
		int indexMachine = evaluator.getLSMachineIndex(random, s, machinesNaoExploradas);
		if(indexMachine < 0)return false;
		
		MachineContainer machine = machinesNaoExploradas.get(indexMachine);
		
		int numTarefasAlocadas = machine.getJobs().size();
		
		int bestI = -1;
		int bestJ = -1;
		float newCost, minCost = evaluator.getObjectFunctionValue(s, machine);
				
		for (int i = 0; i < numTarefasAlocadas && !stopping; i++) {
			
			for (int j = i + 1; j < numTarefasAlocadas && !stopping; j++) {
				
				machine.swapJobs(i, j);
				newCost = evaluator.getObjectFunctionValue(s, machine);
				
				if(newCost < minCost){
					
					minCost = newCost;
					bestI = i;
					bestJ = j;
					
					if (firstImprovement) stopping = true;
					
				}
				
				machine.swapJobs(i, j);
										
			}
			
		}
		
		if(bestI != -1){
			
			machine.swapJobs(bestI, bestJ);
			return true;
		
		}
		
		return false;

	}
	
}
