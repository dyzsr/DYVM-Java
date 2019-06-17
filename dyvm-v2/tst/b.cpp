#include <bits/stdc++.h>
using namespace std;

int main() {
	string s = "aaa bbb ccc 123 456";
	stringstream ss(s);
	string a, b, c;
	int d, e;
	ss >> a;
	ss >> b;
	ss >> c;
	ss >> d;
	ss >> e;
	cout << a << ' ' << b << ' ' << c << ' ' << d << ' ' << e << '\n';
	return 0;
}
