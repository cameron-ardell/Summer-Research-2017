import java.util.Random;

//import ProgramNode.NodeType;

import java.util.Map;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Population {
	// How an object that will generate random values need to be instantiated
	Random rand = new Random();
	private static List<ProgramTree> popt;  // actual trees
	float[] fitnesses;
	
	
	//great constructor
	public Population(int numTrees) {
		this.popt = new ArrayList<ProgramTree>();
		fitnesses = new float[numTrees];
		for (int i=0;i<numTrees;i++){fitnesses[i]=0;}
		
		//tree needs: float min_const, float max_const, float max_depth, int max_seq
		float min_const = 0;
		float max_const = 100;
		float max_depth = 25;
		int max_seq = 5;
		ProgramTree new_tree = new ProgramTree( min_const, max_const, max_depth, max_seq);
		for (int i = 0; i < numTrees; i++){
			new_tree.generateRandomNewTree();
			popt.add(new_tree);
		}
	}
	
	// Single generation
	public void single_gen(){
		/*
		 * 1. calc fit
		 * 2. random select M individuals
		 * 3. the k most fit of M breed
		 * 4. random duplication from entire pool
		 * 
		 */
		// 1
		for (int i = 0; i < popt.size(); i++){
			popt.get(i).fitness = calc_fit(popt.get(i));
		}
		
		//2
		List<ProgramTree> chosenOnes = new ArrayList<ProgramTree>();
		int m = (int)(0.7 * popt.size()); // <--- num selected for breeding
		int cur_unchosen = popt.size();
		
		int ind;
		for (int i = 0; i < m; i++){
			ind = ProgramNode.randomVal(0, cur_unchosen);
			ProgramTree chose = popt.remove(ind);
			chosenOnes.add(chose);
			cur_unchosen--;
		}
		
		//3
		Population.popt.sort(Comparator.comparing(ProgramTree::getFitness));
		k = (int)(0.7 * m);
		for(int i = m-1; m >=k; i-=2 ){
			
		}
		
	}
	
	public void breed(ProgramTree t1, ProgramTree t2){
		
		
	}
	
	
	/*
	 * Garbage calculating fitness
	 */
	public int calc_fit(ProgramTree tree){
		int num_add = 0;
		List<ProgramNode> all_nodes = tree.node_list;
		for(int i =0; i < all_nodes.size(); i++){
			if(all_nodes.get(i).nodeType == ProgramNode.NodeType.ADD){
				num_add+=1;
			}
		}
		return num_add;
	}
	
	
	/*
	 * In this function, given a tree, pick a random node in it and do 1-point mutation 
	 * Input:	tree1 - an existing tree that you want to do the thing to
	 * Output:	void
	 */
	public void single_mut(ProgramTree tree1){

		//random select node
		int num_nodes = tree1.node_list.size();
		int index = ProgramNode.randomVal(0, num_nodes);
		ProgramNode.NodeType selectType = tree1.node_list.get(index).getType();
		
		int newInd;
		// making recursive call for every kind but VAR and CONST (those have no children)
		switch(selectType) {
			// null cases
			case SEQUENCE:
			case IF:
			case ASSIGN:
			case NEG:
				break;

			// all math operations
			case ADD:
			case SUB:
			case MULT:
			case DIV:
			case EXP:
				newInd = ProgramNode.randomVal(0, ProgramNode.allMathOp.length);
				tree1.node_list.get(index).setType(ProgramNode.allMathOp[newInd]);
				break;
			
			// all relational operations
			case EQ:
			case LT:
			case GT:
			case LEQ:
			case GEQ:
				newInd = ProgramNode.randomVal(0, ProgramNode.allCompareOp.length);
				tree1.node_list.get(index).setType(ProgramNode.allCompareOp[newInd]);
				break;
			
			// ++ --
			case INC:
				tree1.node_list.get(index).setType(ProgramNode.NodeType.DEC);
				break;
			case DEC:
				tree1.node_list.get(index).setType(ProgramNode.NodeType.DEC);
				break;				
			
			case AND:
				tree1.node_list.get(index).setType(ProgramNode.NodeType.OR);
				break;
			case OR:
				tree1.node_list.get(index).setType(ProgramNode.NodeType.AND);
				break;
	
			default:
				System.out.println("error: undefined NodeType in single mutation");
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
	
	// THIS IS UNIFORM CROSSOVER (so entire subtrees are swapped)
	public void uniform_crossover(ProgramTree tree1, ProgramTree tree2){
		
		//random select first node
		int num_nodes_1 = tree1.node_list.size();
		int num_nodes_2 = tree2.node_list.size();
		int index = ProgramNode.randomVal(0, num_nodes_1);
		ProgramNode node1 = tree1.node_list.get(index);
		ProgramNode.ReturnType retType = node1.rt;
		
		//for collecting relevant nodes to pick from (only an array of indicies)
		List<Integer> applicable_nodes_indexes = new ArrayList<Integer>();
		
		//adds relevant nodes
		for( int i = 0; i < num_nodes_2; i++){
			if (tree2.node_list.get(i).rt == retType){
				applicable_nodes_indexes.add(i);
			}
		}
		
		//creates probability distribution (even for now...
		// just wanted to do out to show example of it, could
		// have just done random value in applicable_nodes_indexes
		// for this case)
		int num_app_nodes = applicable_nodes_indexes.size();
		int[] prob_dist = new int[num_app_nodes];
		Arrays.fill(prob_dist, 1);
		
		//selects random index for random 2nd node that is compatible return type
		int sec_ind = selectIndexWRTProbability(prob_dist);
		ProgramNode node2 = tree2.node_list.get(sec_ind);
		
		// now that have both nodes, need to make it so their parents are swapped,
		// their parents are pointing to correct child
		
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
	
	
	public void single_crossover(ProgramTree tree1, ProgramTree tree2){
		
		//random select first node
		int num_nodes_1 = tree1.node_list.size();
		int num_nodes_2 = tree2.node_list.size();
		int index = ProgramNode.randomVal(0, num_nodes_1);
		ProgramNode node1 = tree1.node_list.get(index);
		ProgramNode.ReturnType retType = node1.rt;
		
		//get info for parameters for second node selection
		int num_kids = (int)node1.getNumKids();
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
		
		//adds relevant nodes
		for( int i = 0; i < num_nodes_2; i++){
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
