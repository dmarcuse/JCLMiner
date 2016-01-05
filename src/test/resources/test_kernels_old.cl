// This file contains the unit testing kernels
// It should not be included when compiling for mining

// This is only for testing compilation of the CL code - doesn't actually do anything
__kernel void testCompile(){}

// THIS IS NOT FULLY TESTED AND ALMOST DEFINITELY DOES NOT WORK PROPERLY!!
// DO NOT EXPECT USEFUL RESULTS!
__kernel void testHash(__global const uchar* input, int origLength, __global uchar* output) {
	if(get_global_id(0) == 0) {
		// pad input
		uchar padded[64];
		int lengthInBits = origLength * 8;
		int lm1 = sizeof(padded) - 1;
		padded[lm1] = (lengthInBits & 0xFF);
		padded[lm1 - 1] = ((lengthInBits >> 8) & 0xFF);
		padded[lm1 - 2] = ((lengthInBits >> 16) & 0xFF);
		padded[lm1 - 3] = ((lengthInBits >> 24) & 0xFF);
		padded[lm1 - 4] = ((lengthInBits >> 32) & 0xFF);
		padded[lm1 - 5] = ((lengthInBits >> 40) & 0xFF);
		padded[lm1 - 6] = ((lengthInBits >> 48) & 0xFF);
		padded[lm1 - 7] = ((lengthInBits >> 56) & 0xFF);
		padded[origLength] = 0x80;
		for (int i = 0; i < origLength; i++){
			padded[i] = input[i];
		}
		// done padding
		uchar hashed[32];
		int h0 = 0x6a09e667;
		int h1 = 0xbb67ae85;
		int h2 = 0x3c6ef372;
		int h3 = 0xa54ff53a;
		int h4 = 0x510e527f;
		int h5 = 0x9b05688c;
		int h6 = 0x1f83d9ab;
		int h7 = 0x5be0cd19;
	
		int pl64 = sizeof(padded) / 64;
		int i, j, sa, sb, j4;
		int a, b, c, d, e, f, g, h, s0, s1, maj, t1, t2, ch, i64;
		int words[64];
	
		for (i = 0; i < pl64; i++) {
			a = h0;
			b = h1;
			c = h2;
			d = h3;
			e = h4;
			f = h5;
			g = h6;
			h = h7;
			i64 = i * 64;
	
			for (j = 0; j < 16; j++)
			{
				j4 = j * 4 + i64;
				words[j] |= ((padded[j4] & 0x000000FF) << 24);
				words[j] |= ((padded[j4 + 1] & 0x000000FF) << 16);
				words[j] |= ((padded[j4 + 2] & 0x000000FF) << 8);
				words[j] |= (padded[j4 + 3] & 0x000000FF);
			}
	
			for (j = 16; j < 64; j++)
			{
				sa = words[j - 15];
				sb = words[j - 2];
				s0 = rr(sa, 7) ^ rr(sa, 18) ^ (sa >> 3);
				s1 = rr(sb, 17) ^ rr(sb, 19) ^ (sb >> 10);
				words[j] = words[j - 16] + s0 + words[j - 7] + s1;
			}
	
			for (j = 0; j < 64; j++)
			{
				s0 = rr(a, 2) ^ rr(a, 13) ^ rr(a, 22);
				maj = (a & b) ^ (a & c) ^ (b & c);
				t2 = s0 + maj;
				s1 = rr(e, 6) ^ rr(e, 11) ^ rr(e, 25);
				ch = (e & f) ^ (~e & g);
				t1 = h + s1 + ch + K[j] + words[j];
	
				h = g;
				g = f;
				f = e;
				e = d + t1;
				d = c;
				c = b;
				b = a;
				a = t1 + t2;
			}
	
			h0 += a;
			h1 += b;
			h2 += c;
			h3 += d;
			h4 += e;
			h5 += f;
			h6 += g;
			h7 += h;
		}
	
		hashed[0] = ((h0 >> 56) & 0xff);
		hashed[1] = ((h0 >> 48) & 0xff);
		hashed[2] = ((h0 >> 40) & 0xff);
		hashed[3] = ((h0 >> 32) & 0xff);
	
		hashed[4] = ((h1 >> 56) & 0xff);
		hashed[5] = ((h1 >> 48) & 0xff);
		hashed[6] = ((h1 >> 40) & 0xff);
		hashed[7] = ((h1 >> 32) & 0xff);
	
		hashed[8] = ((h2 >> 56) & 0xff);
		hashed[9] = ((h2 >> 48) & 0xff);
		hashed[10] = ((h2 >> 40) & 0xff);
		hashed[11] = ((h2 >> 32) & 0xff);
	
		hashed[12] = ((h3 >> 56) & 0xff);
		hashed[13] = ((h3 >> 48) & 0xff);
		hashed[14] = ((h3 >> 40) & 0xff);
		hashed[15] = ((h3 >> 32) & 0xff);
	
		hashed[16] = ((h4 >> 56) & 0xff);
		hashed[17] = ((h4 >> 48) & 0xff);
		hashed[18] = ((h4 >> 40) & 0xff);
		hashed[19] = ((h4 >> 32) & 0xff);
	
		hashed[20] = ((h5 >> 56) & 0xff);
		hashed[21] = ((h5 >> 48) & 0xff);
		hashed[22] = ((h5 >> 40) & 0xff);
		hashed[23] = ((h5 >> 32) & 0xff);
	
		hashed[24] = ((h6 >> 56) & 0xff);
		hashed[25] = ((h6 >> 48) & 0xff);
		hashed[26] = ((h6 >> 40) & 0xff);
		hashed[27] = ((h6 >> 32) & 0xff);
	
		hashed[28] = ((h7 >> 56) & 0xff);
		hashed[29] = ((h7 >> 48) & 0xff);
		hashed[30] = ((h7 >> 40) & 0xff);
		hashed[31] = ((h7 >> 32) & 0xff);
		for(int i = 0; i < sizeof(hashed); i++) {
			output[i] = hashed[i];
		}
	}
}