package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.jclminer.JCLMiner;
import me.apemanzilla.jclminer.miners.CLCodeLoader;

public class OpenCLTest {

	CLContext context;
	CLQueue queue;
	CLProgram program;
	
	@Before
	public void setUp() {
		context = JavaCL.createBestContext();
		queue = context.createDefaultQueue();
		String mainCode = CLCodeLoader.loadCode("/sha256.cl");
		String vectorCode = CLCodeLoader.loadCode("/vsha256.cl");
		String minerCode = CLCodeLoader.loadCode("/krist_miner.cl");
		String testCode = CLCodeLoader.loadCode("/test_kernels.cl");
		assertNotNull("Failed to load main code", mainCode);
		assertNotNull("Failed to load vector code", vectorCode);
		assertNotNull("Failed to load miner code", minerCode);
		assertNotNull("Failed to load test code", testCode);
		program = context.createProgram(vectorCode, mainCode, minerCode, testCode);
		// Treat all warning as errors so
		// tests will fail if warnings occur
		program.addBuildOption("-Werror");
		
		// Add other build options
		for (String opt : JCLMiner.cl_build_options) {
			program.addBuildOption(opt);
		}
		
		// Test that CL code can be compiled and the testCompile kernel can be run
		CLKernel kernel = program.createKernel("testCompile");
		CLEvent compilationTest = kernel.enqueueNDRange(queue, new int[] {1});
		compilationTest.waitFor();
	}

	@After
	public void tearDown() throws Exception {
		context.release();
	}

}
