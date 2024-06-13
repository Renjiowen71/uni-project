package test;

import org.junit.Test;

import pi.JOCLPi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class aims to test dummyThrowDarts function in JoCLPi java
 */
public class JOCLPiTest {

	@Test
	public void test1() {
//		System.out.println("Test 1 Starts");
		int[] results = new int[6];
		JOCLPi.dummyThrowDarts(new int[] { 0, 1, 2, 3, 4, 5 }, 1000, results, 0);
//		System.out.println("Result[0] = "+results[0]);
		assertTrue("Result[0] "+results[0]+" should be greater than 0 )", results[0] > 0);
        assertEquals("Result[1] " + results[1] + " should be 0 )", 0, results[1]);
        assertEquals("Result[2] " + results[2] + " should be 0 )", 0, results[2]);
        assertEquals("Result[3] " + results[3] + " should be 0 )", 0, results[3]);
        assertEquals("Result[4] " + results[4] + " should be 0 )", 0, results[4]);
        assertEquals("Result[5] " + results[5] + " should be 0 )", 0, results[5]);
		// check that results[0] is around 750 and results[1..5] are 0.
	}

	@Test
	public void test2() {
//		System.out.println("Test 2 Starts");
		int[] results = new int[6];
		JOCLPi.dummyThrowDarts(new int[] { 0, 1, 2, 3, 4, 5 }, 1000, results, 5);
//		System.out.println("Result[5] = "+results[5]);
		assertEquals("Result[0] " + results[0] + " should be 0 )", 0, results[0]);
		assertEquals("Result[1] " + results[1] + " should be 0 )", 0, results[1]);
		assertEquals("Result[2] " + results[2] + " should be 0 )", 0, results[2]);
		assertEquals("Result[3] " + results[3] + " should be 0 )", 0, results[3]);
		assertEquals("Result[4] " + results[4] + " should be 0 )", 0, results[4]);
		assertTrue("Result[5] = "+results[5]+"should be greater than 0 ", results[5] > 0);
		// check that results[5] is around 750 and results[0..4] are 0.

	}
}
