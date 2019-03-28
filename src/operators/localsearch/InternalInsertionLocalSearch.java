package operators.localsearch;

import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import model.MachineContainer;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;
import model.Job;

public class InternalInsertionLocalSearch implements LocalSearch {

	private ObjectFunctionEvaluator evaluator;
	
	private boolean firstImprovement = false;
	
	public InternalInsertionLocalSearch setFirstImprovement(boolean firstImprovement) {
		this.firstImprovement = firstImprovement;
		return this;
	}
	
	public InternalInsertionLocalSearch(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		//if (rand.nextFloat() > 1/s.getNumberOfMachines()) return this.buscaLocalInsercaoExterna.run(rand, s, bestSol);
		
		
		List<MachineContainer> machinesNaoExploradas = s.arrayToList(s.getMachines());
		//MachineContainer machine = s.getMachineWithCost(rand, machinesNaoExploradas);
		
		int indexMachine = evaluator.getLSMachineIndex(rand, s, machinesNaoExploradas);
		if(indexMachine < 0)return false;
		
		MachineContainer machine = machinesNaoExploradas.get(indexMachine);
		
		int numTarefasAlocadas = machine.getJobs().size();
		int melhorI = -1;
		int melhorP = -1;
		float newCost, actualCost = evaluator.getObjectFunctionValue(s, machine);
		
		boolean stopping = false;
		
		for (int i = 0; i < numTarefasAlocadas && !stopping; i++) {
			
			Job t1 =machine.removeJob(i);
			
			for (int j = 0; j < numTarefasAlocadas && !stopping; j++) {
				
				//if (rand.nextFloat() < 0.5) {
					
					machine.addJob(t1, j);
					
					newCost = evaluator.getObjectFunctionValue(s, machine);
					if (newCost < actualCost) {
						
						actualCost = newCost;
						melhorI = i;
						melhorP = j;
						
						if(firstImprovement) stopping = true;
						
					}
					
					machine.removeJob(j);
					
				//}
				
			}
			
			machine.addJob(t1, i);
			
		}
		
		
		if (melhorI != -1) {
			
			Job t = machine.getJob(melhorI);
			machine.removeJob(melhorI);
			machine.addJob(t, melhorP);
			return true;
		
		}
		
		return false;
		
	}
	
}
