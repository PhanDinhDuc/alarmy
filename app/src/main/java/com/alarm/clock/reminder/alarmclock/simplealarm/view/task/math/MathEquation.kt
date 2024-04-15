package com.alarm.clock.reminder.alarmclock.simplealarm.view.task.math

interface MathEquation {
    fun result(): Long
    fun string(): String
}

enum class Operator {
    SUM, MINUS
}

class EasiestMathEquation : MathEquation {
    private val a: Long = LongRange(start = 0, endInclusive = 10).random()
    private val b: Long = LongRange(start = 0, endInclusive = 10).random()
    private val operator: Operator = Operator.values().random()
    override fun result(): Long {
        return when (operator) {
            Operator.SUM -> a + b
            Operator.MINUS -> if (a > b) a - b else b - a
        }
    }

    override fun string(): String {
        return when (operator) {
            Operator.SUM -> "$a + $b"
            Operator.MINUS -> if (a > b) "$a - $b" else "$b - $a"
        }
    }
}

class EasyMathEquation : MathEquation {
    private val a: Long = LongRange(start = 10, endInclusive = 99).random()
    private val b: Long = LongRange(start = 0, endInclusive = 10).random()
    private val operator: Operator = Operator.values().random()
    override fun result(): Long {
        return when (operator) {
            Operator.SUM -> a + b
            Operator.MINUS -> if (a > b) a - b else b - a
        }
    }

    override fun string(): String {
        return when (operator) {
            Operator.SUM -> "$a + $b"
            Operator.MINUS -> if (a > b) "$a - $b" else "$b - $a"
        }
    }
}

class MediumMathEquation : MathEquation {
    private val a: Long = LongRange(start = 0, endInclusive = 99).random()
    private val b: Long = LongRange(start = 0, endInclusive = 99).random()
    private val c: Long = LongRange(start = 0, endInclusive = 99).random()
    override fun result(): Long {
        return a + b + c
    }

    override fun string(): String {
        return "$a + $b + $c"
    }
}

class HardMathEquation : MathEquation {
    private val a = EasyMathEquation()
    private val c: Long = LongRange(start = 0, endInclusive = 99).random()
    override fun result(): Long {
        return a.result() * c
    }

    override fun string(): String {
        return "(${a.string()}) * $c"
    }
}

class HardestMathEquation : MathEquation {
    private val a = MediumMathEquation()
    private val c: Long = LongRange(start = 0, endInclusive = 999).random()
    override fun result(): Long {
        return a.result() * c
    }

    override fun string(): String {
        return "(${a.string()}) * $c"
    }
}
