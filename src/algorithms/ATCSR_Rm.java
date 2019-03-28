package algorithms;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import instance.loader.SchedulingInstancesConfig;
import model.Solution;
import model.Job;
import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.PairwiseInternalLocalSearch;

/**
 * Heuristic ATCSR Rm Apparent Tardiness Cost with separable Setup and Ready times for unrelated parallel machines
 * 
 * Proposed by Lin & Hsieh (2014) in:
 * Lin, Y.-K., & Hsieh, F.-Y. (2014). Unrelated parallel machine scheduling with
 * setup times and ready times. International Journal of Production Research, 52, 1200 – 1214.
 * DOI:10.1080/00207543.2013.848305
 * 
 * @author Rodney Oliveira Marinho Diana
 *
 */
public class ATCSR_Rm {

	private ObjectFunctionEvaluator evaluator;
	private float[] K1 = {0.2f, 0.6f, 0.8f, 1.0f, 1.2f, 1.4f, 1.6f, 1.8f, 2.0f, 2.4f, 2.8f, 3.2f, 3.6f, 4.0f, 4.4f, 4.8f, 5.2f, 5.6f, 6.0f, 6.4f, 6.8f, 7.2f};
	private float[] K2 = {0.1f, 0.3f, 0.5f, 0.7f, 0.9f, 1.1f, 1.3f, 1.5f, 1.7f, 1.9f, 2.1f};
	private float[] K3 = {0.001f, 0.0025f, 0.004f, 0.005f, 0.025f, 0.04f, 0.05f, 0.25f, 0.4f, 0.6f, 0.8f, 1.0f, 1.2f};
	
	/*private float[] K1 = {0.2f};
	private float[] K2 = {0.1f};
	private float[] K3 = {0.001f};
	*/
	
	public ATCSR_Rm(ObjectFunctionEvaluator evaluator) {
		super();
		this.evaluator = evaluator;
	}
	
	public Solution new_solution(Random rand, SchedulingInstancesConfig config, long totalTime, long initialTime){
		
		long tempoInicial = System.currentTimeMillis();
		long processTime = 0;
		
		boolean parar = false;
		
		PairwiseInternalLocalSearch pair_wise = new PairwiseInternalLocalSearch(evaluator);
		
		Solution best = null;
		
		for (int i = 0; i < K1.length && !parar; i++) {
			float k1 = K1[i];
			for (int j = 0; j < K2.length && !parar; j++) {
				float k2 = K2[j];
				for (int k = 0; k < K3.length && !parar; k++) {
					float k3 = K3[k];
					
					Solution solution = new_solution(rand, config, k1, k2, k3);
					pair_wise.run(rand, solution, totalTime, initialTime);
					float objF = solution.setObjectiveFunction();
					if (best == null || objF < best.getObjectiveFunction()) 
						best = solution;
					
					processTime = System.currentTimeMillis() - tempoInicial;
					if (processTime > 30000) parar = true;
					
				}
			}
		}
		
		return best;		
	
	}
	
	public Solution new_solution(Random rand, SchedulingInstancesConfig config, double k1, double k2, double k3){
		
		double[] ATCSR_Rm;
		MachineContainer choosedMachine;
		Job choosedJob;
		List<Job> unschedulingJobs = new ArrayList<Job>();
		MachineContainer[] machines = new MachineContainer[config.getNumberOfMachines()];
		double pMedio = getAvgProcessTime(config);
		double sMedio = getAvgSetupTime(config);
				
		for(int i = 0; i < machines.length; i++) 
			machines[i] = new MachineContainer(i);
		
		for(int job = 0; job < config.getNumberOfJobs(); job++)
			unschedulingJobs.add(new Job(job, config.getDueDate(job), config.isRestrict(job), config.getReleaseDate(job), config.getTardinessWeight(job), config.getEarliestWeight(job), config.getDueDateEarly(job)));
				
		while (!unschedulingJobs.isEmpty()) {
			
			choosedMachine = getLowCompletionTimeMachine(config, rand, machines);
			ATCSR_Rm = calcATCSR_Rm(choosedMachine, unschedulingJobs, pMedio, sMedio, k1, k2, k3, config);
			choosedJob = unschedulingJobs.remove(getIndexMaxATCSR_Rm(ATCSR_Rm));
			choosedMachine = getLowCompletionTimeMachine(config, rand, machines, choosedJob);
			choosedMachine.addJobInFinal(choosedJob);
			
		}
		
		return new Solution(evaluator, rand, machines);
		
	}
	
	public MachineContainer getLowCompletionTimeMachine(SchedulingInstancesConfig config, Random rand, MachineContainer[] machines, Job job){
		
		long minCompletionTime = Long.MAX_VALUE;
		long machineCompletationTime;
		MachineContainer choosedMachine = null;
		
		for (MachineContainer machineContainer : machines) {
			
			machineContainer.addJobInFinal(job);
			machineCompletationTime = config.calcularCompletationTime(machineContainer);
			
			if (machineCompletationTime < minCompletionTime){
				
				minCompletionTime = machineCompletationTime;
				choosedMachine = machineContainer;
							
			}
			
			machineContainer.removeLastJob();
			
		}
		
		return choosedMachine;
		
	}
	
	public MachineContainer getLowCompletionTimeMachine(SchedulingInstancesConfig config, Random rand, MachineContainer[] machines){
		
		long minCompletionTime = Long.MAX_VALUE;
		long machineCompletationTime;
		MachineContainer choosedMachine = null;
		
		for (MachineContainer machineContainer : machines) {
			
			machineCompletationTime = config.calcularCompletationTime(machineContainer);
			
			if (machineCompletationTime < minCompletionTime ){
				
				minCompletionTime = machineCompletationTime;
				choosedMachine = machineContainer;
							
			}
			
		}
		
		return choosedMachine;	
		
	}
	
	public double[] calcATCSR_Rm(MachineContainer machine, List<Job> jobs, double pMedio, double setupMedio, double k1, double k2, double k3, SchedulingInstancesConfig config){
		
		double WSTPTerm, slackTerm, setupTerm, readyTimeTerm;
		double jobProcessTime, jobSetupTime;
		double machineCompletionTime = config.calcularCompletationTime(machine);
		double[] ATCSR_Rm = new double[jobs.size()];
		Job job;
		Job previousJob = machine.getLastJob();
		
		for (int i = 0; i < jobs.size(); i++) {
			
			job = jobs.get(i);
			jobProcessTime = config.getProcessTime(machine.getId(), job.getId());
			jobSetupTime = config.getSetupTime(machine.getId(), (previousJob == null)? job.getId() : previousJob.getId(), job.getId());
			
			WSTPTerm = job.getTardinessWeight()/jobProcessTime;
			
			slackTerm = Math.exp(-Math.max(job.getDueDate() - jobProcessTime - Math.max(job.getReleaseDate(), machineCompletionTime + jobSetupTime), 0.0)/(k1 * pMedio));
						
			setupTerm = (setupMedio == 0.f)? 1.f : Math.exp(-jobSetupTime/(k2 * setupMedio));
			
			readyTimeTerm = Math.exp(-Math.max(job.getReleaseDate() - machineCompletionTime - jobSetupTime, 0.0)/(k3 * pMedio));
			
			ATCSR_Rm[i] = WSTPTerm * slackTerm * setupTerm * readyTimeTerm;
			
		}
		
		return ATCSR_Rm;
		
	}
	
	public int getIndexMaxATCSR_Rm(double[] ATCSR_Rm){
		
		int index = -1;
		double max = Double.NEGATIVE_INFINITY;
		
		for (int i = 0; i < ATCSR_Rm.length; i++) {
			
			double d = ATCSR_Rm[i];
			if (d > max) { max = d; index = i; }
			
		}
		
		return index;
		
	}
	
	public double getAvgSetupTime(SchedulingInstancesConfig config){
		
		double sTotal = 0;
		int[][][] setupsTime = config.getSetupTime();
		double N = config.getNumberOfJobs();
		double M = config.getNumberOfMachines();
		for (int i = 0; i < M; i++) {
			
			for (int j = 0; j < N; j++) {
				
				for (int k = 0; k < N; k++) {
					sTotal += setupsTime[i][j][k];
				}
				
			}
			
		}
		
		return sTotal/(N * N * M);
		
	}
	
	public double getAvgProcessTime(SchedulingInstancesConfig config){
		
		double pTotal = 0;
		int[][] processTime = config.processTime;
		double N = config.getNumberOfJobs();
		double M = config.getNumberOfMachines();
		
		for (int i = 0; i < M; i++) 
			
			for (int j = 0; j < N; j++)  pTotal += processTime[i][j];
				
		
		return pTotal/(N * M);
		
	}
	
}
