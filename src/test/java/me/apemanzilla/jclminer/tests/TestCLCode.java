package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.jclminer.JCLMiner;
import me.apemanzilla.jclminer.miners.MinerInitException;

public class TestCLCode {

	private static String code;
	
	private CLContext context;
	private CLQueue queue;
	private CLProgram program;

	public String loadCode() {
		try {
			InputStream is = JCLMiner.class.getResourceAsStream("/gpu_miner.cl");
			if (is == null) return null;
			if (is.available() == 0) return null;
			byte[] data = new byte[is.available()];
			is.read(data, 0, is.available());
			return new String(data).trim();
		} catch (IOException e) {
			return null;
		}
	}
	
	@Before
	public void setUp() throws Exception {
		this.context = JavaCL.createBestContext();
		this.queue = context.createDefaultQueue();
		String code = loadCode();
		if (code == null) fail("Could not load CL code");
		program = context.createProgram(code);
	}

	@After
	public void tearDown() throws Exception {
		context.release();
	}
	
	@Test
	/**
	 * Tests if the code can be compiled and run
	 */
	public void testCompile() {
		CLKernel kernel = program.createKernel("testCompile");
		kernel.enqueueNDRange(queue, new int[] {1});
	}
	
	@Test
	/**
	 * Test hashing (padding and digest)
	 */
	public void testHash() {
		fail("Not yet implemented");
	}

//	@Test
//	/**
//	 * Test hashing in parallel
//	 */
//	public void testParallelHash() {
//		fail("Not yet implemented");
//	}
	
	@Test
	/**
	 * Test conversion of hashes to longs
	 */
	public void testLongToHash() {
		fail("Not yet implemented");
	}
	
}
