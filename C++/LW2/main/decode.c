//
// Created by W10 on 29.04.2024.
//

#include "decode.h"

#include "return_codes.h"
#include <libavcodec/avcodec.h>
#include <libavformat/avformat.h>
#include <libavutil/avutil.h>
#include <libavutil/opt.h>
#include <libswresample/swresample.h>

#include <stdio.h>

void clear_data(AVFormatContext *formatCtx, AVCodecContext *codecCtx, AVPacket *packet, AVFrame *frame, SwrContext *context, double *audio_buffer)
{
	if (formatCtx != NULL)
	{
		avformat_close_input(&formatCtx);
	}
	if (codecCtx != NULL)
	{
		avcodec_free_context(&codecCtx);
	}
	if (packet != NULL)
	{
		av_packet_free(&packet);
	}
	if (frame != NULL)
	{
		av_frame_free(&frame);
	}
	if (context != NULL)
	{
		swr_free(&context);
	}
	if (audio_buffer != NULL)
	{
		free(audio_buffer);
	}
}

const int TWO_ON_FIFTEENTH = 32768;

int print_error(int res, char *path)
{
	switch (res)
	{
	case AVERROR(EINVAL):
		fprintf(stderr, "Error: We have wrong argument in file: %s\n", path);
		return ERROR_DATA_INVALID;
	case AVERROR(EIO):
		fprintf(stderr, "Error: We can't open file: %s\n", path);
		return ERROR_DATA_INVALID;
	case AVERROR(ENOMEM):
		fprintf(stderr, "Error: We haven't memory\n");
		return ERROR_NOTENOUGH_MEMORY;
	case AVERROR(EAGAIN):
		fprintf(stderr, "Error: We haven't resource\n");
		return ERROR_UNKNOWN;
	case AVERROR(ENOENT):
		fprintf(stderr, "Error: We haven't find resource\n");
		return ERROR_UNKNOWN;
	case AVERROR(EPERM):
		fprintf(stderr, "Error: We haven't permutation\n");
		return ERROR_UNKNOWN;
	default:
		fprintf(stderr, "Error: Failed to open input file: %s\n", path);
		return ERROR_CANNOT_OPEN_FILE;
	}
}

int decode(char *path, double **my_arr, size_t *arr_size, int max_sample_rate)
{
	AVFormatContext *formatCtx = NULL;
	int res = avformat_open_input(&formatCtx, path, NULL, NULL);
	if (res != 0)
	{
		return print_error(res, path);
	}
	res = avformat_find_stream_info(formatCtx, NULL);
	if (res != 0)
	{
		avformat_close_input(&formatCtx);
		return print_error(res, path);
	}
	int audioStreamIndex = -1;
	for (size_t i = 0; i < formatCtx->nb_streams; i++)
	{
		if (formatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			audioStreamIndex = i;
			break;
		}
	}

	if (audioStreamIndex == -1)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: No audio stream found in the input file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVCodecParameters *codecParams = formatCtx->streams[audioStreamIndex]->codecpar;
	int type = codecParams->codec_id;
	if (type != AV_CODEC_ID_FLAC && type != AV_CODEC_ID_MP2 && type != AV_CODEC_ID_MP3 && type != AV_CODEC_ID_OPUS && type != AV_CODEC_ID_AAC)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't parse audio format in the input file: %s\n", path);
		return ERROR_UNSUPPORTED;
	}
	const AVCodec *codec = avcodec_find_decoder(codecParams->codec_id);
	if (codec == NULL)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't find right codec for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVCodecContext *codecCtx = avcodec_alloc_context3(codec);
	if (codecCtx == NULL)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't find right codec context for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	res = avcodec_parameters_to_context(codecCtx, codecParams);
	if (res != 0)
	{
		return print_error(res, path);
	}
	res = avcodec_open2(codecCtx, codec, NULL);
	if (res < 0)
	{
		clear_data(formatCtx, codecCtx, NULL, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't open right codec for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVPacket *packet = av_packet_alloc();
	if (packet == NULL)
	{
		clear_data(formatCtx, codecCtx, NULL, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't alloc packet");
		return ERROR_NOTENOUGH_MEMORY;
	}
	AVFrame *frame = av_frame_alloc();
	if (frame == NULL)
	{
		clear_data(formatCtx, codecCtx, packet, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't alloc frame");
		return ERROR_NOTENOUGH_MEMORY;
	}
	double *audio_buffer;
	int data_index = 0;
	int data_size = TWO_ON_FIFTEENTH;

	audio_buffer = malloc(data_size * sizeof(double));
	if (!audio_buffer)
	{
		fprintf(stderr, "Error: Failed to allocate memory for audio buffer\n");
		clear_data(formatCtx, codecCtx, packet, frame, NULL, audio_buffer);
		return ERROR_NOTENOUGH_MEMORY;
	}
	SwrContext *context = NULL;
	res = swr_alloc_set_opts2(
		&context,
		&(AVChannelLayout)AV_CHANNEL_LAYOUT_MONO,
		AV_SAMPLE_FMT_DBLP,
		max_sample_rate,
		&codecCtx->ch_layout,
		codecCtx->sample_fmt,
		codecParams->sample_rate,
		0,
		NULL);
	if (res != 0)
	{
		clear_data(formatCtx, codecCtx, packet, frame, NULL, audio_buffer);
		return print_error(res, path);
	}
	res = swr_init(context);
	if (res != 0)
	{
		clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
		return print_error(res, path);
	}
	double *buf;
	while (av_read_frame(formatCtx, packet) >= 0)
	{
		if (packet->stream_index == audioStreamIndex)
		{
			res = avcodec_send_packet(codecCtx, packet);
			if (res != 0)
			{
				clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
				return print_error(res, path);
			}
			int ret = avcodec_receive_frame(codecCtx, frame);
			if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF || ret < 0)
			{
				fprintf(stderr, "Error during decoding\n");
				clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
				return ERROR_DATA_INVALID;
			}
			while (ret == 0)
			{
				res = av_samples_alloc((uint8_t **)&buf, NULL, codecParams->ch_layout.nb_channels, frame->nb_samples, AV_SAMPLE_FMT_DBLP, 0);
				if (res < 0)
				{
					clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
					fprintf(stderr, "Error during decoding, we need more memory\n");
					return ERROR_NOTENOUGH_MEMORY;
				}
				if (swr_convert(context, (uint8_t **)&buf, frame->nb_samples, (const uint8_t **)frame->data, frame->nb_samples) < 0)
				{
					fprintf(stderr, "Error during decoding\n");
					clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
					return ERROR_DATA_INVALID;
				}
				if (data_index + frame->nb_samples > data_size)
				{
					double *update_audio_buffer = realloc(audio_buffer, data_size * 10 * sizeof(double));
					if (update_audio_buffer == NULL)
					{
						fprintf(stderr, "Error during decoding, we need more memory\n");
						clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
						return ERROR_NOTENOUGH_MEMORY;
					}
					data_size *= 2;
					audio_buffer = update_audio_buffer;
				}

				memcpy(&audio_buffer[data_index], buf, frame->nb_samples * sizeof(double));
				data_index += frame->nb_samples;
				int size = av_get_bytes_per_sample(codecCtx->sample_fmt);
				av_frame_unref(frame);
				ret = avcodec_receive_frame(codecCtx, frame);
				if (ret != AVERROR_EOF && ret != AVERROR(EAGAIN) && (ret < 0))
				{
					fprintf(stderr, "Error during decoding\n");
					clear_data(formatCtx, codecCtx, packet, frame, context, audio_buffer);
					return ERROR_DATA_INVALID;
				}
			}
		}
		av_packet_unref(packet);
	}
	*my_arr = audio_buffer;
	*arr_size = data_index;
	clear_data(formatCtx, codecCtx, packet, frame, context, NULL);

	return SUCCESS;
}

int find_sample_rate(char *path, int *sample_rate)
{
	AVFormatContext *formatCtx = NULL;
	int res = avformat_open_input(&formatCtx, path, NULL, NULL);
	if (res != 0)
	{
		return print_error(res, path);
	}
	res = avformat_find_stream_info(formatCtx, NULL);
	if (res != 0)
	{
		avformat_close_input(&formatCtx);
		return print_error(res, path);
	}
	int audioStreamIndex = -1;
	for (int i = 0; i < formatCtx->nb_streams; i++)
	{
		if (formatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			audioStreamIndex = i;
			break;
		}
	}

	if (audioStreamIndex == -1)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: No audio stream found in the input file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVCodecParameters *codecParams = formatCtx->streams[audioStreamIndex]->codecpar;
	int type = codecParams->codec_id;
	if (type != AV_CODEC_ID_FLAC && type != AV_CODEC_ID_MP2 && type != AV_CODEC_ID_MP3 && type != AV_CODEC_ID_OPUS && type != AV_CODEC_ID_AAC)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't parse audio format in the input file: %s\n", path);
		return ERROR_UNSUPPORTED;
	}
	const AVCodec *codec = avcodec_find_decoder(codecParams->codec_id);
	if (codec == NULL)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't find right codec for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVCodecContext *codecCtx = avcodec_alloc_context3(codec);
	if (codecCtx == NULL)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't find right codec context for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	res = avcodec_parameters_to_context(codecCtx, codecParams);
	if (res != 0)
	{
		return print_error(res, path);
	}
	res = avcodec_open2(codecCtx, codec, NULL);
	if (res < 0)
	{
		clear_data(formatCtx, codecCtx, NULL, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't open right codec for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}

	*sample_rate = codecParams->sample_rate;
	avcodec_free_context(&codecCtx);
	avformat_close_input(&formatCtx);
	return SUCCESS;
}

int decode_1_file(char *path, double **my_arr_1, double **my_arr_2, size_t *arr_size, int *sample_rate)
{
	AVFormatContext *formatCtx = NULL;
	int res = avformat_open_input(&formatCtx, path, NULL, NULL);
	if (res != 0)
	{
		return print_error(res, path);
	}
	res = avformat_find_stream_info(formatCtx, NULL);
	if (res != 0)
	{
		avformat_close_input(&formatCtx);
		return print_error(res, path);
	}
	int audioStreamIndex = -1;
	for (int i = 0; i < formatCtx->nb_streams; i++)
	{
		if (formatCtx->streams[i]->codecpar->codec_type == AVMEDIA_TYPE_AUDIO)
		{
			audioStreamIndex = i;
			break;
		}
	}
	if (audioStreamIndex == -1)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: No audio stream found in the input file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVCodecParameters *codecParams = formatCtx->streams[audioStreamIndex]->codecpar;
	if (codecParams->ch_layout.nb_channels != 2)
	{
		fprintf(stderr, "Error: We expect two audio channels in file: %s\nBut we have: %i", path, codecParams->ch_layout.nb_channels);
		return ERROR_FORMAT_INVALID;
	}
	int type = codecParams->codec_id;
	if (type != AV_CODEC_ID_FLAC && type != AV_CODEC_ID_MP2 && type != AV_CODEC_ID_MP3 && type != AV_CODEC_ID_OPUS && type != AV_CODEC_ID_AAC)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't parse audio format in the input file: %s\n", path);
		return ERROR_UNSUPPORTED;
	}
	const AVCodec *codec = avcodec_find_decoder(codecParams->codec_id);
	if (codec == NULL)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't find right codec for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	AVCodecContext *codecCtx = avcodec_alloc_context3(codec);
	if (codecCtx == NULL)
	{
		avformat_close_input(&formatCtx);
		fprintf(stderr, "Error: We can't find right codec context for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	res = avcodec_parameters_to_context(codecCtx, codecParams);
	if (res != 0)
	{
		return print_error(res, path);
	}
	res = avcodec_open2(codecCtx, codec, NULL);
	if (res < 0)
	{
		clear_data(formatCtx, codecCtx, NULL, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't open right codec for your file: %s\n", path);
		return ERROR_FORMAT_INVALID;
	}
	*sample_rate = codecParams->sample_rate;
	AVPacket *packet = av_packet_alloc();
	if (packet == NULL)
	{
		clear_data(formatCtx, codecCtx, NULL, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't alloc packet");
		return ERROR_NOTENOUGH_MEMORY;
	}
	AVFrame *frame = av_frame_alloc();
	if (frame == NULL)
	{
		clear_data(formatCtx, codecCtx, packet, NULL, NULL, NULL);
		fprintf(stderr, "Error: We can't alloc frame");
		return ERROR_NOTENOUGH_MEMORY;
	}
	double *audio_buffer_1, *audio_buffer_2;
	int data_index_1 = 0, data_index_2 = 0;
	int data_size_1 = TWO_ON_FIFTEENTH, data_size_2 = TWO_ON_FIFTEENTH;

	audio_buffer_1 = malloc(data_size_1 * sizeof(double));
	audio_buffer_2 = malloc(data_size_2 * sizeof(double));
	if (!audio_buffer_1 || !audio_buffer_2)
	{
		fprintf(stderr, "Error: Failed to allocate memory for audio buffer\n");
		clear_data(formatCtx, codecCtx, packet, frame, NULL, audio_buffer_1);
		free(audio_buffer_2);
		return ERROR_NOTENOUGH_MEMORY;
	}

	SwrContext *context_1 = NULL, *context_2 = NULL;
	res = swr_alloc_set_opts2(
		&context_1,
		&codecCtx->ch_layout,
		AV_SAMPLE_FMT_DBLP,
		codecParams->sample_rate,
		&codecCtx->ch_layout,
		codecCtx->sample_fmt,
		codecParams->sample_rate,
		0,
		NULL);
	if (res != 0)
	{
		clear_data(formatCtx, codecCtx, packet, frame, NULL, audio_buffer_1);
		free(audio_buffer_2);
		return print_error(res, path);
	}
	res = swr_alloc_set_opts2(
		&context_2,
		&codecCtx->ch_layout,
		AV_SAMPLE_FMT_DBLP,
		codecParams->sample_rate,
		&codecCtx->ch_layout,
		codecCtx->sample_fmt,
		codecParams->sample_rate,
		0,
		NULL);
	if (res != 0)
	{
		clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
		free(audio_buffer_2);
		return print_error(res, path);
	}
	res = swr_init(context_1);
	if (res != 0)
	{
		clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
		swr_free(&context_2);
		free(audio_buffer_2);
		return print_error(res, path);
	}
	res = swr_init(context_2);
	if (res != 0)
	{
		clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
		swr_free(&context_2);
		free(audio_buffer_2);
		return print_error(res, path);
	}
	int j = 0;
	while (av_read_frame(formatCtx, packet) >= 0)
	{
		if (packet->stream_index == audioStreamIndex)
		{
			res = avcodec_send_packet(codecCtx, packet);
			if (res != 0)
			{
				clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
				swr_free(&context_2);
				free(audio_buffer_2);
				return print_error(res, path);
			}
			int ret = avcodec_receive_frame(codecCtx, frame);
			if (ret == AVERROR(EAGAIN) || ret == AVERROR_EOF || ret < 0)
			{
				fprintf(stderr, "Error during decoding\n");
				clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
				free(audio_buffer_2);
				swr_free(&context_2);
				return ERROR_DATA_INVALID;
			}
			int buf_size = TWO_ON_FIFTEENTH;
			double *buf_1 = malloc(sizeof(double) * buf_size), *buf_2 = malloc(sizeof(double) * buf_size);
			while (ret == 0)
			{
				if (frame->nb_samples > buf_size)
				{
					double *update_buf = realloc(buf_1, frame->nb_samples * 2 * sizeof(double));
					if (update_buf == NULL)
					{
						fprintf(stderr, "Error during decoding, we need more memory\n");
						clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
						free(audio_buffer_2);
						swr_free(&context_2);
						return ERROR_NOTENOUGH_MEMORY;
					}
					buf_size = frame->nb_samples * 2;
					buf_1 = update_buf;
					update_buf = realloc(buf_2, frame->nb_samples * 2 * sizeof(double));
					if (update_buf == NULL)
					{
						fprintf(stderr, "Error during decoding, we need more memory\n");
						clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
						free(audio_buffer_2);
						swr_free(&context_2);
						return ERROR_NOTENOUGH_MEMORY;
					}
					buf_2 = update_buf;
				}

				for (int i = 0; i < frame->nb_samples; ++i)
				{
					buf_1[i] = frame->data[0][i];
					buf_2[i] = frame->data[1][i];
					j++;
				}
				//                I left it. Suddenly it will seem better to you
				//				memcpy(buf_1, frame->data[0], frame->nb_samples * sizeof(double));
				//				memcpy(buf_2, frame->data[1], frame->nb_samples * sizeof(double));
				//                av_samples_alloc(&buf_1, NULL, frame->channels, frame->nb_samples, AV_SAMPLE_FMT_DBLP,
				//                0); av_samples_alloc(&buf_2, NULL, frame->channels, frame->nb_samples,
				//                AV_SAMPLE_FMT_DBLP, 0); swr_convert(context_1, (uint8_t**) &buf_1, frame->nb_samples,
				//                frame->data[0], frame->nb_samples) >= 0; swr_convert(context_2, (uint8_t**) &buf_2,
				//                frame->nb_samples, frame->data[1], frame->nb_samples) >= 0;
				if (data_index_1 + frame->nb_samples > data_size_1)
				{
					double *update_audio_buffer = realloc(audio_buffer_1, data_size_1 * 2 * sizeof(double));
					if (update_audio_buffer != NULL)
					{
						data_size_1 *= 2;
						audio_buffer_1 = update_audio_buffer;
					}
				}
				memcpy(&audio_buffer_1[data_index_1], buf_1, frame->nb_samples * sizeof(double));
				data_index_1 += frame->nb_samples;
				if (data_index_2 + frame->nb_samples > data_size_2)
				{
					double *update_audio_buffer = realloc(audio_buffer_2, data_size_2 * 2 * sizeof(double));
					if (update_audio_buffer == NULL)
					{
						fprintf(stderr, "Error during decoding, we need more memory\n");
						clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
						free(audio_buffer_2);
						swr_free(&context_2);
						return ERROR_NOTENOUGH_MEMORY;
					}
					data_size_2 *= 2;
					audio_buffer_2 = update_audio_buffer;
				}
				memcpy(&audio_buffer_2[data_index_2], buf_2, frame->nb_samples * sizeof(double));
				data_index_2 += frame->nb_samples;
				int size = av_get_bytes_per_sample(codecCtx->sample_fmt);
				av_frame_unref(frame);
				ret = avcodec_receive_frame(codecCtx, frame);
				if (ret != AVERROR_EOF && ret != AVERROR(EAGAIN) && (ret < 0))
				{
					fprintf(stderr, "Error during decoding\n");
					clear_data(formatCtx, codecCtx, packet, frame, context_1, audio_buffer_1);
					free(audio_buffer_2);
					swr_free(&context_2);
					return ERROR_DATA_INVALID;
				}
			}
			free(buf_1);
			free(buf_2);
		}
		av_packet_unref(packet);
	}

	*my_arr_1 = audio_buffer_1;
	*my_arr_2 = audio_buffer_2;
	*arr_size = data_index_1;
	clear_data(formatCtx, codecCtx, packet, frame, context_1, NULL);
	swr_free(&context_2);
	return SUCCESS;
}
