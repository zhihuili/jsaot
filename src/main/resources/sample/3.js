function f(x) {
	if (x <= 1)
		return x;
	return x * f(x - 1);
}
var x = 7.2 + 8;
var y = "7" + 8;
x = x + y;
var z = 5;
var m = fw(z);
function fw(z) {
	var x = 1;
	while (z > 1) {
		x += z;
		z--;
	}
	return x;
}
