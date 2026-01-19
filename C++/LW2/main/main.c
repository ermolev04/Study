
#include "cross_cor.h"
#include "decode.h"
#include "return_codes.h"
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>

#include <fftw3.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

int main(int argc, char *argv[])
{
	av_log_set_level(AV_LOG_QUIET);
	if (argc < 2 || argc > 3)
	{
		fprintf(stderr, "We expect one or two input file, but we have: %i\n", argc);
		return ERROR_ARGUMENTS_INVALID;
	}

	size_t arr_size = 0, arr_size_1 = 0, arr_size_2 = 0;
	int res = 0;
	double *my_arr_1, *my_arr_2;
	int sample_rate_max = 0;
	if (argc == 3)
	{
		int sample_rate_2 = 0;
		find_sample_rate(argv[1], &sample_rate_max);
		find_sample_rate(argv[2], &sample_rate_2);
		if (sample_rate_2 > sample_rate_max)
		{
			sample_rate_max = sample_rate_2;
		}
		int my_res_1 = decode(argv[1], &my_arr_1, &arr_size_1, sample_rate_max);
		if (my_res_1)
		{
			return my_res_1;
		}
		int my_res_2 = decode(argv[2], &my_arr_2, &arr_size_2, sample_rate_max);
		if (my_res_2)
		{
			free(my_arr_1);
			return my_res_2;
		}
		arr_size = arr_size_1 + arr_size_2;
	}
	else
	{
		res = decode_1_file(argv[1], &my_arr_1, &my_arr_2, &arr_size, &sample_rate_max);
		if (res)
		{
			return res;
		}
		arr_size_1 = arr_size;
		arr_size_2 = arr_size;
		arr_size *= 2;
	}

	fftw_complex *arr_1;
	fftw_complex *arr_2;
	fure_compile(arr_size, arr_size_1, arr_size_2, my_arr_1, &arr_1);
	free(my_arr_1);
	fure_compile(arr_size, arr_size_2, arr_size_1, my_arr_2, &arr_2);
	free(my_arr_2);

	fftw_complex *arr = fftw_malloc(arr_size * sizeof(fftw_complex));
	fftw_complex *ans = fftw_malloc(arr_size * sizeof(fftw_complex));

	for (size_t i = 0; i < arr_size; i++)
	{
		arr[i][0] = (arr_1[i][0] * arr_2[i][0]) / arr_size;
	}
	fftw_free(arr_1);
	fftw_free(arr_2);
	fftw_plan p = fftw_plan_dft_1d(arr_size, arr, ans, FFTW_FORWARD, FFTW_ESTIMATE);
	fftw_execute(p);
	fftw_destroy_plan(p);

	int index = 0;
	double max = 0;
	if (arr_size > 0)
	{
		max = ans[0][0];
	}
	else
	{
		fprintf(stderr, "We didn't expect empty file.\n");
		fftw_free(arr);
		fftw_free(ans);
		return ERROR_DATA_INVALID;
	}

	for (size_t i = 1; i < arr_size; i++)
	{
		if (ans[i][0] - max > 0.000001)
		{
			max = ans[i][0];
			index = i;
		}
	}
	if (arr_size_1 < arr_size_2)
	{
		index *= -1;
	}
	printf("delta: %i samples\nsample rate: %i Hz\ndelta time: %i ms\n", index, sample_rate_max, index * 1000 / sample_rate_max);
	fftw_free(arr);
	fftw_free(ans);
	return SUCCESS;
}
