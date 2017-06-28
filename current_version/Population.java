import java.util.ArrayList;

public class Population{
	public static ArrayList<GPTree> pop; //actual population of trees
	int pm = 30; 	// percent probability of mutation
	int pc = 72; 	// percent probability of crossover
	int num_gen = 37; 	//number of generations of evolution
	float k_as_frac_of_N = 0.2f; 	// portion of population to use in tournament selection

	// for creating a tree. adjustable for user
	float min_const = 0;
	float max_const = 1000;
	float max_depth = 6;
	int max_seq = 5;

	public Population(int numTrees){
		this.pop = new ArrayList<GPTree>();
		//generate the specified number of random trees
		GPTree new_tree;
		for (int i = 0; i < numTrees; i++){
			new_tree = new GPTree( min_const, max_const, max_depth, max_seq);
			new_tree.generateNewTree();
			pop.add(new_tree);
		}

		int randFit;
		for (int i = 0; i < numTrees; i++){
			randFit = GPNode.randomVal(0, numTrees);
			pop.get(i).fitness = randFit;
		}


		for (int i = 0; i < numTrees; i++){
			System.out.print(i + ": ");
			pop.get(i).printStats();
		}

		//mutate(pop.get(0), pop.get(1));
		GPTree b = tournament_selection();
		System.out.println();
		b.printTree();
	}


	/*
	 * Given two trees, mutate them at random points based
	 *	on an already established mutation probability: pm
	 * Input: two trees, t1 and t2
	 * Output: void
	*/
	public void mutate(GPTree t1, GPTree t2){
		ArrayList<GPNode> nodes1 = t1.toArrayList();
		ArrayList<GPNode> nodes2 = t2.toArrayList();

		int numnodes1 = nodes1.size();
		int numnodes2 = nodes2.size();

		// walk through each node on each tree with some probability of mutating
		int rand = 0;
		GPNode node;
		for(int i = 0; i<numnodes1;i++){
			node = nodes1.get(i);
			rand = GPNode.randomVal(0, 100);
			if(rand <= pm){	single_mut(node);
			}
		}
		for(int i = 0; i<numnodes2;i++){
			node = nodes2.get(i);
			rand = GPNode.randomVal(0, 100);
			if(rand <= pm){ single_mut(node); }
		}

	}

	/*
	 * In this function, given a node, mutate  it based on relevant 
	 * Input:	node - an existing node that you want to do the thing to
	 * Output:	void
	 */
	public void single_mut(GPNode node){

		GPNode.NodeType selectType = node.nodeType;
		
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
				newInd = GPNode.randomVal(0, GPNode.allMathOp.length);
				node.nodeType = GPNode.allMathOp[newInd];
				break;
			
			// all relational operations
			case EQ:
			case LT:
			case GT:
			case LEQ:
			case GEQ:
				newInd = GPNode.randomVal(0, GPNode.allCompareOp.length);
				node.nodeType = GPNode.allCompareOp[newInd];
				break;
			
			// ++ --
			case INC:
				node.nodeType = GPNode.NodeType.DEC;
				break;
			case DEC:
				node.nodeType = GPNode.NodeType.INC;
				break;				
			
			case AND:
				node.nodeType = GPNode.NodeType.OR;
				break;
			case OR:
				node.nodeType = GPNode.NodeType.AND;
				break;
	
			default:
				System.out.println("error: undefined NodeType in single mutation");
				System.out.println(selectType);
				System.exit(0);
		}
	}

	/*
	 * Go through k of N of the population, select the most fit
	 * Input: void
	 * Output: most fit of k 
	*/
	public GPTree tournament_selection(){
		GPTree best = null;
		int n = pop.size();
		float numTreesExact = k_as_frac_of_N * (float)n;
		int k = (int)Math.round( numTreesExact );
//		System.out.println("Given n = " + n + " trees, want " + k_as_frac_of_N + " of them, looking at: " + k + " trees.");
		
		GPTree cur;
		for(int i = 0; i < k; i++){
			int rand_ind = GPNode.randomVal(0, n);
			// System.out.println("number looked at: " + rand_ind);
			cur = pop.get( rand_ind );
			if( (best == null) || cur.fitness > best.fitness){
				best = cur;
			}
		}

		return best;
	}

	public void single_crossover(GPTree t1, GPTree t2){

		ArrayList<GPNode> nodes1 = t1.toArrayList();
		ArrayList<GPNode> nodes2 = t2.toArrayList();

		int numnodes1 = nodes1.size();
		int numnodes2 = nodes2.size();

		//find a node in t1 that is swappable with other nodes in t2
		




	}

























}