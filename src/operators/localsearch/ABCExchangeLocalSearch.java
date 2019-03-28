package operators.localsearch;

import java.util.Random;

import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class ABCExchangeLocalSearch implements LocalSearch {

	private ObjectFunctionEvaluator evaluator;
	
	public ABCExchangeLocalSearch(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	@Override
	public boolean run(Random rand, Solution s, long totalTime, long initialTime) {
		

		int M = evaluator.getSchedulingConfig().getNumberOfMachines(); 
		int N = evaluator.getSchedulingConfig().getNumberOfJobs();
		float actualCostM1, actualCostM2, newCost;
		MachineContainer machine1, machine2;
		Job job1, job2;
		boolean improvement = true;
		
		while(improvement){ 
			
			improvement = false;
			
			for (int m1 = 0; m1 < M && !improvement; m1++) {
				
				machine1 = s.getMachine(m1);
				actualCostM1 = evaluator.getObjectFunctionValue(s, machine1);
				
				for (int j1 = 0; j1 < machine1.getJobs().size() && !improvement; j1++) {
					
					job1 = machine1.getJob(j1);
					for (int m2 = m1; m2 < M && !improvement; m2++) {
						
						machine2 = s.getMachine(m2);
						actualCostM2 = evaluator.getObjectFunctionValue(s, machine2);
						for (int j2 = (m1 == m2)? j1 + 1 : 0; j2 < machine2.getJobs().size() && !improvement; j2++) {
						
							if (m1 == m2) {
								
								//troca interna
								machine1.swapJobs(j1, j2);
								newCost = evaluator.getObjectFunctionValue(s, machine1);
								
								if(newCost < actualCostM1) improvement = true;
								else machine1.swapJobs(j1, j2);
								
							} else {
								
								//troca externa
								job2 = machine2.getJob(j2);
								
								machine1.replaceJob(j1, job2);
								machine2.replaceJob(j2, job1);
								
								newCost = evaluator.getObjectFunctionValue(s, machine1) + evaluator.getObjectFunctionValue(s, machine2);
								
								if(newCost < actualCostM1 + actualCostM2) improvement = true;
								else {	
									
									machine1.replaceJob(j1, job1);
									machine2.replaceJob(j2, job2);
								
								}
								
							}
															
						}
												
					}
					
				}
				
			}
		
		}
		
		return false;
	}

}
