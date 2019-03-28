package operators.localsearch;

import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import model.MachineContainer;
import model.Solution;
import operators.function.evaluator.ObjectFunctionEvaluator;
import model.Job;

public class ExternalInsertionLocalSearch implements LocalSearch {
	
	private ObjectFunctionEvaluator evaluator;
	
	private boolean firstImprovement = false;
	
	public ExternalInsertionLocalSearch setFirstImprovement(boolean firstImprovement) {
		this.firstImprovement = firstImprovement;
		return this;
	}

	public ExternalInsertionLocalSearch(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
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
			//indexMachine = (indexMachine == 0)? 1 : indexMachine;
			MachineContainer m2 = machinesNaoExploradas.remove(indexMachine);
			
			float costM1 = evaluator.getObjectFunctionValue(s, machineTardiness);
			float costM2 =  evaluator.getObjectFunctionValue(s, m2);
			
			//Pair<Integer, Integer> indice = getRandomBestImprovementInsercaoExterna(machineTardiness, m2, rand);
 			Pair<Integer, Integer> indice = searchImprovement(s, machineTardiness, m2, rand, costM1, costM2);
			
 			if (indice != null) {
 				
				Job t1 = machineTardiness.getJob(indice.fst);
				machineTardiness.removeJob(indice.fst);
				m2.addJob(t1, indice.snd);
				foundNewBest = true;
			
 			} 
			
		}
		
		return foundNewBest;
		
	}
	
	private Pair<Integer, Integer> searchImprovement(Solution solution, MachineContainer m1, MachineContainer m2, Random rand, float costM1, float costM2) {
		
		boolean stopping = false;
		
		float actualCost = evaluator.getLSCost(costM1, costM2);//Math.max(costM1, costM2);// costM1 + costM2;
						
		Pair<Integer, Integer> indice = null;
		
		Job jobM1; 
				
		for (int i = 0; i < m1.getJobs().size() && !stopping; i++) {
			
			jobM1 = m1.removeJob(i);
			costM1 = evaluator.getObjectFunctionValue(solution, m1);
						
			for (int j = 0; j <= m2.getJobs().size() && !stopping; j++) {
				
				m2.addJob(jobM1, j);
				costM2 = evaluator.getObjectFunctionValue(solution, m2);
				
				if (evaluator.getLSCost(costM1, costM2) < actualCost) {
					
					actualCost = evaluator.getLSCost(costM1, costM2);
					indice = new Pair<Integer, Integer>(i, j);
					
					if (firstImprovement) stopping = true;
					
				}
								
				m2.removeJob(j);
							
			}
			
			m1.addJob(jobM1, i);
			
		}
		
		return indice;
		
	}
	
		
	
}
