package com.vaguehope.morrigan.util;

import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;

public class LimitedRecentSetTest {

	private LimitedRecentSet<String> undertest;

	@Before
	public void before () throws Exception {
		this.undertest = new LimitedRecentSet<String>(3);
	}

	@Test
	public void itLimitsToMostRecent () throws Exception {
		this.undertest.push("a");
		this.undertest.push("b");
		this.undertest.push("c");
		this.undertest.push("d");
		assertEquals(Arrays.asList("d", "c", "b"), this.undertest.all());
	}

	@Test
	public void itDedupes () throws Exception {
		this.undertest.push("a");
		this.undertest.push("a");
		this.undertest.push("a");
		assertEquals(Arrays.asList("a"), this.undertest.all());
	}

	@Test
	public void itMovesDupesToEnd () throws Exception {
		this.undertest.push("c");
		this.undertest.push("a");
		this.undertest.push("b");
		this.undertest.push("c");
		this.undertest.push("a");
		assertEquals(Arrays.asList("a", "c", "b"), this.undertest.all());
	}

}
