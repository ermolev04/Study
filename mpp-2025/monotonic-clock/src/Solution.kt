/**
 * В теле класса решения разрешено использовать только переменные делегированные в класс RegularInt.
 * Нельзя volatile, нельзя другие типы, нельзя блокировки, нельзя лазить в глобальные переменные.
 *
 * @author :TODO: Ermolev Mihail
 */
class Solution : MonotonicClock {
    private var b1 by RegularInt(0)
    private var b2 by RegularInt(0)
    private var b3 by RegularInt(0)

    private var c1 by RegularInt(0)
    private var c2 by RegularInt(0)
    private var c3 by RegularInt(0)

    override fun write(time: Time) {
        c1 = time.d1
        c2 = time.d2
        c3 = time.d3

        b3 = c3
        b2 = c2
        b1 = c1
    }

    override fun read(): Time {
        val curB1 = b1
        val curB2 = b2
        val curB3 = b3

        val curC3 = c3
        val curC2 = c2
        val curC1 = c1

        val d1 = curC1
        val d2 = if (curB1 == curC1) curC2 else 0
        val d3 = if (curB1 == curC1 && curB2 == curC2) curC3 else 0

        return Time(d1, d2, d3)
    }
}