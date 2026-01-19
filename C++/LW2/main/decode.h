//
// Created by W10 on 29.04.2024.
//

#ifndef LW2_DECODE_H
#define LW2_DECODE_H

#include <stdio.h>

int decode(char *path, double **my_arr, size_t *arr_size, int max_sample_rate);

int find_sample_rate(char *path, int *sample_rate);

int decode_1_file(char *path, double **my_arr_1, double **my_arr_2, size_t *arr_size, int *sample_rate);

#endif	  // LW2_DECODE_H
