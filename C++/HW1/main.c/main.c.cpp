#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
const uint32_t MER_31 = 2147483647;
uint32_t fact(int32_t n)
{
	int64_t ans = 1;
	for (int32_t i = 2; i <= n; i++)
	{
		ans = (ans * i) % MER_31;
	}
	return ans;
}

uint32_t update_fact(uint16_t i, uint64_t ans)
{
	if (i == (uint16_t)0)
	{
		return 1;
	}
	else
	{
		return (ans * i) % MER_31;
	}
}

int8_t numb_size(int32_t n)
{
	int8_t ans = 0;
	while (n > 0 || ans == 0)
	{
		n /= 10;
		ans++;
	}
	return ans;
}

void print_symbols(char c, int8_t size)
{
	for (int i = 0; i < size; i++)
	{
		printf("%c", c);
	}
}

void out_line(int8_t left, int8_t right)
{
	printf("+");
	print_symbols('-', left + 2);
	printf("+");
	print_symbols('-', right + 2);
	printf("+\n");
}

void print_head(int8_t left, int8_t right, int8_t type)
{
	printf("| ");
	if (type == -1)
	{
		printf("%-*c | %-*s", left, 'n', right, "n!");
	}
	if (type == 0)
	{
		int8_t actual_left = left / 2 + 1, actual_right = (right - 1) / 2 + 2;
		printf("%*c", actual_left, 'n');
		print_symbols(' ', left - actual_left);
		printf(" | ");
		printf("%*s", actual_right, "n!");
		print_symbols(' ', right - actual_right);
	}
	if (type == 1)
	{
		printf("%*c | %*s", left, 'n', right, "n!");
	}
	printf(" |\n");
}

void print_body(int8_t left, int8_t right, int8_t type, uint16_t position, uint32_t ans)
{
	printf("| ");
	if (type == -1)
	{
		printf("%-*d | %-*d", left, position, right, ans);
	}
	if (type == 0)
	{
		int8_t left_size = numb_size(position), right_size = numb_size(ans);
		int8_t actual_left = (left - left_size + 1) / 2 + left_size, actual_right = (right - right_size + 1) / 2 + right_size;
		printf("%*d", actual_left, position);
		print_symbols(' ', left - actual_left);
		printf(" | ");
		printf("%*d", actual_right, ans);
		print_symbols(' ', right - actual_right);
	}
	if (type == 1)
	{
		printf("%*d | %*d", left, position, right, ans);
	}
	printf(" |\n");
}

int main()
{
	int32_t n_start, n_end;
	int8_t align, left_size = 0, right_size = 0, max_size = 2;
	uint32_t ans = 1;
	scanf("%" SCNd32 "%" SCNd32 "%" SCNd8, &n_start, &n_end, &align);
	if (n_start < 0 || n_end < 0 || align < -1 || align > 1)
	{
		fprintf(
			stderr,
			"We find wrong values: %d, %d, %d\n Please check your argument. First and second argument must be more "
			"or equals then 0. Third argument must be in [-1, 1]",
			n_start,
			n_end,
			align);
		return 1;
	}
	if (n_start > n_end)
	{
		left_size = 5;
	}
	else
	{
		left_size = numb_size(n_end);
	}

	ans = fact(n_start - 1);
	uint32_t sup_ans = ans;
	for (uint16_t i = n_start;; i++)
	{
		sup_ans = update_fact(i, sup_ans);
		int8_t cur_size = numb_size(sup_ans);

		if (max_size < cur_size)
		{
			max_size = cur_size;
		}
		if (i == n_end || max_size == 10)
		{
			break;
		}
	}

	right_size += max_size;
	out_line(left_size, right_size);
	print_head(left_size, right_size, align);
	out_line(left_size, right_size);
	for (uint16_t i = n_start;; i++)
	{
		ans = update_fact(i, ans);
		print_body(left_size, right_size, align, i, ans);
		if (i == n_end)
		{
			break;
		}
	}
	out_line(left_size, right_size);
	return 0;
}
