import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import model.MachineContainer;
import operators.function.evaluator.ObjectFunctionEvaluator;
import model.Solution;


public class SolutionWriter {
	
	private int[][][] custoDeSetup;
	private int[][] custos;
	private Solution solucao;
	private ObjectFunctionEvaluator evaluator;
	
	public SolutionWriter(Solution solucao, ObjectFunctionEvaluator evaluator, int[][] custos, int[][][] custoDeSetup) {
		super();
		this.solucao = solucao;
		this.custos = custos;
		this.custoDeSetup = custoDeSetup;
		this.evaluator = evaluator;
	}
	
	
	public void writeSolInArq(String arq, String instance, String FILE_TYPE){
		try {
			
			solucao.setObjectiveFunction();
			
			String instanciaName = instance.substring(0, instance.indexOf(FILE_TYPE));
			
			FileWriter fileWriter = new FileWriter(arq, false);
			PrintWriter printWriter = new  	PrintWriter(fileWriter);
			printWriter.println(instanciaName + "\t" + custos[0].length + "\t" + custos.length + "\t" + this.solucao.getObjectiveFunction());
			
			MachineContainer[] machines = this.solucao.getMachines();
			for (int i = 0; i < machines.length; i++) {
				MachineContainer machine = machines[i];
				printWriter.print(evaluator.getObjectFunctionValue(this.solucao, machine) + "\t");
				int numTarefas = machine.size();
				printWriter.print(numTarefas + "\t");
				for (int j = 0; j < numTarefas; j++) {
					printWriter.print((machine.getJob(j).getId() + 1) + "\t");
				}
				printWriter.println();
			}
			printWriter.close();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
