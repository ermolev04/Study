#include "return_codes.h"

#include <stdio.h>

const int CONST2_IN30 = 1073741824;
const unsigned long long CONST2_IN63 = 9223372036854775808llu;

typedef struct
{
	char zero, inf, nan, ma, sign;
	int exp;
	unsigned long long mant;
} my_float;

unsigned long long extract(unsigned long long num, int left, int right)
{	 // I take this algorithm from StackOverflow. I like this algorithm, but I can't rewrite it. :(
	int size = (left - right + 1);
	if (size == 32)
	{
		return num;
	}
	unsigned long long ans;
	int mask = ((1 << size) - 1) << right;
	ans = num & mask;
	ans = ans >> right;
	return ans;
}

void to_bin(unsigned long long a, int size)
{
	unsigned long long bin = 1;
	1ull << size;
	while (bin > 0)
	{
		printf("%llu", (a / bin));
		a %= bin;
		bin /= 2;
	}
	printf("\n");
}

unsigned long long find_main_part(unsigned long long ans, int stp)
{
	unsigned long long d = CONST2_IN63;
	unsigned short step = 63;
	unsigned long long umn, del;
	while (ans / d == 0 && d > 1)
	{
		d /= 2;
		step--;
	}
	ans = ans % d;
	del = 1ull << (step - stp);
	umn = 1ull << (step - stp);
	return (ans * umn) / del;
}

int find_r(unsigned long long ans, int stp)
{
	unsigned long long d = CONST2_IN63;
	unsigned short step = 63 - stp;
	unsigned long long del = 0;
	while (ans / d == 0 && d > 0)
	{
		d /= 2;
		step--;
	}
	if (step >= 0)
	{
		del = 1llu << step;
	}
	if (del == 0 || ans % del == 0)
	{
		return 0;
	}
	if (ans % del < del / 2)
	{
		return 1;
	}
	if (ans % del == del / 2)
	{
		return 2;
	}
	return 3;
}

void to_hex(unsigned long long a, int sz)	 // I dont like use "%x", because he start translate to Hex right to left,
											 // but I want left to right.
{
	unsigned long long bin = 1ull << (4 * sz);
	while (bin > 0)
	{
		printf("%llx", (a / bin));
		a %= bin;
		bin /= 16;
	}
}

void print_zero(int sz)
{
	printf("0x0.%p", 0);
	to_hex(0, sz);
	printf("p+0");
}

void to_ans(my_float ans, long long fix, int stp)
{
	if (ans.nan)
	{
		printf("nan");
		return;
	}
	if (ans.sign)
	{
		printf("-");
	}
	if (ans.inf)
	{
		printf("inf");
		return;
	}
	if (ans.zero)
	{
		print_zero((stp + 3) / 4);
		return;
	}
	printf("0x%i.", ans.ma);
	printf("%.((stp + 3) / 4)x", ans.mant * fix);
	//	to_hex(ans.mant * fix, (stp + 3) / 4);
	printf("p%+i", ans.exp);
}

int norm_exp(unsigned long long mant, int stp)
{
	int ans = 0;
	while (mant > 0)
	{
		mant /= 2;
		ans++;
	}
	return stp - ans;
}

unsigned long long add_ma(int stp)
{
	long long ans = 1;
	while (stp > 0)
	{
		ans *= 2;
		stp--;
	}
	return ans;
}

unsigned long long norm(unsigned long long a, int stp)
{
	unsigned long long ans = a;
	while (stp > 0)
	{
		stp--;
		ans *= 2;
	}
	while (stp < 0)
	{
		stp++;
		ans /= 2;
	}
	return ans;
}

unsigned long long update_mant(unsigned long long mant, int step)
{
	unsigned long long ans = mant;
	while (step > 0)
	{
		ans /= 2;
		step--;
	}
	while (step < 0)
	{
		ans *= 2;
		step++;
	}
	return ans;
}

int exp_sdv(unsigned long long ans, int stp)
{
	unsigned long long div = add_ma(63);
	int step = 63;
	while (ans / div == 0)
	{
		div /= 2;
		step--;
	}
	step -= 2 * stp;
	if (step > 0)
	{
		return step;
	}
	return 0;
}

my_float normalise(my_float fl, int stp, int type, int shift)
{
	my_float ans;
	ans.sign = fl.sign;
	ans.zero = fl.zero;
	ans.inf = fl.inf;
	ans.nan = fl.nan;
	ans.ma = 1;
	ans.exp = fl.exp;
	ans.mant = fl.mant;
	if (fl.exp == -shift)
	{
		if (fl.mant == 0)
		{
			ans.zero = 1;
			ans.exp = 0;
			ans.ma = 0;
			ans.mant = 0;
			return ans;
		}
		else
		{
			ans.exp -= norm_exp(fl.mant, stp);
			ans.mant = find_main_part(fl.mant, stp);
			fl.ma = 0;
			ans.ma = 1;
		}
	}
	if (fl.exp == shift + 1)
	{
		if (fl.mant == 0)
		{
			ans.inf = 1;
			return ans;
		}
		else
		{
			ans.nan = 1;
			return ans;
		}
	}
	ans.mant = find_main_part(fl.mant + (fl.ma * add_ma(stp)), stp);
	ans.ma = 1;
	unsigned long long r = find_r(fl.mant, stp);
	switch (type)
	{
	case 0:
		if (fl.exp > shift + stp)
		{
			ans.inf = 1;
			return ans;
		}
		if (fl.exp < -shift - stp)
		{
			ans.zero = 1;
			return ans;
		}
		return ans;
	case 1:
		if (ans.exp > shift + 1)
		{
			ans.inf = 1;
			return ans;
		}
		if (ans.exp < -shift - stp)
		{
			ans.zero = 1;
			return ans;
		}
		switch (r)
		{
		case 0:
		case 1:
			break;
		case 2:
			if (ans.mant % 2 != 0)
			{
				ans.mant++;
			}
			break;
		case 3:
			ans.mant++;
			break;
		}
		return ans;
	case 2:
		if (fl.exp > shift + 1)
		{
			if (fl.sign % 2)
			{
				ans.exp = -shift - stp + 1;
				ans.mant = 0;
				ans.ma = 1;
				return ans;
			}
			ans.inf = 1;
			return ans;
		}
		if (fl.exp < -shift)
		{
			if (fl.sign % 2)
			{
				ans.zero = 1;
				return ans;
			}
			ans.mant = 0;
			ans.ma = 1;
			ans.exp = -shift - stp + 1;
			return ans;
		}
		if (!(fl.sign % 2) && r > 0)
		{
			ans.mant++;
		}
		return ans;
	case 3:
		if (fl.exp > shift + 1)
		{
			ans.inf = 1;
			return ans;
		}
		if (fl.exp < -shift)
		{
			if (fl.sign % 2)
			{
				ans.ma = 1;
				ans.exp = -shift - stp + 1;
				ans.mant = 0;
				return ans;
			}
			ans.zero = 1;
			return ans;
		}
		if (((fl.sign) % 2) && r > 0)
		{
			ans.mant++;
		}
		return ans;
	default:
		return ans;
	}
}

int main(int argc, char *argv[])
{
	short type;	   // I don't want write char, because I afraid what '0' -> 48
	unsigned int num_1, num_2;
	char pre, op;

	int size, dub, stp, shift, fix;

	if (argc != 4 && argc != 6)
	{
		fprintf(stderr, "We find wrong number values: %i.\n Please check your argument. We expect 4 or 6 arguments", argc);
		return ERROR_ARGUMENTS_INVALID;
	}
	if (!sscanf(argv[1], "%c", &pre))
	{
		fprintf(stderr, "We cant read your argument.\n Please check your argument.");
		return ERROR_ARGUMENTS_INVALID;
	}
	switch (pre)
	{
	case 'f':
		size = 32;
		dub = 31;
		stp = 23;
		shift = 127;
		fix = 2;
		break;
	case 'h':
		size = 16;
		dub = 15;
		stp = 10;
		shift = 15;
		fix = 4;
		break;
	default:
		fprintf(stderr, "We find wrong values: %c.\n Please check your argument. On first position we can read only \"f\" and \"h\"", pre);
		return ERROR_ARGUMENTS_INVALID;
	}
	if (!sscanf(argv[2], "%i", &type))
	{
		fprintf(stderr, "We cant read your argument.\n Please check your argument.");
		return ERROR_ARGUMENTS_INVALID;
	}

	if (type > 3 || type < 0)
	{
		fprintf(stderr, "We find wrong values: %i.\n Please check your argument. On second position we can read int [0 - 3]", type);
		return ERROR_ARGUMENTS_INVALID;
	}
	if (!sscanf(argv[3], "%x", &num_1))
	{
		fprintf(stderr, "We cant read your argument.\n Please check your argument.");
		return ERROR_ARGUMENTS_INVALID;
	}
	if (argc == 6)
	{
		if (!sscanf(argv[4], "%c", &op))
		{
			fprintf(stderr, "We cant read your argument.\n Please check your argument.");
			return ERROR_ARGUMENTS_INVALID;
		}
		if (!sscanf(argv[5], "%x", &num_2))
		{
			fprintf(stderr, "We cant read your argument.\n Please check your argument.");
			return ERROR_ARGUMENTS_INVALID;
		}
	}

	my_float fl_1 = {
		0,
		0,
		0,
		1,
		(char)extract(num_1, size - 1, dub),
		(int)extract(num_1, dub - 1, stp) - shift,
		extract(num_1, stp - 1, 0)
	};
	fl_1 = normalise(fl_1, stp, type, shift);

	if (argc == 4)
	{
		to_ans(fl_1, fix, stp);
		return SUCCESS;
	}
	my_float fl_2 = {
		0,
		0,
		0,
		1,
		(char)extract(num_2, size - 1, dub),
		(int)extract(num_2, dub - 1, stp) - shift,
		extract(num_2, stp - 1, 0)
	};
	fl_2 = normalise(fl_2, stp, type, shift);

	if (fl_1.nan || fl_2.nan)
	{
		printf("nan");
		return SUCCESS;
	}
	my_float ans = { 0, 0, 0, 1, 0, 0, 0 };
	fl_1.mant += fl_1.ma * add_ma(stp);
	fl_1.ma = 0;
	fl_2.mant += fl_2.ma * add_ma(stp);
	fl_2.ma = 0;
	unsigned long long new_mant_2;
	switch (op)
	{
	case '+':
		if ((fl_1.inf && fl_2.inf) && (fl_1.sign != fl_2.sign))
		{
			ans.nan = 1;
		}
		ans.sign = fl_1.sign;
		if (fl_1.zero && fl_2.zero)
		{
			ans.sign = 0;
			print_zero((stp + 3) / 4);
			return SUCCESS;
		}
		if (fl_2.inf || fl_1.inf)
		{
			ans.inf = 1;
			to_ans(ans, fix, stp);
			return SUCCESS;
		}
		int max_exp = fl_2.exp, min_exp = fl_1.exp;
		unsigned long long max_mant = fl_2.mant, min_mant = fl_1.mant;
		if (fl_1.exp >= fl_2.exp)
		{
			max_exp = fl_1.exp;
			min_exp = fl_2.exp;
			max_mant = fl_1.mant;
			min_mant = fl_2.mant;
		}
		ans.exp = max_exp + 1;
		ans.mant = (max_mant * CONST2_IN30) + norm(min_mant, 30 - max_exp + min_exp);
		ans.ma = 0;

		ans = normalise(ans, stp, type, shift);
		to_ans(ans, fix, stp);
		break;
	case '-':
		if (fl_1.inf && fl_2.inf)
		{
			if (fl_1.sign != fl_2.sign)
			{
				printf("nan");
			}
			else
			{
				if (fl_1.sign)
				{
					printf("-");
				}
				printf("inf");
			}
			return SUCCESS;
		}
		if (fl_1.zero && fl_2.zero)
		{
			print_zero((stp + 3) / 4);
			return SUCCESS;
		}
		ans.exp = fl_1.exp > fl_2.exp ? fl_1.exp : fl_2.exp;
		ans.exp -= 30;
		ans.mant = fl_1.mant * CONST2_IN30;
		new_mant_2 = update_mant(fl_2.mant, fl_1.exp - fl_2.exp);
		ans.mant += new_mant_2;
		normalise(ans, stp, type, shift);
		break;
	case '*':
		if ((fl_1.inf && fl_2.zero) || (fl_1.zero && fl_2.inf))
		{
			ans.nan = 1;
		}
		ans.sign = fl_1.sign ^ fl_2.sign;
		if (fl_1.zero || fl_2.zero)
		{
			ans.zero = 1;
		}
		if (fl_1.inf || fl_2.inf)
		{
			ans.inf = 1;
		}
		ans.mant = fl_1.mant * fl_2.mant;
		ans.ma = 0;
		ans.exp = fl_1.exp + fl_2.exp + exp_sdv(ans.mant, stp);
		ans = normalise(ans, stp, type, shift);
		to_ans(ans, fix, stp);
		break;
	case '/':
		if ((fl_1.zero && fl_2.zero) || (fl_1.inf && fl_2.inf))
		{
			ans.nan = 1;
		}
		ans.sign = fl_1.sign ^ fl_2.sign;
		if (fl_1.zero || fl_2.inf)
		{
			ans.zero = 1;
		}
		if (fl_2.zero || fl_1.inf)
		{
			ans.inf = 1;
			to_ans(ans, fix, stp);
			return SUCCESS;
		}
		ans.mant = (fl_1.mant * CONST2_IN30) / fl_2.mant;
		ans.ma = 0;
		ans.exp = fl_1.exp - fl_2.exp;
		ans = normalise(ans, stp, type, shift);
		to_ans(ans, fix, stp);
		break;
	default:
		fprintf(stderr,
				"We find wrong values: %c.\n Please check your argument. On fourth position we can read only "
				"\"+\", \"-\", \"*\" and \"/\"",
				op);
		return ERROR_ARGUMENTS_INVALID;
	}
}
