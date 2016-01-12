typedef uint4 vuint;

#define vRR(x, y) rotate((vuint)x, (vuint)y)

#define vCH(x, y, z) bitselect(z, y, x)
#define vMAJ(x, y, z) bitselect(x, y, z ^ x)
#define vEP0(x) (vRR(x,2) ^ vRR(x,13) ^ vRR(x,22))
#define vEP1(x) (vRR(x,6) ^ vRR(x,11) ^ vRR(x,25))
#define vSIG0(x) (vRR(x,7) ^ vRR(x,18) ^ (x >> 3))
#define vSIG1(x) (vRR(x,17) ^ vRR(x,19) ^ (x >> 10))

__constant vuint vK[64] = {
					0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
					0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
					0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
					0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
					0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
					0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
					0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
					0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

// vectorized SHA256 digest function
void vdigest(vuint* padded, vuint* hashed) {
	/* init vars */
	vuint h0, h1, h2, h3, h4, h5, h6, h7;
	vuint a, b, c, d, e, f, g, h, t1, t2, m[64] = {(vuint)0};
	uint i, l;
	/* init hash state */
	h0 = 0x6a09e667;
	h1 = 0xbb67ae85;
	h2 = 0x3c6ef372;
	h3 = 0xa54ff53a;
	h4 = 0x510e527f;
	h5 = 0x9b05688c;
	h6 = 0x1f83d9ab;
	h7 = 0x5be0cd19;
	/* transform */
#pragma unroll
	for (i = 0; i < 16; i++) {
		m[i] = padded[i*4] << 24
				| padded[i*4+1] << 16
				| padded[i*4+2] << 8
				| padded[i*4+3];
	}
#pragma unroll
	for (i = 16; i < 64; i++) {
		m[i] = vSIG1(m[i-2]) + m[i-7] + vSIG0(m[i-15]) + m[i-16];
	}
	a = h0;
	b = h1;
	c = h2;
	d = h3;
	e = h4;
	f = h5;
	g = h6;
	h = h7;
#pragma unroll
	for (i = 0; i < 64; i++) {
		t1 = h + vEP1(e) + vCH(e,f,g) + vK[i] + m[i];
		t2 = vEP0(a) + vMAJ(a,b,c);
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
	/* finish */
#pragma unroll
	for (i = 0; i < 4; i++) {
		l = i * -8 + 24;
		hashed[i]			= (h0 >> l) & 0xFF;
		hashed[i + 4] 	= (h1 >> l) & 0xFF;
		hashed[i + 8] 	= (h2 >> l) & 0xFF;
		hashed[i + 12]	= (h3 >> l) & 0xFF;
		hashed[i + 16]	= (h4 >> l) & 0xFF;
		hashed[i + 20]	= (h5 >> l) & 0xFF;
		hashed[i + 24]	= (h6 >> l) & 0xFF;
		hashed[i + 28]	= (h7 >> l) & 0xFF;
	}
}
