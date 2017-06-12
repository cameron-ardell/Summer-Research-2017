import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ProgramTreeTest {

	ProgramTree t;
	float min_const = 0;
	float max_const = 1000;
	float max_depth = 3;
	int max_seq = 3;
	@Before
	public void setUp() throws Exception {
		t = new ProgramTree(min_const, max_const, max_depth, max_seq);
		t.generateRandomNewTree();
	}

	//this is not thorough enough but does show there is a tree structure here of some kind
	@Test
	public void testGenerateRandomNewTree() {
		
		//has root with no parents
		ProgramNode root_parent = t.getRoot().parent;
		Assert.assertTrue(root_parent == null);
		
		//make sure depth <= max_depth
		//the last item in the list will be the last one added, so deepest
		ProgramNode last = t.node_list.get(t.node_list.size()-1);
		float depth = last.true_depth;
		Assert.assertTrue(max_depth + 1 >= depth );
	}

	@Test
	public void testGenerateRandomSubtree() {
		
		//make sure depth <= max_depth + 3
		//if @ max_depth (or +1), will play terminal node automatically
		//the last item in the list will be the last one added, so deepest
		ProgramNode last = t.node_list.get(t.node_list.size()-1);
		ProgramNode.ReturnType rt = last.rt;
		ProgramTree.generateRandomSubtree(last.true_depth, last, rt);		//build subtree on known leaf
		last = t.node_list.get(t.node_list.size()-1);
		float depth = last.true_depth;
		Assert.assertTrue(max_depth + 3 >= depth );
	}

	@Test
	public void testAddChildren() {
		ProgramNode last = t.node_list.get(t.node_list.size()-1);
		//see if can add kids to a leaf node (should have none)
		
	}

	@Test
	public void testRemoveNodeFromList() {
		fail("Not yet implemented");
	}

	@Test
	public void testRemoveSubtreeFromList() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetFitness() {
		fail("Not yet implemented");
	}

	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRoot() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetRoot() {
		fail("Not yet implemented");
	}

	@Test
	public void testRandomValIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testRandomValFloatFloat() {
		fail("Not yet implemented");
	}

	@Test
	public void testDetermineChildrenNum() {
		fail("Not yet implemented");
	}

	@Test
	public void testBuild_a_tree() {
		fail("Not yet implemented");
	}

}
