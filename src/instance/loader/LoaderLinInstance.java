package instance.loader;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.StringTokenizer;

import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.function.evaluator.scheduling.TardinessEvaluator;

public class LoaderLinInstance implements LoaderInstance {
	
	private int[][] dueDates;
	private int[][][] setupTimes;
	private static final TardinessEvaluator evaluator = new TardinessEvaluator();
	
	public LoaderLinInstance() {
		// TODO Auto-generated constructor stub
	}
	
	public ObjectFunctionEvaluator getEvaluator(String arqPath) {
		
		return evaluator;	
		
	}
	
	@Override
	public int[][] loadDueDate(String arqPath) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return dueDates;
	}

	@Override
	public int[][] loadProcessTime(String arqPath) throws FileNotFoundException, IOException {
		
		Integer N = new Integer(100);
		Integer M = new Integer(10);
        
		int[][] dueDates = new int[N][5];
		int[][] procTimes = new int[M][N];
		int[][][] setupTimes = new int[M][N][N];
        
		BufferedReader in = new BufferedReader(new FileReader(arqPath));
        
		String line = in.readLine();
        StringTokenizer st = new StringTokenizer(line, " ");
        
        for (int i = 0; i < M; i++) {
			
        	for (int j = 0; j < N; j++) {
				
        		String valueAttribute = ((String)st.nextToken());
        		procTimes[i][j] = new Integer(valueAttribute);
            
        	}
        	
		}
        
        //weights
		for (int j = 0; j < N; j++) {
			
    		String valueAttribute = ((String)st.nextToken());
    		dueDates[j][LoaderInstance.IS_RESTRICT] = 0;
           	dueDates[j][LoaderInstance.EARLINESS_WEIGHT] = 0;
           	dueDates[j][LoaderInstance.TARDINESS_WEIGHT] = new Integer(valueAttribute);
           	            
		}
		
		
        for (int i = 0; i < M; i++) {
			
        	for (int j = 0; j < N; j++) {
        		
        		for (int w = 0; w < N; w++) {
        			
        			String valueAttribute = ((String)st.nextToken());
            		setupTimes[i][w][j] = new Integer(valueAttribute);
            	
        		}
        	
        	}
        }
        
        //duedates
		for (int j = 0; j < N; j++) {
			
    		String valueAttribute = ((String)st.nextToken());
    		dueDates[j][LoaderInstance.DUE_DATE] = new Integer(valueAttribute);
    	   	
        }
		
		//release dates
		for (int j = 0; j < N; j++) {
			
    		String valueAttribute = ((String)st.nextToken());
    		dueDates[j][LoaderInstance.RELEASE_DATE] = new Integer(valueAttribute);
    	  		
        }
		
		this.setupTimes = setupTimes;
		this.dueDates = dueDates;
		
		return procTimes;
	}

	@Override
	public int[][][] loadSetupTime(String arqPath) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		return setupTimes;
	}

	public HashMap<String, TargetSol> loadTargetSolutions(String arqPath)throws FileNotFoundException, IOException{
		
		HashMap<String, TargetSol> result = new HashMap<String, TargetSol>();
		try {
			BufferedReader in = new BufferedReader(new FileReader(arqPath));
			String line;
			while((line = in.readLine()) != null) {
				
				StringTokenizer st = new StringTokenizer(line, ";");
				result.put(st.nextToken(), new TargetSol().setBestSol(new Integer(st.nextToken())));
				
			}
		
		} catch (FileNotFoundException e) {
			System.out.println("WARM: TARGET solutions file not found.");
		}		
		
		return result;
		
	}

}
