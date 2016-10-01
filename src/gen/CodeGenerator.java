package gen;

import ast.Program;

public class CodeGenerator {

	public byte[] data;

	public void emitProgram(Program program) {
		// generate code for AST
		CodeGeneratorVisitor cgv = new CodeGeneratorVisitor();
		program.accept(cgv);
		this.data = cgv.cw.toByteArray();
	}
}
