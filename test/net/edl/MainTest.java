package net.edl;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MainTest {
	
	@Rule
	public ExpectedException exceptionRule = ExpectedException.none();

	//@Test
	public void testNullArg() throws Exception 
	{		
		exceptionRule.expect(org.apache.commons.cli.MissingOptionException.class);
		exceptionRule.expectMessage("Missing required option: r");
		
		net.edl.Main.main(null);	
	}

	@Test
	public void testValidArg() throws Exception 
	{		
		exceptionRule = ExpectedException.none();
		
		try {
			net.edl.Main.main(new String[] { "-r", "Genèse 1/1-28" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}

		try {
			net.edl.Main.main(new String[] { "-r", "Exode 2:1-22" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}

		try {
			net.edl.Main.main(new String[] { "-r", "Exode 22" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}

		try {
			net.edl.Main.main(new String[] { "-r", "2 Corinthiens 3/1-18" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}
		
		try {
			net.edl.Main.main(new String[] { "-r", "Jean 6/29-35" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}
		
		try {
			net.edl.Main.main(new String[] { "-r", "Psaume 133" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}
		
		try {
			net.edl.Main.main(new String[] { "-r", "Psaumes 119:1-110" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}
	}
	
	@Test
	public void testGenerateDoc() throws Exception 
	{		
		exceptionRule = ExpectedException.none();
		
		try {
			net.edl.Main.main(new String[] { "-r", "Exode 4/10-17" });
			net.edl.Main.main(new String[] { "-r", "Jean 6/29-35" });
		} catch (Exception e) {
			e.printStackTrace();
			fail("Ne doit pas planter");
		}
	}
}
