import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Population {
	// How an object that will generate random values need to be instantiated
	Random rand = new Random();
	public static List<ProgramTree> popt;  // actual trees
	int pm = 3; 	// percent probability of mutation
	int pc = 72; 	// percent probability of crossover
	int num_gen = 37; 	//number of generations of evolution
	
	double k_frac = 0.2;
	
	//constructor
	public Population(int numTrees) {
		this.popt = new ArrayList<ProgramTree>();
		
		//tree needs: float min_const, float max_const, float max_depth, int max_seq
		float min_const = 0;
		float max_const = 1000;
		float max_depth = 3;
		int max_seq = 3;
		ProgramTree new_tree = new ProgramTree( min_const, max_const, max_depth, max_seq);
		for (int i = 0; i < numTrees; i++){
			new_tree.generateRandomNewTree();
			popt.add(new_tree);
		}
		
		//add fitness to tree since it now exists
		for (int i = 0; i < popt.size(); i++){
			popt.get(i).fitness = calc_fit(popt.get(i));
		}
	}
	
	// get it? Because Mayflies die so quickly so you can see all the generations...
	// 	Yeah that was a reach.
	// Point being, this is all the evolving
	public void time_mayfly(){
		for(int i = 0; i < num_gen; i++){
			single_gen();
		}
	}
	
	
/*	Algorithm:
 * 			N = population size
			P = create parent population by randomly creating N individuals
			C = create empty child population
		    while not enough individuals in C
		        parent1 = select parent   ***** HERE IS WHERE YOU DO TOURNAMENT SELECTION *****
		        parent2 = select parent   ***** HERE IS WHERE YOU DO TOURNAMENT SELECTION *****
		        child1, child2 = crossover(parent1, parent2)
		        mutate child1, child2
		        evaluate child1, child2 for fitness
		        insert child1, child2 into C
		    end while
		    P = C*/
	// Single generation
	public void single_gen(){
		List<ProgramTree> new_kids = new ArrayList<ProgramTree>();
		int N = popt.size();
		ProgramTree c1, c2, p1, p2;
		int rand;
		while (new_kids.size() != N){
			p1 = tournament();
			c1 = new ProgramTree(p1);
			p2 = tournament();
			c2 = new ProgramTree(p2);
			
			rand = ProgramNode.randomVal(0, 100);
			if(rand <= pc){ single_crossover(c1, c2); }
			mutate(c1, c2);
			
			c1.fitness = calc_fit(c1);
			c2.fitness = calc_fit(c2);
			
			new_kids.add(c1);
			new_kids.add(c2);
		}
		
	}
	
	public void mutate(ProgramTree t1, ProgramTree t2){
		int numnodes1 = t1.node_list.size();
		int numnodes2 = t1.node_list.size();
		
		// walk through each node on each tree with some probability of mutating
		int rand = 0;
		ProgramNode node;
		for(int i = 0; i<numnodes1;i++){
			node = t1.node_list.get(i);
			rand = ProgramNode.randomVal(0, 100);
			if(rand <= pm){ single_mut(node); }
		}
		for(int i = 0; i<numnodes2;i++){
			node = t2.node_list.get(i);
			rand = ProgramNode.randomVal(0, 100);
			if(rand <= pm){ single_mut(node); }
		}	
	}
	
	public ProgramTree tournament(){
		ProgramTree best = null;
		int N = popt.size();
		int k = (int)k_frac * N;
		ProgramTree ind;
		for(int i = 0; i < k; i++){
			ind = popt.get(	ProgramNode.randomVal(0, N) );
			if((best == null) || ind.fitness > best.fitness){
				best = ind;
			}
		}
		return best;
	}
	
	
	/*
	 * Garbage calculating fitness
	 */
	public int calc_fit(ProgramTree tree) {
		int num_add = 0;
		List<ProgramNode> all_nodes = tree.node_list;
		for (int i = 0; i < all_nodes.size(); i++) {
			if (all_nodes.get(i).nodeType == ProgramNode.NodeType.ADD) {
				num_add += 1;
			}
		}
		return num_add;
	}
	
	
	/*
	 * In this function, given a node, do single mutation 
	 * Input:	node - an existing node that you want to do the thing to
	 * Output:	void
	 */
	public void single_mut(ProgramNode node){

		ProgramNode.NodeType selectType = node.getType();
		
		int newInd;
		// making recursive call for every kind but VAR and CONST (those have no children)
		switch(selectType) {
			// null cases
			case SEQUENCE:
			case IF:
			case ASSIGN:
			case NEG:
			case CONST:
			case VAR:
				break;

			// all math operations
			case ADD:
			case SUB:
			case MULT:
			case DIV:
			case EXP:
				newInd = ProgramNode.randomVal(0, ProgramNode.allMathOp.length);
				node.setType(ProgramNode.allMathOp[newInd]);
				break;
			
			// all relational operations
			case EQ:
			case LT:
			case GT:
			case LEQ:
			case GEQ:
				newInd = ProgramNode.randomVal(0, ProgramNode.allCompareOp.length);
				node.setType(ProgramNode.allCompareOp[newInd]);
				break;
			
			// ++ --
			case INC:
				node.setType(ProgramNode.NodeType.DEC);
				break;
			case DEC:
				node.setType(ProgramNode.NodeType.DEC);
				break;				
			
			case AND:
				node.setType(ProgramNode.NodeType.OR);
				break;
			case OR:
				node.setType(ProgramNode.NodeType.AND);
				break;
	
			default:
				System.out.println("error: undefined NodeType in single mutation");
				System.out.println(selectType);
				System.exit(0);
			}
	}
	
	/*
	 * In this function, given a tree, pick a random node in it and do uniform mutation 
	 * Input:	tree1 - an existing tree that you want to do the thing to
	 * Output:	void
	 */
	public void subtree_mut(ProgramTree tree1){

		//random select node
		int num_nodes = tree1.node_list.size();
		int index = ProgramNode.randomVal(0, num_nodes);
		ProgramNode.ReturnType retType = tree1.node_list.get(index).rt;
		ProgramNode chosen = tree1.node_list.get(index);
		ProgramNode parentNode = chosen.getParent();
		
		//removing any extra information
		ProgramNode[] children = chosen.children;
		for(int i = 0; i < chosen.getNumKids(); i++){
			tree1.removeSubtreeFromList(children[i]);
		}
		chosen.setChilden(null);
		chosen.constValue = ProgramTree.float_null;
		chosen.varName = null;
		
		
		chosen = ProgramTree.generateRandomSubtree(chosen.true_depth, chosen.parent, retType);
	}
	
	// THIS IS SINGLE POINT CROSSOVER (so entire subtrees are swapped)
	public void single_crossover(ProgramTree tree1, ProgramTree tree2){
		
		int num_nodes_1 = tree1.node_list.size();
		int num_nodes_2 = tree2.node_list.size();
		
		// find a node that is swapable with a node in the other tree
		int num_app_nodes = 0;
		ProgramNode node1 = null;
		while(num_app_nodes == 0){
			//random select first node
			int index = ProgramNode.randomVal(0, num_nodes_1);
			node1 = tree1.node_list.get(index);
			ProgramNode.ReturnType retType = node1.rt;

			//for collecting relevant nodes to pick from (only an array of indicies)
			List<Integer> applicable_nodes_indexes = new ArrayList<Integer>();

			//adds relevant nodes
			for( int i = 0; i < num_nodes_2; i++){
				if (tree2.node_list.get(i).rt == retType){
					applicable_nodes_indexes.add(i);
				}
			}

			num_app_nodes = applicable_nodes_indexes.size();
		}
		
		if(num_app_nodes > 0){
			//creates probability distribution (just wanted to do out to show example of it, could
			// 		have just done random value in applicable_nodes_indexes for this case)
			int[] prob_dist = new int[num_app_nodes];
			Arrays.fill(prob_dist, 1);

			//selects random index for random 2nd node that is compatible return type
			int sec_ind = selectIndexWRTProbability(prob_dist);
			ProgramNode node2 = tree2.node_list.get(sec_ind);


			// lists to add to new tree (also removes from list)
			List<ProgramNode> sub_from_1 = tree1.removeSubtreeFromList( node1 );
			List<ProgramNode> sub_from_2 = tree2.removeSubtreeFromList( node2 );
			// update node lists for new crossover
			tree1.node_list.addAll(sub_from_2);
			tree2.node_list.addAll(sub_from_1);

			//getting index of parents so avoiding pass by reference error I hope
			int ind_p1 = tree1.node_list.indexOf(node1.parent);
			int ind_p2 = tree2.node_list.indexOf(node2.parent);
			//reassign subtree roots' parent
			node1.parent = tree2.node_list.get(ind_p2);
			node2.parent = tree1.node_list.get(ind_p1);
			//reassign child for parents of subtree roots
			int p1_num_kids = (int)node2.parent.getNumKids();
			for (int i = 0; i < p1_num_kids; i++){
				if(node2.parent.children[i] == node1){ node2.parent.children[i] = node2; break;	}
			}
			int p2_num_kids = (int)node1.parent.getNumKids();
			for (int i = 0; i < p2_num_kids; i++){
				if(node1.parent.children[i] == node2){ node1.parent.children[i] = node1; break;	}
			}
		}
	}
	
	//THIS IS UNIFORM CROSSOVER
	public void uniform_crossover(ProgramNode node1, ProgramTree tree1, ProgramTree tree2){
				
		//get info for parameters for second node selection
		int num_kids = (int)node1.getNumKids();
		ProgramNode.ReturnType retType = node1.rt;
		ProgramNode.NodeType selectType = node1.nodeType;
		List<ProgramNode.NodeType> okTypes = new ArrayList<ProgramNode.NodeType>();
		
		//determine which nodetypes are OK for selecting 2nd node
		switch(selectType) {
			// null cases all unique
			case SEQUENCE:
				okTypes.add(ProgramNode.NodeType.SEQUENCE);
				break;
			
			case IF:
				okTypes.add(ProgramNode.NodeType.IF);
				break;
			
			case ASSIGN:
				okTypes.add(ProgramNode.NodeType.ASSIGN);
				break;
			
			case NEG:
				okTypes.add(ProgramNode.NodeType.NEG);
				break;

			// all math operations
			case ADD:
			case SUB:
			case MULT:
			case DIV:
			case EXP:
				okTypes.add(ProgramNode.NodeType.ADD);
				okTypes.add(ProgramNode.NodeType.SUB);
				okTypes.add(ProgramNode.NodeType.MULT);
				okTypes.add(ProgramNode.NodeType.DIV);
				okTypes.add(ProgramNode.NodeType.EXP);
				break;
			
			// all relational operations
			case EQ:
			case LT:
			case GT:
			case LEQ:
			case GEQ:
				okTypes.add(ProgramNode.NodeType.EQ);
				okTypes.add(ProgramNode.NodeType.LT);
				okTypes.add(ProgramNode.NodeType.GT);
				okTypes.add(ProgramNode.NodeType.LEQ);
				okTypes.add(ProgramNode.NodeType.GEQ);
				break;
			
			// ++ --
			case INC:
			case DEC:
				okTypes.add(ProgramNode.NodeType.INC);
				okTypes.add(ProgramNode.NodeType.DEC);
				break;				
			
			case AND:
			case OR:
				okTypes.add(ProgramNode.NodeType.AND);
				okTypes.add(ProgramNode.NodeType.OR);
				break;
			
			case VAR:
			case CONST:
				okTypes.add(ProgramNode.NodeType.VAR);
				okTypes.add(ProgramNode.NodeType.CONST);
				break;
				
			default:
				System.out.println("error: undefined NodeType in single mutation");
				System.exit(0);
			}
		
		//for collecting relevant nodes to pick from (only an array of indicies)
		List<Integer> applicable_nodes_indexes = new ArrayList<Integer>();
		
		int num_nodes2 = tree2.node_list.size();
		//adds relevant nodes
		for( int i = 0; i < num_nodes2; i++){
			ProgramNode.NodeType nt = tree2.node_list.get(i).nodeType;
			int numK2 = (int)tree2.node_list.get(i).getNumKids();
			if (okTypes.contains(nt) && numK2 == num_kids ){ 	//if OK NodeType and good # of kids, add
				applicable_nodes_indexes.add(i);
			}
		}
		
		//creates probability distribution (even for now...
		// just wanted to do out to show example of it, could
		// have just done random value in applicable_nodes_indexes
		// for this case)
		int num_app_nodes = applicable_nodes_indexes.size();
		
		if (num_app_nodes > 0){
		
			int[] prob_dist = new int[num_app_nodes];
			Arrays.fill(prob_dist, 1);

			//selects random index for random 2nd node that is compatible return type
			int sec_ind = selectIndexWRTProbability(prob_dist);
			ProgramNode node2 = tree2.node_list.get(sec_ind);

			// lists to add to new tree (also removes from list)
			tree1.removeNodeFromList( node1 );
			tree2.removeNodeFromList( node2 );
			// update node lists for new crossover
			tree1.node_list.add( node2 );
			tree2.node_list.add( node1 );

			//getting index of parents so avoiding pass by reference error I hope
			int ind_p1 = tree1.node_list.indexOf(node1.parent);
			int ind_p2 = tree2.node_list.indexOf(node2.parent);
			//reassign swapped nodes' parent
			node1.parent = tree2.node_list.get(ind_p2);
			node2.parent = tree1.node_list.get(ind_p1);
			//reassign child for parents of swapped nodes
			int p1_num_kids = (int)node2.parent.getNumKids();
			for (int i = 0; i < p1_num_kids; i++){
				if(node2.parent.children[i] == node1){ node2.parent.children[i] = node2; break;	}
			}
			int p2_num_kids = (int)node1.parent.getNumKids();
			for (int i = 0; i < p2_num_kids; i++){
				if(node1.parent.children[i] == node2){ node1.parent.children[i] = node1; break;	}
			}
		}
		// If no nodes that can swap with, mutate
		else {
			single_mut(node1);
		}
	}
	
	
	
	/* Given an array of raw weights, NOT relative, randomly select
	 * 	an index and return it.
	 * Useful for crossover, single mutation, subtree mutation
	 */
	public int selectIndexWRTProbability(int[] prob_distrib){
		
		int sum = 0;
		for ( int i = 0; i < prob_distrib.length; i++){ sum += prob_distrib[i]; }
		
		int threshold = rand.nextInt(sum);
		int return_ind = 0;
		int cur_sum = 0;
		while(cur_sum < threshold){
			cur_sum += prob_distrib[return_ind];
			return_ind++;
			
			// alternatively, can write:
			//cur_sum += prob_distrib[++return_ind];
			// but not sure about syntax so left as option
		}
		return Math.max(0, return_ind-1);
	}
	
	
	
	
}
