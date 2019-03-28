package operators.localsearch;

import java.util.Random;

import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class IGSSwapLocalSearch implements LocalSearch {

	private ObjectFunctionEvaluator evaluator;
	
	private int K;
	
	public IGSSwapLocalSearch(ObjectFunctionEvaluator evaluator, int K) {
		
		super();
		this.evaluator = evaluator;
		this.K = K;
		
	}
	
	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		
		boolean improvement = false;
		//if(true) return false; 
		for (int k = 0; k < K; k++) {
			
			Solution clone = s.clone();
			MachineContainer[] machines = clone.getMachines();
				
			int bestM = -1;
			int bestJ = -1;
			float newCost, minCost;
			Job jobM1, jobM2;
			
			MachineContainer m2;
			MachineContainer m1 = machines[rand.nextInt(machines.length)];
			
			while(m1.getJobs().size() == 0) m1 = machines[rand.nextInt(machines.length)];
			
			int i = rand.nextInt(m1.getJobs().size());
			jobM1 = m1.getJob(i);
					
			minCost = evaluator.getObjectFunctionValue(clone);
				
			for (int m = 0; m < machines.length; m++) {
				
				m2 = machines[m];
				
				for (int j = 0; j < m2.getJobs().size(); j++) {
					
					jobM2 = m2.getJob(j);
					m1.replaceJob(i, jobM2);
					m2.replaceJob(j, jobM1);
					
					newCost = evaluator.getObjectFunctionValue(clone) ;
					
					if(newCost < minCost){
						
						minCost = newCost;
						bestJ = j;
						bestM = m;
						
					}
					
					m1.replaceJob(i, jobM1);
					m2.replaceJob(j, jobM2);
				
				}
				
			}
			
			if (minCost < evaluator.getObjectFunctionValue(clone)) {
				
				m1.replaceJob(i, machines[bestM].getJob(bestJ));
				machines[bestM].replaceJob(bestJ, jobM1);
				s.setMachines(clone.getMachines());
				s.setObjectiveFunction();
				improvement = true;
				
			}
			
		}
			
		return improvement;
		
	}

}
