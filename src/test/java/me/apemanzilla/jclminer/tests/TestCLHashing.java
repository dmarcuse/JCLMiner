package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.Test;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;
import com.sci.skristminer.util.SHA256;
import com.sci.skristminer.util.Utils;

public class TestCLHashing extends OpenCLTest {

	@Test
	public void testHashing_messagePadding() {
		// create an input that's 55 characters long (max supported input length)
		String longInput = "";
		for (int i = 0; i < 55; i++) {
			longInput += (char) i;
		}
		String inputs[] = {"", "hello", "hello world", "ThIIs ^Is_ a T3ZT", longInput};
		for (int j = 0; j < inputs.length; j++) {
			String input = inputs[j];
			byte[] inpbytes = Utils.getBytes(input);
			Pointer<Byte> inputPtr = Pointer.allocateBytes(input.length());
			for (int i = 0; i < input.length(); i++) {
				inputPtr.set(i, inpbytes[i]);
			}
			CLBuffer<Byte>
					inputBuf = input.length() > 0 ? context.createByteBuffer(Usage.Input, inputPtr) : context.createByteBuffer(Usage.Input, 1),
					outputBuf = context.createByteBuffer(Usage.Output, 64);
			CLKernel kernel = program.createKernel("testPadding", inputBuf, input.length(), outputBuf);
			CLEvent evt = kernel.enqueueNDRange(queue, new int[] {1});
			Pointer<Byte> outputPtr = outputBuf.read(queue, evt);
			byte[] output = new byte[64];
			for (int i = 0; i < 64; i++) {
				output[i] = outputPtr.get(i);
			}
			byte[] expect = SHA256.padMessage(Utils.getBytes(input));
			for (int i = 0; i < 64; i++) {
				if (output[i] != expect[i]) {
					fail(String.format("Mismatch on byte %d when padding item %d: read byte %d, wanted byte %d", i, j, output[i], expect[i]));
				}
			}
		}
	}

}
