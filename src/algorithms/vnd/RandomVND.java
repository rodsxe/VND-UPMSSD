package algorithms.vnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.Solution;
import operators.localsearch.LocalSearch;

public class RandomVND extends BasicVND{

	private List<LocalSearch> seekers = new ArrayList<LocalSearch>();
	
	public void addLocalSearch(LocalSearch localSearch) { 
		seekers.add(localSearch);
	}
	
	public boolean run(Random rand, Solution s, long totalTime, long initialTime){
		
		boolean improvement = false;
		
		List<LocalSearch> removedLSOp = new ArrayList<LocalSearch>();
		while (!seekers.isEmpty()) {
			
			LocalSearch ls = seekers.remove(rand.nextInt(seekers.size()));
			removedLSOp.add(ls);
			
			//if (rand.nextFloat() < new Sigmoid().value(s.taxaDeMutacao[ls.getProbabilityIndex()])) 
				
			if (ls.run(rand, s, totalTime, initialTime)) {
				
				improvement = true;
				seekers.addAll(removedLSOp);
				removedLSOp.clear();
				
			}
						
		}
		
		seekers.addAll(removedLSOp);
		s.setObjectiveFunction();
		
		return improvement;
		
	}
	
	/*public void runCellOrder(Random rand, Cell s){
		
		int i = 0;
		int[] order = s.getVndOrder();
		
		while (i < order.length) {
			
			LocalSearch ls = seekers.get(order[i]);
			i++;
				
			if (ls.run(rand, s, totalTime, initialTime)) {
				
				i = 0;
				
			}
						
		}
		
		s.setObjectiveFunction();
		
	}*/
	
}
