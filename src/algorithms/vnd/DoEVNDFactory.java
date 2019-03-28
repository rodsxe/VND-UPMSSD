package algorithms.vnd;

import java.util.HashMap;

import operators.function.evaluator.ObjectFunctionEvaluator;
import operators.localsearch.ExternalInsertionLocalSearch;
import operators.localsearch.InternalInsertionLocalSearch;
import operators.localsearch.ExternalSwapLocalSearch;
import operators.localsearch.InternalSwapLocalSearch;
import operators.localsearch.IGSInternalLocalSearch;
import operators.localsearch.IGSSwapLocalSearch;


public class DoEVNDFactory {
	
	public static final String ALPHA_TYPE = "--ALPHA";
	public static final String ORDER_TYPE = "--ORDER_TYPE";
	public static final String VND_TYPE = "--VND_TYPE";
	public static final String II_NEIGHBORHOOD = "--II_NEIGHBORHOOD";
	public static final String IS_NEIGHBORHOOD = "--IS_NEIGHBORHOOD";
	public static final String EI_NEIGHBORHOOD = "--EI_NEIGHBORHOOD";
	public static final String ES_NEIGHBORHOOD = "--ES_NEIGHBORHOOD";
	public static final String IGS_NEIGHBORHOOD = "--IGS_NEIGHBORHOOD";
	public static final String IGS_SWAP_NEIGHBORHOOD = "--IGS_SWAP_NEIGHBORHOOD";
	
	public static final String[] levels_parameters_l_18_design = {	ORDER_TYPE, 
																	VND_TYPE,
																	II_NEIGHBORHOOD, 
																	IS_NEIGHBORHOOD, 
																	EI_NEIGHBORHOOD, 
																	ES_NEIGHBORHOOD,
																	IGS_NEIGHBORHOOD,
																	IGS_SWAP_NEIGHBORHOOD
																 };
	
	private static String[] ORDER_DESIGN = {"cardinality", "structure"};
	private static String[] VND_DESIGN = {"basic", "random", "union"};
	private static String[] II_NEIGHBORHOOD_DESIGN = {"NA", "FI", "BI"};
	private static String[] IS_NEIGHBORHOOD_DESIGN = {"NA", "FI", "BI"};
	private static String[] EI_NEIGHBORHOOD_DESIGN = {"NA", "FI", "BI"};
	private static String[] ES_NEIGHBORHOOD_DESIGN = {"NA", "FI", "BI"};
	private static String[] IGS_NEIGHBORHOOD_DESIGN = {"NA", "2", "3"};
	private static String[] IGS_SWAP_NEIGHBORHOOD_DESIGN = {"NA", "2", "3"};
	
	public static VND getVND(ObjectFunctionEvaluator evaluator, HashMap<String, String> params) {
		
		VND vnd = null;
		
		if (params.containsKey(VND_TYPE)) {
			
			String vndType = VND_DESIGN[new Integer(params.get(VND_TYPE)) - 1];
					
			switch (vndType) {
			
				case "basic": vnd = new BasicVND(); break;
				case "random": vnd = new RandomVND(); break;
				case "union": vnd = new UnionVND(); break;
					
			}
			
		} else vnd = new BasicVND(); 
		
			
		String order = (!params.containsKey(VND_TYPE))? "cardinality" : ORDER_DESIGN[new Integer(params.get(ORDER_TYPE)) - 1];
		switch (order) {
		
			case "cardinality": setNeighborhoodByCardinalityOrder(vnd, evaluator, params); break;
			case "structure": setNeighborhoodByStructureChangingOrder(vnd, evaluator, params); break;
			
		}
			
		return vnd;
		
	}
	
	private static void setNeighborhoodByCardinalityOrder(VND vnd, ObjectFunctionEvaluator evaluator, HashMap<String, String> params) {
		
		String igs_neighboorhood = IGS_NEIGHBORHOOD_DESIGN[new Integer(params.get(IGS_NEIGHBORHOOD)) - 1];
		switch (igs_neighboorhood) {
			
			case "2": vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 2));break;
			case "3": vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 3));break;
			case "NA":break;
				
		}
		
		String igs_swap_neighboorhood = IGS_SWAP_NEIGHBORHOOD_DESIGN[new Integer(params.get(IGS_SWAP_NEIGHBORHOOD)) - 1];
		switch (igs_swap_neighboorhood) {
			
			case "2": vnd.addLocalSearch(new IGSSwapLocalSearch(evaluator, 1));break;
			case "3": vnd.addLocalSearch(new IGSSwapLocalSearch(evaluator, 2));break;
			case "NA":break;
				
		}
		
		String is_neighboorhood = IS_NEIGHBORHOOD_DESIGN[new Integer(params.get(IS_NEIGHBORHOOD)) - 1];
		switch (is_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new InternalSwapLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new InternalSwapLocalSearch(evaluator));break;
			case "NA":break;
				
		}
		
		String ii_neighboorhood = II_NEIGHBORHOOD_DESIGN[new Integer(params.get(II_NEIGHBORHOOD)) - 1];
		switch (ii_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator));break;
			case "NA":break;
			
		}
		
		String es_neighboorhood = ES_NEIGHBORHOOD_DESIGN[new Integer(params.get(ES_NEIGHBORHOOD)) - 1];
		switch (es_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator));break;
			case "NA":break;
				
		}
		
		String ei_neighboorhood = EI_NEIGHBORHOOD_DESIGN[new Integer(params.get(EI_NEIGHBORHOOD)) - 1];
		switch (ei_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator));break;
			case "NA":break;
				
		}
				
	}
	
	
	private static void setNeighborhoodByStructureChangingOrder(VND vnd, ObjectFunctionEvaluator evaluator, HashMap<String, String> params) {
		
		String ii_neighboorhood = II_NEIGHBORHOOD_DESIGN[new Integer(params.get(II_NEIGHBORHOOD)) - 1];
		switch (ii_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new InternalInsertionLocalSearch(evaluator));break;
			case "NA":break;
			
		}
		
		String is_neighboorhood = IS_NEIGHBORHOOD_DESIGN[new Integer(params.get(IS_NEIGHBORHOOD)) - 1];
		switch (is_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new InternalSwapLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new InternalSwapLocalSearch(evaluator));break;
			case "NA":break;
				
		}
		
		String ei_neighboorhood = EI_NEIGHBORHOOD_DESIGN[new Integer(params.get(EI_NEIGHBORHOOD)) - 1];
		switch (ei_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new ExternalInsertionLocalSearch(evaluator));break;
			case "NA":break;
				
		}
		
		String es_neighboorhood = ES_NEIGHBORHOOD_DESIGN[new Integer(params.get(ES_NEIGHBORHOOD)) - 1];
		switch (es_neighboorhood) {
			
			case "FI": vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator).setFirstImprovement(true));break;
			case "BI": vnd.addLocalSearch(new ExternalSwapLocalSearch(evaluator));break;
			case "NA":break;
				
		}
		
		String igs_neighboorhood = IGS_NEIGHBORHOOD_DESIGN[new Integer(params.get(IGS_NEIGHBORHOOD)) - 1];
		switch (igs_neighboorhood) {
			
			case "2": vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 2));break;
			case "3": vnd.addLocalSearch(new IGSInternalLocalSearch(evaluator, 3));break;
			case "NA":break;
				
		}
		
		String igs_swap_neighboorhood = IGS_SWAP_NEIGHBORHOOD_DESIGN[new Integer(params.get(IGS_SWAP_NEIGHBORHOOD)) - 1];
		switch (igs_swap_neighboorhood) {
			
			case "2": vnd.addLocalSearch(new IGSSwapLocalSearch(evaluator, 1));break;
			case "3": vnd.addLocalSearch(new IGSSwapLocalSearch(evaluator, 2));break;
			case "NA":break;
				
		}
					
	}
	
}
