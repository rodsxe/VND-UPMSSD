package operators.function.evaluator.scheduling;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.sun.tools.javac.util.Pair;

import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import model.base.Container;
import operators.function.evaluator.ObjectFunctionEvaluator;

public class TardinessEvaluator implements ObjectFunctionEvaluator {
	
	private float CONSTRAINT_PENALTY = 1000;
	
	private long numberOfEvaluations = 0;
	
	private boolean isToContEvaluation = true;
	
	private SchedulingInstancesConfig schedulingConfig;
	
	public void setNumberOfEvaluations(long numberOfEvaluations) {
		this.numberOfEvaluations = numberOfEvaluations;
	}

	public TardinessEvaluator() {
		super();
	}

	public SchedulingInstancesConfig getSchedulingConfig() {
		return schedulingConfig;
	}

	public void setSchedulingConfig(SchedulingInstancesConfig schedulingConfig) {
		this.schedulingConfig = schedulingConfig;
		numberOfEvaluations = 0;
	}
	
	public void setCONSTRAINT_PENALTY(float cONSTRAINT_PENALTY) {
		CONSTRAINT_PENALTY = cONSTRAINT_PENALTY;
	}

	@Override
	public float getObjectFunctionValue(Solution solution, Container container) {
		
		long tardiness = 0;
		long completationTime = 0;
		
		MachineContainer machine = (MachineContainer)container;
		int machineID = machine.getId();
		
		List<Job> jobs = machine.getJobs();
		Job job, previousJob;
		
		if (jobs.isEmpty()) return 0;
		
		previousJob = jobs.get(0);
		
		for (int i = 0; i < jobs.size(); i++){
			
			job = jobs.get(i);
			
			completationTime = Math.max(job.getReleaseDate(), completationTime + schedulingConfig.getSetupTime(machineID, previousJob.getId(), job.getId()));
			
			completationTime += schedulingConfig.getProcessTime(machineID, job.getId());
			
			long taskDiff = completationTime - job.getDueDate();
			
			long taskTardiness = Math.max(0, taskDiff);
			job.setHaAtraso(taskTardiness > 0);
			tardiness += (taskTardiness * job.getTardinessWeight()) + ((job.isRestrict() && taskTardiness > 0)? taskTardiness * CONSTRAINT_PENALTY: 0);
			
			previousJob = job;
									
		}
		
		if (isToContEvaluation) numberOfEvaluations++;
		
		return tardiness;
		
	}
	
	public float getObjectFunctionValue(Solution solution) {
		
		this.isToContEvaluation = false; 
			
		float custoTotal = 0;
		MachineContainer[] machines = solution.getMachines();
		for(int i = 0; i < machines.length; i++){
			
			MachineContainer machine = machines[i];
			custoTotal += getObjectFunctionValue(solution, machine);
		
		}
		
		numberOfEvaluations++;
		
		this.isToContEvaluation = true; 
		
		return custoTotal;
	
	}
	
	public float getLSCost(float costM1, float costM2){
		
		return costM1 + costM2;
		
	}
	
	@Override
	public int getLSMachineIndex(Random rand, Solution solution, List<MachineContainer> machines) {
		
		float cost = -1;
		int index = -1;
		int cont = 0;
		
		while (cost <= 0 && cont < machines.size()) {
			
			index = rand.nextInt(machines.size());
			MachineContainer choosed = machines.get(index);
			cost = getObjectFunctionValue(solution, choosed);
			cont++;
			
		}
		
		if(cost == 0) return -1;
		
		return index;
		
	}
	
	public boolean isValidSolution(Solution solution) {
		
		MachineContainer[] machines = solution.getMachines();
		
		for (MachineContainer machineContainer : machines) {
			
			List<Job> jobs = machineContainer.getJobs();
			
			if (jobs.isEmpty()) continue;
						
			for (Job job : jobs) 
				
				if (job.isRestrict() && job.isHaAtraso()) return false;
				
		}
		
		return true;
		
	}
	
	
	public void writeResult(Solution solution, PrintWriter printWriter) {
		
		long tardiness = 0;
		long maxTardiness = 0;
		long dueDate = 0;
		int nTaredinessJobs = 0;
		for (MachineContainer machine : solution.getMachines()) {
			
			long completationTime = 0;
			int machineID = machine.getId();
			List<Job> jobs = machine.getJobs();
			Job job, previousJob;
			
			if (jobs.isEmpty()) continue;
			
			previousJob = jobs.get(0);
			for (int i = 0; i < jobs.size(); i++){
				
				job = jobs.get(i);
				completationTime = Math.max(job.getReleaseDate(), completationTime + schedulingConfig.getSetupTime(machineID, previousJob.getId(), job.getId()));
				
				completationTime += schedulingConfig.getProcessTime(machineID, job.getId());
				
				long taskDiff = completationTime - job.getDueDate();
				
				if (Math.max(0, taskDiff) > 0){
					
					nTaredinessJobs++;
					
					if (Math.max(0, taskDiff) > maxTardiness){
						
						maxTardiness = Math.max(0, taskDiff);
						dueDate = job.getDueDate();
					
					}
					
				}
				previousJob = job;
				
				long taskTardiness = Math.max(0, taskDiff);
				//System.out.print((job.getId() + 1) + ":" + taskTardiness + ":" + job.getTardinessWeight() + ";");
				//job.setHaAtraso(taskTardiness > 0);
				tardiness += (taskTardiness * job.getTardinessWeight()) + 
							((job.isRestrict() && taskTardiness > 0)? taskTardiness * CONSTRAINT_PENALTY: 0);
				
				
										
			}
			
			
			//System.out.println(tardiness);
			
			
		}
		
		printWriter.print(";" + solution.getObjectiveFunction() + ";"+ nTaredinessJobs +";" + maxTardiness + ";" + dueDate + ";" + numberOfEvaluations + ";");
		
				
	}

}
