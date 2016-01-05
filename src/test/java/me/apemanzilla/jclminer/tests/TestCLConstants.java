package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.Test;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.sci.skristminer.util.SHA256;

public class TestCLConstants extends OpenCLTest {

	@Test
	public void testConstant_K() {
		int[] K = SHA256.K;
		CLBuffer<Integer> outputBuf = context.createIntBuffer(Usage.Output, K.length);
		CLKernel kernel = program.createKernel("testK", outputBuf);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {K.length});
		Pointer<Integer> outputPtr = outputBuf.read(queue, evt);
	}

}
