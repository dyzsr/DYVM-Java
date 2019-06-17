long foo(long a, long b, long c, long d, 
		long e, long f, long g, long h) {
	return a + b + c + d + e + f + g + h;
}

long bar(long *p, long *q) {
	return foo(p[0], p[1], p[2], p[3], p[4], p[5], p[6], p[7]) +
		foo(q[0], q[1], q[2], q[3], q[4], q[5], q[6], q[7]);
}
