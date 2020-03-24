package prog8.ast.processing

import prog8.ast.*
import prog8.ast.base.DataType
import prog8.ast.base.FatalAstException
import prog8.ast.base.NumericDatatypes
import prog8.ast.base.VarDeclType
import prog8.ast.expressions.*
import prog8.ast.statements.*


internal class StatementReorderer(private val program: Program): IAstModifyingVisitor {
    // Reorders the statements in a way the compiler needs.
    // - 'main' block must be the very first statement UNLESS it has an address set.
    // - blocks are ordered by address, where blocks without address are put at the end.
    // - in every scope:
    //      -- the directives '%output', '%launcher', '%zeropage', '%zpreserved', '%address' and '%option' will come first.
    //      -- all vardecls then follow.
    //      -- the remaining statements then follow in their original order.
    //
    // - the 'start' subroutine in the 'main' block will be moved to the top immediately following the directives.
    // - all other subroutines will be moved to the end of their block.
    // - sorts the choices in when statement.
    // - a vardecl with a non-const initializer value is split into a regular vardecl and an assignment statement.

    private val directivesToMove = setOf("%output", "%launcher", "%zeropage", "%zpreserved", "%address", "%option")

    private val addVardecls = mutableMapOf<INameScope, MutableList<VarDecl>>()

    override fun visit(module: Module) {
        addVardecls.clear()
        super.visit(module)

        val (blocks, other) = module.statements.partition { it is Block }
        module.statements = other.asSequence().plus(blocks.sortedBy { (it as Block).address ?: Int.MAX_VALUE }).toMutableList()

        // make sure user-defined blocks come BEFORE library blocks, and move the "main" block to the top of everything
        val nonLibraryBlocks = module.statements.withIndex()
                .filter { it.value is Block && !(it.value as Block).isInLibrary }
                .map { it.index to it.value }
                .reversed()
        for(nonLibBlock in nonLibraryBlocks)
            module.statements.removeAt(nonLibBlock.first)
        for(nonLibBlock in nonLibraryBlocks)
            module.statements.add(0, nonLibBlock.second)
        val mainBlock = module.statements.singleOrNull { it is Block && it.name=="main" }
        if(mainBlock!=null && (mainBlock as Block).address==null) {
            module.remove(mainBlock)
            module.statements.add(0, mainBlock)
        }

        val varDecls = module.statements.filterIsInstance<VarDecl>()
        module.statements.removeAll(varDecls)
        module.statements.addAll(0, varDecls)

        val directives = module.statements.filter {it is Directive && it.directive in directivesToMove}
        module.statements.removeAll(directives)
        module.statements.addAll(0, directives)

        for((where, decls) in addVardecls) {
            where.statements.addAll(0, decls)
            decls.forEach { it.linkParents(where as Node) }
        }
    }

    override fun visit(block: Block): Statement {

        val subroutines = block.statements.filterIsInstance<Subroutine>()
        var numSubroutinesAtEnd = 0
        // move all subroutines to the end of the block
        for (subroutine in subroutines) {
            if(subroutine.name!="start" || block.name!="main") {
                block.remove(subroutine)
                block.statements.add(subroutine)
            }
            numSubroutinesAtEnd++
        }
        // move the "start" subroutine to the top
        if(block.name=="main") {
            block.statements.singleOrNull { it is Subroutine && it.name == "start" } ?.let {
                block.remove(it)
                block.statements.add(0, it)
                numSubroutinesAtEnd--
            }
        }

        val varDecls = block.statements.filterIsInstance<VarDecl>()
        block.statements.removeAll(varDecls)
        block.statements.addAll(0, varDecls)
        val directives = block.statements.filter {it is Directive && it.directive in directivesToMove}
        block.statements.removeAll(directives)
        block.statements.addAll(0, directives)
        block.linkParents(block.parent)

        return super.visit(block)
    }

    override fun visit(subroutine: Subroutine): Statement {
        super.visit(subroutine)

        val varDecls = subroutine.statements.filterIsInstance<VarDecl>()
        subroutine.statements.removeAll(varDecls)
        subroutine.statements.addAll(0, varDecls)
        val directives = subroutine.statements.filter {it is Directive && it.directive in directivesToMove}
        subroutine.statements.removeAll(directives)
        subroutine.statements.addAll(0, directives)

        return subroutine
    }


    private fun addVarDecl(scope: INameScope, variable: VarDecl): VarDecl {
        if(scope !in addVardecls)
            addVardecls[scope] = mutableListOf()
        val declList = addVardecls.getValue(scope)
        val existing = declList.singleOrNull { it.name==variable.name }
        return if(existing!=null) {
            existing
        } else {
            declList.add(variable)
            variable
        }
    }

    override fun visit(decl: VarDecl): Statement {
        val declValue = decl.value
        if(declValue!=null && decl.type== VarDeclType.VAR && decl.datatype in NumericDatatypes) {
            val declConstValue = declValue.constValue(program)
            if(declConstValue==null) {
                // move the vardecl (without value) to the scope and replace this with a regular assignment
                val target = AssignTarget(null, IdentifierReference(listOf(decl.name), decl.position), null, null, decl.position)
                val assign = Assignment(target, null, declValue, decl.position)
                assign.linkParents(decl.parent)
                decl.value = null
                addVarDecl(decl.definingScope(), decl)
                return assign
            }
        }
        return super.visit(decl)
    }

    override fun visit(assignment: Assignment): Statement {
        val assg = super.visit(assignment)
        if(assg !is Assignment)
            return assg

        // see if a typecast is needed to convert the value's type into the proper target type
        val valueItype = assg.value.inferType(program)
        val targetItype = assg.target.inferType(program, assg)

        if(targetItype.isKnown && valueItype.isKnown) {
            val targettype = targetItype.typeOrElse(DataType.STRUCT)
            val valuetype = valueItype.typeOrElse(DataType.STRUCT)

            // struct assignments will be flattened (if it's not a struct literal)
            if (valuetype == DataType.STRUCT && targettype == DataType.STRUCT) {
                val assignments = if (assg.value is StructLiteralValue) {
                    flattenStructAssignmentFromStructLiteral(assg, program)    //  'structvar = { ..... } '
                } else {
                    flattenStructAssignmentFromIdentifier(assg, program)    //   'structvar1 = structvar2'
                }
                return if (assignments.isEmpty()) {
                    // something went wrong (probably incompatible struct types)
                    // we'll get an error later from the AstChecker
                    assg
                } else {
                    val scope = AnonymousScope(assignments.toMutableList(), assg.position)
                    scope.linkParents(assg.parent)
                    scope
                }
            }
        }

        if(assg.aug_op!=null) {
            // transform augmented assg into normal assg so we have one case less to deal with later
            val newTarget: Expression =
                    when {
                        assg.target.register != null -> RegisterExpr(assg.target.register!!, assg.target.position)
                        assg.target.identifier != null -> assg.target.identifier!!
                        assg.target.arrayindexed != null -> assg.target.arrayindexed!!
                        assg.target.memoryAddress != null -> DirectMemoryRead(assg.target.memoryAddress!!.addressExpression, assg.value.position)
                        else -> throw FatalAstException("strange assg")
                    }

            val expression = BinaryExpression(newTarget, assg.aug_op.substringBeforeLast('='), assg.value, assg.position)
            expression.linkParents(assg.parent)
            val convertedAssignment = Assignment(assg.target, null, expression, assg.position)
            convertedAssignment.linkParents(assg.parent)
            return super.visit(convertedAssignment)
        }

        return assg
    }

    private fun flattenStructAssignmentFromStructLiteral(structAssignment: Assignment, program: Program): List<Assignment> {
        val identifier = structAssignment.target.identifier!!
        val identifierName = identifier.nameInSource.single()
        val targetVar = identifier.targetVarDecl(program.namespace)!!
        val struct = targetVar.struct!!

        val slv = structAssignment.value as? StructLiteralValue
        if(slv==null || slv.values.size != struct.numberOfElements)
            throw FatalAstException("element count mismatch")

        return struct.statements.zip(slv.values).map { (targetDecl, sourceValue) ->
            targetDecl as VarDecl
            val mangled = mangledStructMemberName(identifierName, targetDecl.name)
            val idref = IdentifierReference(listOf(mangled), structAssignment.position)
            val assign = Assignment(AssignTarget(null, idref, null, null, structAssignment.position),
                    null, sourceValue, sourceValue.position)
            assign.linkParents(structAssignment)
            assign
        }
    }

    private fun flattenStructAssignmentFromIdentifier(structAssignment: Assignment, program: Program): List<Assignment> {
        val identifier = structAssignment.target.identifier!!
        val identifierName = identifier.nameInSource.single()
        val targetVar = identifier.targetVarDecl(program.namespace)!!
        val struct = targetVar.struct!!
        when (structAssignment.value) {
            is IdentifierReference -> {
                val sourceVar = (structAssignment.value as IdentifierReference).targetVarDecl(program.namespace)!!
                if (sourceVar.struct == null)
                    throw FatalAstException("can only assign arrays or structs to structs")
                // struct memberwise copy
                val sourceStruct = sourceVar.struct!!
                if(sourceStruct!==targetVar.struct) {
                    // structs are not the same in assignment
                    return listOf()     // error will be printed elsewhere
                }
                return struct.statements.zip(sourceStruct.statements).map { member ->
                    val targetDecl = member.first as VarDecl
                    val sourceDecl = member.second as VarDecl
                    if(targetDecl.name != sourceDecl.name)
                        throw FatalAstException("struct member mismatch")
                    val mangled = mangledStructMemberName(identifierName, targetDecl.name)
                    val idref = IdentifierReference(listOf(mangled), structAssignment.position)
                    val sourcemangled = mangledStructMemberName(sourceVar.name, sourceDecl.name)
                    val sourceIdref = IdentifierReference(listOf(sourcemangled), structAssignment.position)
                    val assign = Assignment(AssignTarget(null, idref, null, null, structAssignment.position),
                            null, sourceIdref, member.second.position)
                    assign.linkParents(structAssignment)
                    assign
                }
            }
            is StructLiteralValue -> {
                throw IllegalArgumentException("not going to flatten a structLv assignment here")
            }
            else -> throw FatalAstException("strange struct value")
        }
    }

}
