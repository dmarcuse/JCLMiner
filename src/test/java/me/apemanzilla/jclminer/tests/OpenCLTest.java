package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.jclminer.miners.CLCodeLoader;

public class OpenCLTest {

	CLContext context;
	CLQueue queue;
	CLProgram program;
	
	@Before
	public void setUp() {
		context = JavaCL.createBestContext();
		queue = context.createDefaultQueue();
		String mainCode = CLCodeLoader.loadCode("/gpu_miner.cl");
		String testCode = CLCodeLoader.loadCode("/test_kernels.cl");
		assertNotNull("Failed to load main code", mainCode);
		assertNotNull("Failed to load test code", testCode);
		program = context.createProgram(mainCode, testCode);
		// Test that CL code can be compiled and the testCompile kernel can be run
		CLKernel kernel = program.createKernel("testCompile");
		CLEvent compilationTest = kernel.enqueueNDRange(queue, new int[] {1});
		compilationTest.waitFor();
	}

	@After
	public void tearDown() throws Exception {
		context.release();
	}
	
	CLContext getContext() {
		return context;
	}
	
	CLQueue getQueue() {
		return queue;
	}
	
	CLProgram getProgram() {
		return program;
	}

}
