//
// Created by W10 on 29.04.2024.
//

#ifndef LW2_CROSS_COR_H
#define LW2_CROSS_COR_H

#include <fftw3.h>
#include <stdio.h>

int fure_compile(size_t arr_size, size_t arr_size_1, size_t arr_size_2, double *my_arr, fftw_complex **m_arr);

#endif	  // LW2_CROSS_COR_H
