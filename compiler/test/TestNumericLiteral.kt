package prog8tests

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import prog8.ast.base.DataType
import prog8.ast.base.ExpressionError
import prog8.ast.base.Position
import prog8.ast.expressions.ArrayLiteral
import prog8.ast.expressions.InferredTypes
import prog8.ast.expressions.NumericLiteral
import prog8.ast.expressions.StringLiteral
import prog8.compilerinterface.Encoding

class TestNumericLiteral: FunSpec({

    fun sameValueAndType(lv1: NumericLiteral, lv2: NumericLiteral): Boolean {
        return lv1.type==lv2.type && lv1==lv2
    }

    val dummyPos = Position("test", 0, 0, 0)

    test("testIdentity") {
        val v = NumericLiteral(DataType.UWORD, 12345.0, dummyPos)
        (v==v) shouldBe true
        (v != v) shouldBe false
        (v <= v) shouldBe true
        (v >= v) shouldBe true
        (v < v ) shouldBe false
        (v > v ) shouldBe false

        sameValueAndType(NumericLiteral(DataType.UWORD, 12345.0, dummyPos), NumericLiteral(DataType.UWORD, 12345.0, dummyPos)) shouldBe true
    }

    test("test rounding") {
        shouldThrow<ExpressionError> {
            NumericLiteral(DataType.BYTE, -2.345, dummyPos)
        }.message shouldContain "refused rounding"
        shouldThrow<ExpressionError> {
            NumericLiteral(DataType.BYTE, -2.6, dummyPos)
        }.message shouldContain "refused rounding"
        shouldThrow<ExpressionError> {
            NumericLiteral(DataType.UWORD, 2222.345, dummyPos)
        }.message shouldContain "refused rounding"
        NumericLiteral(DataType.UBYTE, 2.0, dummyPos).number shouldBe 2.0
        NumericLiteral(DataType.BYTE, -2.0, dummyPos).number shouldBe -2.0
        NumericLiteral(DataType.UWORD, 2222.0, dummyPos).number shouldBe 2222.0
        NumericLiteral(DataType.FLOAT, 123.456, dummyPos)
    }

    test("testEqualsAndNotEquals") {
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) == NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) == NumericLiteral(DataType.UWORD, 100.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) == NumericLiteral(DataType.FLOAT, 100.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) == NumericLiteral(DataType.UBYTE, 254.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 12345.0, dummyPos) == NumericLiteral(DataType.UWORD, 12345.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 12345.0, dummyPos) == NumericLiteral(DataType.FLOAT, 12345.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) == NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 22239.0, dummyPos) == NumericLiteral(DataType.UWORD, 22239.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 9.99, dummyPos) == NumericLiteral(DataType.FLOAT, 9.99, dummyPos)) shouldBe true

        sameValueAndType(NumericLiteral(DataType.UBYTE, 100.0, dummyPos), NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe true
        sameValueAndType(NumericLiteral(DataType.UBYTE, 100.0, dummyPos), NumericLiteral(DataType.UWORD, 100.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UBYTE, 100.0, dummyPos), NumericLiteral(DataType.FLOAT, 100.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UWORD, 254.0, dummyPos), NumericLiteral(DataType.UBYTE, 254.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UWORD, 12345.0, dummyPos), NumericLiteral(DataType.UWORD, 12345.0, dummyPos)) shouldBe true
        sameValueAndType(NumericLiteral(DataType.UWORD, 12345.0, dummyPos), NumericLiteral(DataType.FLOAT, 12345.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.FLOAT, 100.0, dummyPos), NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.FLOAT, 22239.0, dummyPos), NumericLiteral(DataType.UWORD, 22239.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.FLOAT, 9.99, dummyPos), NumericLiteral(DataType.FLOAT, 9.99, dummyPos)) shouldBe true

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) != NumericLiteral(DataType.UBYTE, 101.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) != NumericLiteral(DataType.UWORD, 101.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) != NumericLiteral(DataType.FLOAT, 101.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 245.0, dummyPos) != NumericLiteral(DataType.UBYTE, 246.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 12345.0, dummyPos) != NumericLiteral(DataType.UWORD, 12346.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 12345.0, dummyPos) != NumericLiteral(DataType.FLOAT, 12346.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 9.99, dummyPos) != NumericLiteral(DataType.UBYTE, 9.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 9.99, dummyPos) != NumericLiteral(DataType.UWORD, 9.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 9.99, dummyPos) != NumericLiteral(DataType.FLOAT, 9.0, dummyPos)) shouldBe true

        sameValueAndType(NumericLiteral(DataType.UBYTE, 100.0, dummyPos), NumericLiteral(DataType.UBYTE, 101.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UBYTE, 100.0, dummyPos), NumericLiteral(DataType.UWORD, 101.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UBYTE, 100.0, dummyPos), NumericLiteral(DataType.FLOAT, 101.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UWORD, 245.0, dummyPos), NumericLiteral(DataType.UBYTE, 246.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UWORD, 12345.0, dummyPos), NumericLiteral(DataType.UWORD, 12346.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.UWORD, 12345.0, dummyPos), NumericLiteral(DataType.FLOAT, 12346.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.FLOAT, 9.99, dummyPos), NumericLiteral(DataType.UBYTE, 9.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.FLOAT, 9.99, dummyPos), NumericLiteral(DataType.UWORD, 9.0, dummyPos)) shouldBe false
        sameValueAndType(NumericLiteral(DataType.FLOAT, 9.99, dummyPos), NumericLiteral(DataType.FLOAT, 9.0, dummyPos)) shouldBe false


    }

    test("testEqualsRef") {
        (StringLiteral("hello", Encoding.PETSCII, dummyPos) == StringLiteral("hello", Encoding.PETSCII, dummyPos)) shouldBe true
        (StringLiteral("hello", Encoding.PETSCII, dummyPos) != StringLiteral("bye", Encoding.PETSCII, dummyPos)) shouldBe true
        (StringLiteral("hello", Encoding.SCREENCODES, dummyPos) == StringLiteral("hello", Encoding.SCREENCODES, dummyPos)) shouldBe true
        (StringLiteral("hello", Encoding.SCREENCODES, dummyPos) != StringLiteral("bye", Encoding.SCREENCODES, dummyPos)) shouldBe true
        (StringLiteral("hello", Encoding.SCREENCODES, dummyPos) != StringLiteral("hello", Encoding.PETSCII, dummyPos)) shouldBe true

        val lvOne = NumericLiteral(DataType.UBYTE, 1.0, dummyPos)
        val lvTwo = NumericLiteral(DataType.UBYTE, 2.0, dummyPos)
        val lvThree = NumericLiteral(DataType.UBYTE, 3.0, dummyPos)
        val lvOneR = NumericLiteral(DataType.UBYTE, 1.0, dummyPos)
        val lvTwoR = NumericLiteral(DataType.UBYTE, 2.0, dummyPos)
        val lvThreeR = NumericLiteral(DataType.UBYTE, 3.0, dummyPos)
        val lvFour= NumericLiteral(DataType.UBYTE, 4.0, dummyPos)
        val lv1 = ArrayLiteral(InferredTypes.InferredType.known(DataType.ARRAY_UB), arrayOf(lvOne, lvTwo, lvThree), dummyPos)
        val lv2 = ArrayLiteral(InferredTypes.InferredType.known(DataType.ARRAY_UB), arrayOf(lvOneR, lvTwoR, lvThreeR), dummyPos)
        val lv3 = ArrayLiteral(InferredTypes.InferredType.known(DataType.ARRAY_UB), arrayOf(lvOneR, lvTwoR, lvFour), dummyPos)
        lv1 shouldBe lv2
        lv1 shouldNotBe lv3
    }

    test("testGreaterThan") {
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) > NumericLiteral(DataType.UBYTE, 99.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) > NumericLiteral(DataType.UWORD, 253.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) > NumericLiteral(DataType.FLOAT, 99.9, dummyPos)) shouldBe true

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) >= NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) >= NumericLiteral(DataType.UWORD, 254.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) >= NumericLiteral(DataType.FLOAT, 100.0, dummyPos)) shouldBe true

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) > NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) > NumericLiteral(DataType.UWORD, 254.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) > NumericLiteral(DataType.FLOAT, 100.0, dummyPos)) shouldBe false

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) >= NumericLiteral(DataType.UBYTE, 101.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) >= NumericLiteral(DataType.UWORD, 255.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) >= NumericLiteral(DataType.FLOAT, 100.1, dummyPos)) shouldBe false
    }

    test("testLessThan") {
        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) < NumericLiteral(DataType.UBYTE, 101.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) < NumericLiteral(DataType.UWORD, 255.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) < NumericLiteral(DataType.FLOAT, 100.1, dummyPos)) shouldBe true

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) <= NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) <= NumericLiteral(DataType.UWORD, 254.0, dummyPos)) shouldBe true
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) <= NumericLiteral(DataType.FLOAT, 100.0, dummyPos)) shouldBe true

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) < NumericLiteral(DataType.UBYTE, 100.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) < NumericLiteral(DataType.UWORD, 254.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) < NumericLiteral(DataType.FLOAT, 100.0, dummyPos)) shouldBe false

        (NumericLiteral(DataType.UBYTE, 100.0, dummyPos) <= NumericLiteral(DataType.UBYTE, 99.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.UWORD, 254.0, dummyPos) <= NumericLiteral(DataType.UWORD, 253.0, dummyPos)) shouldBe false
        (NumericLiteral(DataType.FLOAT, 100.0, dummyPos) <= NumericLiteral(DataType.FLOAT, 99.9, dummyPos)) shouldBe false
    }

})
