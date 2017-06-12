import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

public class ProgramNodeTest {
	
	ProgramNode node;
	ProgramNode.NodeType nodeType;
	ProgramNode parent;
	ProgramNode[] children;
	String varName = "nope";
	float constValue = 0;
	float depth;
	ProgramNode.ReturnType rt;

	@Before
	public void setUp() throws Exception {
		
		
	}

	// Dummy tester to make sure I understand format of unit test
	@Test
	public void testOnePlus() {
		float num = 16;
		float answer = num+1;
		Assert.assertEquals(answer, node.onePlus(num));
	}


	@Test
	public void testRun() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChildren() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetChilden() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetChild() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetChild() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetParent() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetParent() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDepth() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDepth() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetType() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetType() {
		fail("Not yet implemented");
	}

	@Test
	public void testPrintNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNumKids() {
		fail("Not yet implemented");
	}

	@Test
	public void testRandomVal() {
		fail("Not yet implemented");
	}


}
