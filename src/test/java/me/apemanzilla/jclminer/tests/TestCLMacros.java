package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.jclminer.miners.CLCodeLoader;

/**
 * This class contains multiple JUnit tests that test the OpenCL portions of JCLMiner code.
 * This should be used to ensure that valid results are produced when modifying the OpenCL code.
 * @author apemanzilla
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCLMacros {
	
	private CLContext context;
	private CLQueue queue;
	private CLProgram program;
	
	@Before
	public void setUp() {
		this.context = JavaCL.createBestContext();
		this.queue = context.createDefaultQueue();
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
	
	/**
	 * Tests whether the ZFRS_INT macro (integer zero fill right shift) is producing expected results - compares output from OpenCL to Java's results
	 */
	@Test
	public void testMacro_ZFRS_INT() {
		int[] input = {1, 5, -1, -20, -190, 40, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
		int[] shift = new int[input.length];
		for (int i = 0; i < shift.length; i++)
			shift[i] = 1;
		Pointer<Integer>
				inputPtr = Pointer.allocateInts(input.length).order(context.getByteOrder()),
				shiftPtr = Pointer.allocateInts(input.length).order(context.getByteOrder());
		for (int i = 0; i < input.length; i++) {
			inputPtr.set(i, input[i]);
			// default value should be 1
			shiftPtr.set(i, shift[i]);
		}
		CLBuffer<Integer>
				inputBuf = context.createIntBuffer(Usage.Input, inputPtr),
				shiftBuf = context.createIntBuffer(Usage.Input, shiftPtr),
				outputBuf = context.createIntBuffer(Usage.Output, input.length);
		CLKernel kernel = program.createKernel("testZFRS_INT", inputBuf, shiftBuf, outputBuf, input.length);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {input.length});
		Pointer<Integer> outputPtr = outputBuf.read(queue, evt);
		for (int i = 0; i < input.length; i++) {
			int
					got = outputPtr.get(i).intValue(),
					expected = input[i] >>> shift[i];
			assertEquals(String.format("Got %d, expected %d", got, expected), expected, got);
		}
	}
	
	/**
	 * Tests whether the ZFRS_CHAR macro (character/byte zero fill right shift) is producing expected results - compares output from OpenCL to Java's results
	 */
	@Test
	public void testMacro_ZFRS_CHAR() {
		byte[] input = {1, 2, -1, 50, -50, Byte.MAX_VALUE, Byte.MIN_VALUE, 0};
		int[] shift = new int[input.length];
		for (int i = 0; i < shift.length; i++)
			shift[i] = 1;
		Pointer<Byte> inputPtr = Pointer.allocateBytes(input.length).order(context.getByteOrder());
		Pointer<Integer> shiftPtr = Pointer.allocateInts(input.length).order(context.getByteOrder());
		for (int i = 0; i < input.length; i++) {
			inputPtr.set(i, input[i]);
			// default value should be 1
			shiftPtr.set(i, shift[i]);
		}
		CLBuffer<Byte>
				inputBuf = context.createByteBuffer(Usage.Input, inputPtr),
				outputBuf = context.createByteBuffer(Usage.Output, input.length);
		CLBuffer<Integer> shiftBuf = context.createIntBuffer(Usage.Input, shiftPtr);
		CLKernel kernel = program.createKernel("testZFRS_CHAR", inputBuf, shiftBuf, outputBuf, input.length);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {input.length});
		Pointer<Byte> outputPtr = outputBuf.read(queue, evt);
		for (int i = 0; i < input.length; i++) {
			byte
					got = outputPtr.get(i),
					expected = (byte) (input[i] >>> shift[i]);
			assertEquals(String.format("Got %d, expected %d for item %d", got, expected, i), expected, got);
		}
	}
	
	/**
	 * Tests whether the RR macro (rotate right) is producing expected results - compares output from OpenCL to Java's results
	 */
	@Test
	public void testMacro_RR() {
		int[] input = {1, 5, -20, -190, 40, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
		int[] dist = new int[input.length];
		for (int i = 0; i < dist.length; i++)
			dist[i] = 1;
		Pointer<Integer>
				inputPtr = Pointer.allocateInts(input.length).order(context.getByteOrder()),
				distPtr = Pointer.allocateInts(input.length).order(context.getByteOrder());
		for (int i = 0; i < input.length; i++) {
			inputPtr.set(i, input[i]);
			// default value should be 1
			distPtr.set(i, dist[i]);
		}
		CLBuffer<Integer>
				inputBuf = context.createIntBuffer(Usage.Input, inputPtr),
				distBuf = context.createIntBuffer(Usage.Input, distPtr),
				outputBuf = context.createIntBuffer(Usage.Output, input.length);
		CLKernel kernel = program.createKernel("testRR", inputBuf, distBuf, outputBuf, input.length);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {input.length});
		Pointer<Integer> outputPtr = outputBuf.read(queue, evt);
		for (int i = 0; i < input.length; i++) {
			int
					got = outputPtr.get(i).intValue(),
					expected = Integer.rotateRight(input[i], dist[i]);
			assertEquals(String.format("Got %d, expected %d", got, expected), expected, got);
		}
	}
	
	/**
	 * Tests whether the RL macro (rotate left) is producing expected results - compares output from OpenCL to Java's results
	 */
	@Test
	public void testMacro_RL() {
		int[] input = {1, 5, -20, -190, 40, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
		int[] dist = new int[input.length];
		for (int i = 0; i < dist.length; i++)
			dist[i] = 1;
		Pointer<Integer>
				inputPtr = Pointer.allocateInts(input.length).order(context.getByteOrder()),
				distPtr = Pointer.allocateInts(input.length).order(context.getByteOrder());
		for (int i = 0; i < input.length; i++) {
			inputPtr.set(i, input[i]);
			// default value should be 1
			distPtr.set(i, dist[i]);
		}
		CLBuffer<Integer>
				inputBuf = context.createIntBuffer(Usage.Input, inputPtr),
				distBuf = context.createIntBuffer(Usage.Input, distPtr),
				outputBuf = context.createIntBuffer(Usage.Output, input.length);
		CLKernel kernel = program.createKernel("testRL", inputBuf, distBuf, outputBuf, input.length);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {input.length});
		Pointer<Integer> outputPtr = outputBuf.read(queue, evt);
		for (int i = 0; i < input.length; i++) {
			int
					got = outputPtr.get(i).intValue(),
					expected = Integer.rotateLeft(input[i], dist[i]);
			assertEquals(String.format("Got %d, expected %d", got, expected), expected, got);
		}
	}
}
