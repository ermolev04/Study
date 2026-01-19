//
// Created by W10 on 29.04.2024.
//

#include "cross_cor.h"

#include "return_codes.h"

#include <fftw3.h>
#include <string.h>

int fure_compile(size_t arr_size, size_t arr_size_1, size_t arr_size_2, double *my_arr, fftw_complex **m_arr)
{
	double *m = (double *)fftw_malloc(arr_size * sizeof(double));
	if (m == NULL)
	{
		fprintf(stderr, "We haven't memory! While trying make fure compose\n");
		return ERROR_NOTENOUGH_MEMORY;
	}

	fftw_complex *arr = fftw_alloc_complex(arr_size * sizeof(fftw_complex));
	if (arr == NULL)
	{
		fprintf(stderr, "We haven't memory! While trying make fure compose.\n");
		return ERROR_NOTENOUGH_MEMORY;
	}
	memcpy(m, my_arr, arr_size_1 * sizeof(double));
	memset(&m[arr_size_1], 0, sizeof(double) * arr_size_2);
	fftw_plan p = fftw_plan_dft_r2c_1d(arr_size, m, arr, FFTW_ESTIMATE);
	if (p == NULL)
	{
		fftw_free(m);
		fprintf(stderr, "We can't create plan.\n");
		return ERROR_UNKNOWN;
	}
	fftw_execute(p);
	*m_arr = arr;
	fftw_free(m);
	//	fftw_free(p);
	fftw_destroy_plan(p);
	return SUCCESS;
}
