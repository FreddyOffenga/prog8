package prog8tests

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import prog8.ast.Module
import prog8.ast.Program
import prog8.ast.base.DataType
import prog8.ast.base.Position
import prog8.ast.base.VarDeclType
import prog8.ast.expressions.ArrayIndexedExpression
import prog8.ast.expressions.IdentifierReference
import prog8.ast.expressions.NumericLiteral
import prog8.ast.expressions.PrefixExpression
import prog8.ast.statements.*
import prog8.codegen.target.C64Target
import prog8.compiler.printProgram
import prog8.compilerinterface.isIOAddress
import prog8.parser.SourceCode
import prog8tests.helpers.*


class TestMemory: FunSpec({

    val c64target = C64Target()

    fun wrapWithProgram(statements: List<Statement>): Program {
        val program = Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), statements.toMutableList(), false, Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        program.addModule(module)
        return program
    }

    test("assignment target not in mapped IO space C64") {

        var memexpr = NumericLiteral.optimalInteger(0x0002, Position.DUMMY)
        var target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        var assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0x1000, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0x9fff, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0xa000, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0xc000, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0xcfff, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0xeeee, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = NumericLiteral.optimalInteger(0xffff, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false
    }

    test("assign target in mapped IO space C64") {

        var memexpr = NumericLiteral.optimalInteger(0x0000, Position.DUMMY)
        var target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        var assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe true

        memexpr = NumericLiteral.optimalInteger(0x0001, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe true

        memexpr = NumericLiteral.optimalInteger(0xd000, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe true

        memexpr = NumericLiteral.optimalInteger(0xdfff, Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe true
    }

    fun createTestProgramForMemoryRefViaVar(address: UInt, vartype: VarDeclType): AssignTarget {
        val decl = VarDecl(vartype, VarDeclOrigin.USERCODE, DataType.BYTE, ZeropageWish.DONTCARE, null, "address", NumericLiteral.optimalInteger(address, Position.DUMMY), false, false, null, Position.DUMMY)
        val memexpr = IdentifierReference(listOf("address"), Position.DUMMY)
        val target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(decl, assignment))
        return target
    }

    test("identifier mapped to IO memory on C64") {
        var target = createTestProgramForMemoryRefViaVar(0x1000u, VarDeclType.VAR)
        target.isIOAddress(c64target.machine) shouldBe false
        target = createTestProgramForMemoryRefViaVar(0xd020u, VarDeclType.VAR)
        target.isIOAddress(c64target.machine) shouldBe false
        target = createTestProgramForMemoryRefViaVar(0x1000u, VarDeclType.CONST)
        target.isIOAddress(c64target.machine) shouldBe false
        target = createTestProgramForMemoryRefViaVar(0xd020u, VarDeclType.CONST)
        target.isIOAddress(c64target.machine) shouldBe true
        target = createTestProgramForMemoryRefViaVar(0x1000u, VarDeclType.MEMORY)
        target.isIOAddress(c64target.machine) shouldBe false
        target = createTestProgramForMemoryRefViaVar(0xd020u, VarDeclType.MEMORY)
        target.isIOAddress(c64target.machine) shouldBe true
    }

    test("memory expression mapped to IO memory on C64") {
        var memexpr = PrefixExpression("+", NumericLiteral.optimalInteger(0x1000, Position.DUMMY), Position.DUMMY)
        var target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        var assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        target.isIOAddress(c64target.machine) shouldBe false

        memexpr = PrefixExpression("+", NumericLiteral.optimalInteger(0xd020, Position.DUMMY), Position.DUMMY)
        target = AssignTarget(null, null, DirectMemoryWrite(memexpr, Position.DUMMY), Position.DUMMY)
        assign = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        wrapWithProgram(listOf(assign))
        printProgram(target.definingModule.program)
        target.isIOAddress(c64target.machine) shouldBe true
    }

    test("regular variable not in mapped IO ram on C64") {
        val decl = VarDecl(VarDeclType.VAR, VarDeclOrigin.USERCODE, DataType.BYTE, ZeropageWish.DONTCARE, null, "address", null, false, false, null, Position.DUMMY)
        val target = AssignTarget(IdentifierReference(listOf("address"), Position.DUMMY), null, null, Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), emptyList(), emptyList(), emptySet(), null, false, false, mutableListOf(decl, assignment), Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
            .addModule(module)
        target.isIOAddress(c64target.machine) shouldBe false
    }

    test("memory mapped variable not in mapped IO ram on C64") {
        val address = 0x1000u
        val decl = VarDecl(VarDeclType.MEMORY, VarDeclOrigin.USERCODE, DataType.UBYTE, ZeropageWish.DONTCARE, null, "address", NumericLiteral.optimalInteger(address, Position.DUMMY), false, false, null, Position.DUMMY)
        val target = AssignTarget(IdentifierReference(listOf("address"), Position.DUMMY), null, null, Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), emptyList(), emptyList(), emptySet(), null, false, false, mutableListOf(decl, assignment), Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
            .addModule(module)
        target.isIOAddress(c64target.machine) shouldBe false
    }

    test("memory mapped variable in mapped IO ram on C64") {
        val address = 0xd020u
        val decl = VarDecl(VarDeclType.MEMORY, VarDeclOrigin.USERCODE, DataType.UBYTE, ZeropageWish.DONTCARE, null, "address", NumericLiteral.optimalInteger(address, Position.DUMMY), false, false, null, Position.DUMMY)
        val target = AssignTarget(IdentifierReference(listOf("address"), Position.DUMMY), null, null, Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), emptyList(), emptyList(), emptySet(), null, false, false, mutableListOf(decl, assignment), Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
            .addModule(module)
        target.isIOAddress(c64target.machine) shouldBe true
    }

    test("array not in mapped IO ram") {
        val decl = VarDecl(VarDeclType.VAR, VarDeclOrigin.USERCODE, DataType.ARRAY_UB, ZeropageWish.DONTCARE, null, "address", null, false, false, null, Position.DUMMY)
        val arrayindexed = ArrayIndexedExpression(IdentifierReference(listOf("address"), Position.DUMMY), ArrayIndex(NumericLiteral.optimalInteger(1, Position.DUMMY), Position.DUMMY), Position.DUMMY)
        val target = AssignTarget(null, arrayindexed, null, Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), emptyList(), emptyList(), emptySet(), null, false, false, mutableListOf(decl, assignment), Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
            .addModule(module)
        target.isIOAddress(c64target.machine) shouldBe false
    }

    test("memory mapped array not in mapped IO ram") {
        val address = 0x1000u
        val decl = VarDecl(VarDeclType.MEMORY, VarDeclOrigin.USERCODE, DataType.ARRAY_UB, ZeropageWish.DONTCARE, null, "address", NumericLiteral.optimalInteger(address, Position.DUMMY), false, false, null, Position.DUMMY)
        val arrayindexed = ArrayIndexedExpression(IdentifierReference(listOf("address"), Position.DUMMY), ArrayIndex(NumericLiteral.optimalInteger(1, Position.DUMMY), Position.DUMMY), Position.DUMMY)
        val target = AssignTarget(null, arrayindexed, null, Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), emptyList(), emptyList(), emptySet(), null, false, false, mutableListOf(decl, assignment), Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
            .addModule(module)
        target.isIOAddress(c64target.machine) shouldBe false
    }

    test("memory mapped array in mapped IO ram") {
        val address = 0xd800u
        val decl = VarDecl(VarDeclType.MEMORY, VarDeclOrigin.USERCODE, DataType.ARRAY_UB, ZeropageWish.DONTCARE, null, "address", NumericLiteral.optimalInteger(address, Position.DUMMY), false, false, null, Position.DUMMY)
        val arrayindexed = ArrayIndexedExpression(IdentifierReference(listOf("address"), Position.DUMMY), ArrayIndex(NumericLiteral.optimalInteger(1, Position.DUMMY), Position.DUMMY), Position.DUMMY)
        val target = AssignTarget(null, arrayindexed, null, Position.DUMMY)
        val assignment = Assignment(target, NumericLiteral.optimalInteger(0, Position.DUMMY), AssignmentOrigin.USERCODE, Position.DUMMY)
        val subroutine = Subroutine("test", mutableListOf(), emptyList(), emptyList(), emptyList(), emptySet(), null, false, false, mutableListOf(decl, assignment), Position.DUMMY)
        val module = Module(mutableListOf(subroutine), Position.DUMMY, SourceCode.Generated("test"))
        Program("test", DummyFunctions, DummyMemsizer, DummyStringEncoder)
            .addModule(module)
        target.isIOAddress(c64target.machine) shouldBe true
    }


    test("memory() with spaces in name works") {
        compileText(C64Target(), false, """
            main {
                sub start() {
                    uword @shared mem = memory("a b c", 100, $100)
                }
            }
        """, writeAssembly = true).assertSuccess()
    }

    test("memory() with invalid argument") {
        compileText(C64Target(), false, """
            main {
                sub start() {
                    uword @shared mem1 = memory("abc", 100, -2)
                }
            }
        """, writeAssembly = true).assertFailure()
    }
})
