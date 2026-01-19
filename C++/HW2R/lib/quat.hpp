
#pragma once

#include <cmath>
#include <iostream>

template< typename T >
struct matrix_t
{
	T data[16];
};

template< typename T >
struct vector3_t
{
	T x, y, z;
};

template< typename T >
class Quat
{
  private:
	T m_value[4]{ 0, 0, 0, 0 };

  public:
	const T *data() const { return m_value; };

	Quat(T a, T b, T c, T d)
	{
		m_value[0] = b;
		m_value[1] = c;
		m_value[2] = d;
		m_value[3] = a;
	}

	Quat(T angle, bool rad, const vector3_t< T > &arr)
	{
		T shift = 1 / sqrt(arr.x * arr.x + arr.y * arr.y + arr.z * arr.z);
		if (!rad)
			angle = angle * M_PI / 180.0;

		T s = std::sin(angle / 2.0);

		m_value[0] = arr.x * std::sin(angle / 2.0) * shift;
		m_value[1] = arr.y * std::sin(angle / 2.0) * shift;
		m_value[2] = arr.z * std::sin(angle / 2.0) * shift;
		m_value[3] = std::cos(angle / 2.0);
	}

	Quat() = default;

	Quat operator+(const Quat &A) const
	{
		return Quat(m_value[3] + A.m_value[3], m_value[0] + A.m_value[0], m_value[1] + A.m_value[1], m_value[2] + A.m_value[2]);
	}

	Quat &operator+=(const Quat &A)
	{
		for (int i = 0; i < 4; i++)
		{
			m_value[i] = m_value[i] + A.m_value[i];
		}
		return *this;
	}

	Quat operator-(const Quat &A) const
	{
		return Quat(m_value[3] - A.m_value[3], m_value[0] - A.m_value[0], m_value[1] - A.m_value[1], m_value[2] - A.m_value[2]);
	}

	Quat &operator-=(const Quat &A)
	{
		for (int i = 0; i < 4; i++)
		{
			m_value[i] = m_value[i] - A.m_value[i];
		}
		return *this;
	}

	Quat operator~() const { return Quat(m_value[3], -m_value[0], -m_value[1], -m_value[2]); }

	explicit operator T() const
	{
		return sqrt(m_value[0] * m_value[0] + m_value[1] * m_value[1] + m_value[2] * m_value[2] + m_value[3] * m_value[3]);
	}

	bool operator==(const Quat &A) const
	{
		for (int i = 0; i < 4; ++i)
		{
			if (m_value[i] != A.m_value[i])
			{
				return false;
			}
		}
		return true;
	}
	bool operator!=(const Quat &A) const
	{
		for (int i = 0; i < 4; ++i)
		{
			if (m_value[i] != A.m_value[i])
			{
				return true;
			}
		}
		return false;
	}

	Quat operator*(const Quat &A) const
	{
		return Quat(
			m_value[3] * A.m_value[3] - m_value[0] * A.m_value[0] - m_value[1] * A.m_value[1] - m_value[2] * A.m_value[2],
			m_value[3] * A.m_value[0] + m_value[0] * A.m_value[3] + m_value[1] * A.m_value[2] - m_value[2] * A.m_value[1],
			m_value[3] * A.m_value[1] - m_value[0] * A.m_value[2] + m_value[1] * A.m_value[3] + m_value[2] * A.m_value[0],
			m_value[3] * A.m_value[2] + m_value[0] * A.m_value[1] - m_value[1] * A.m_value[0] + m_value[2] * A.m_value[3]);
	}

	Quat operator*(float f) const { return Quat(m_value[3] * f, m_value[0] * f, m_value[1] * f, m_value[2] * f); }

	Quat operator*(const vector3_t< T > &v) const
	{
		return Quat(
			-m_value[0] * v.x - m_value[1] * v.y - m_value[2] * v.z,
			m_value[3] * v.x + m_value[1] * v.z - m_value[2] * v.y,
			m_value[3] * v.y - m_value[0] * v.z + m_value[2] * v.x,
			m_value[3] * v.z + m_value[0] * v.y - m_value[1] * v.x);
	}

	vector3_t< T > apply(const vector3_t< T > &v) const
	{
		Quat ans = *this * v * ~*this;
		const T *arr = ans.data();
		T sqd = (T(~*this) * T(~*this));
		return { arr[0] / sqd, arr[1] / sqd, arr[2] / sqd };
	}

	T angle(bool degrees) const
	{
		T angle_rad = 2 * std::acos(m_value[3]);
		if (degrees)
		{
			return angle_rad;
		}
		else
		{
			return angle_rad * 180.0 / M_PI;
		}
	}

	T angle() const { return 2 * std::acos(m_value[3]); }

	matrix_t< T > return_matrix(T *arr) const
	{
		matrix_t< T > result;

		for (int i = 0; i < 16; i++)
		{
			result.data[i] = arr[i];
		}

		return result;
	}

	matrix_t< T > rotation_matrix() const
	{
		T x = m_value[0] / T(*this);
		T y = m_value[1] / T(*this);
		T z = m_value[2] / T(*this);
		T w = m_value[3] / T(*this);
		matrix_t< T > result;

		T xx = x * x;
		T xy = x * y;
		T xz = x * z;
		T xw = x * w;

		T yy = y * y;
		T yz = y * z;
		T yw = y * w;

		T zz = z * z;
		T zw = z * w;

		T arr[16]{
			1 - 2 * (yy + zz),
			2 * (xy + zw),
			2 * (xz - yw),
			0,
			2 * (xy - zw),
			1 - 2 * (xx + zz),
			2 * (yz + xw),
			0,
			2 * (xz + yw),
			2 * (yz - xw),
			1 - 2 * (xx + yy),
			0,
			0,
			0,
			0,
			1
		};
		return return_matrix(arr);
	}

	matrix_t< T > matrix() const
	{
		T x = m_value[3];
		T y = m_value[0];
		T z = m_value[1];
		T w = m_value[2];

		T arr[16]{ x, -y, -z, -w, y, x, -w, z, z, w, x, -y, w, -z, y, x };
		return return_matrix(arr);
	}
};
