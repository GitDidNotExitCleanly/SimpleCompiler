package gen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import ast.Assign;
import ast.BinOp;
import ast.Block;
import ast.ChrLiteral;
import ast.Expr;
import ast.FunCallExpr;
import ast.FunCallStmt;
import ast.If;
import ast.IntLiteral;
import ast.Procedure;
import ast.Program;
import ast.Return;
import ast.Stmt;
import ast.StrLiteral;
import ast.Type;
import ast.Var;
import ast.VarDecl;
import ast.While;

public class CodeGeneratorVisitor implements ast.ASTVisitor<Void> {

	public ClassWriter cw;
	private MethodVisitor mv;
	private LocalVariableTable local;

	public CodeGeneratorVisitor() {
		this.cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
		this.mv = null;
		this.local = null;
	}

	private String getTypeInternalName(Type type) {
		if (type == Type.INT) {
			return "I";
		} else if (type == Type.CHAR) {
			return "C";
		} else {
			return "V";
		}
	}

	private String getMainMethodDescriptor() {
		return "([Ljava/lang/String;)V";
	}

	private String getMethodDescriptor(Procedure proc) {
		StringBuilder desc = new StringBuilder();
		desc.append("(");
		for (VarDecl vd : proc.params) {
			desc.append(getTypeInternalName(vd.type));
		}
		desc.append(")");
		desc.append(getTypeInternalName(proc.type));
		return desc.toString();
	}

	private void loadVariable(Var v) {
		if (v.varDecl.isField) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, "Main", v.name, getTypeInternalName(v.varDecl.type));
		} else {
			int slot = this.local.get(v.varDecl);

			switch (v.varDecl.type) {
			case INT:
				mv.visitVarInsn(Opcodes.ILOAD, slot);
				break;
			case CHAR:
				mv.visitVarInsn(Opcodes.ILOAD, slot);
				break;
			default:
				break;
			}
		}
	}

	private void storeVariable(Var v) {
		if (v.varDecl.isField) {
			mv.visitFieldInsn(Opcodes.PUTSTATIC, "Main", v.name, getTypeInternalName(v.varDecl.type));
		} else {
			int slot = this.local.get(v.varDecl);

			switch (v.varDecl.type) {
			case INT:
				mv.visitVarInsn(Opcodes.ISTORE, slot);
				break;
			case CHAR:
				mv.visitVarInsn(Opcodes.ISTORE, slot);
				break;
			default:
				break;
			}
		}
	}

	@Override
	public Void visitProgram(Program p) {
		// generate main class structure
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null, "java/lang/Object", null);
		// generate field data
		for (VarDecl vd : p.varDecls) {
			cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, vd.var.name, getTypeInternalName(vd.type), null,
					null).visitEnd();
			vd.isField = true;
		}
		// generate member functions
		for (Procedure proc : p.procs) {
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, proc.name, getMethodDescriptor(proc), null,
					null);
			proc.accept(this);
		}
		// generate main function
		mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, p.main.name, getMainMethodDescriptor(), null,
				null);
		p.main.accept(this);
		// end of class
		cw.visitEnd();
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		return null;
	}

	@Override
	public Void visitProcedure(Procedure p) {
		mv.visitCode();
		this.local = new LocalVariableTable();
		// add local variables to table
		for (VarDecl vd : p.params) {
			this.local.put(vd);
		}
		p.block.accept(this);
		// remove local variables from table
		for (VarDecl vd : p.params) {
			this.local.remove(vd);
		}
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		return null;
	}

	/* EXPRESSION */

	@Override
	public Void visitIntLiteral(IntLiteral i) {
		mv.visitIntInsn(Opcodes.BIPUSH, i.val);
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral s) {
		mv.visitLdcInsn(s.val);
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral c) {
		mv.visitIntInsn(Opcodes.BIPUSH, c.val);
		return null;
	}

	@Override
	public Void visitVar(Var v) {
		loadVariable(v);
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		for (Expr e : f.exprs) {
			e.accept(this);
		}
		// deal with IO functions
		if (f.name.compareTo("print_c") == 0 || f.name.compareTo("print_i") == 0 || f.name.compareTo("print_s") == 0
				|| f.name.compareTo("read_c") == 0 || f.name.compareTo("read_i") == 0) {
			if (f.name.compareTo("print_s") == 0) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "IO", f.name, "(Ljava/lang/String;)V");
			} else {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "IO", f.name, getMethodDescriptor(f.proc));
			}
		} else {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Main", f.name, getMethodDescriptor(f.proc));
		}
		return null;
	}

	@Override
	public Void visitBinOp(BinOp b) {
		Label stmt2Block = new Label();
		Label end = new Label();

		b.lhs.accept(this);
		b.rhs.accept(this);
		switch (b.op) {
		case ADD:
			mv.visitInsn(Opcodes.IADD);
			break;
		case SUB:
			mv.visitInsn(Opcodes.ISUB);
			break;
		case MUL:
			mv.visitInsn(Opcodes.IMUL);
			break;
		case DIV:
			mv.visitInsn(Opcodes.IDIV);
			break;
		case MOD:
			mv.visitInsn(Opcodes.IREM);
			break;
		case EQ:
			mv.visitJumpInsn(Opcodes.IF_ICMPNE, stmt2Block);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(stmt2Block);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(end);
			break;
		case GE:
			mv.visitJumpInsn(Opcodes.IF_ICMPLT, stmt2Block);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(stmt2Block);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(end);
			break;
		case GT:
			mv.visitJumpInsn(Opcodes.IF_ICMPLE, stmt2Block);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(stmt2Block);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(end);
			break;
		case LE:
			mv.visitJumpInsn(Opcodes.IF_ICMPGT, stmt2Block);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(stmt2Block);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(end);
			break;
		case LT:
			mv.visitJumpInsn(Opcodes.IF_ICMPGE, stmt2Block);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(stmt2Block);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(end);
			break;
		case NE:
			mv.visitJumpInsn(Opcodes.IF_ICMPEQ, stmt2Block);
			mv.visitInsn(Opcodes.ICONST_1);
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(stmt2Block);
			mv.visitInsn(Opcodes.ICONST_0);
			mv.visitLabel(end);
			break;
		default:
			break;
		}
		return null;
	}

	/* STATEMENT */

	@Override
	public Void visitBlock(Block b) {
		// add local variables to table
		for (VarDecl vd : b.varDecls) {
			this.local.put(vd);
		}
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		// remove local variables from table
		for (VarDecl vd : b.varDecls) {
			this.local.remove(vd);
		}
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		Label pretest = new Label();
		Label posttest = new Label();
		// pre-test
		w.expr.accept(this);
		mv.visitJumpInsn(Opcodes.IFEQ, pretest);
		// loop body
		mv.visitLabel(posttest);
		w.stmt.accept(this);
		// post-test
		w.expr.accept(this);
		mv.visitJumpInsn(Opcodes.IFNE, posttest);
		mv.visitLabel(pretest);
		return null;
	}

	@Override
	public Void visitIf(If i) {
		Label stmt2Block = new Label();
		Label end = new Label();
		// expression test
		i.expr.accept(this);
		mv.visitJumpInsn(Opcodes.IFEQ, stmt2Block);
		// main body
		i.stmt1.accept(this);
		mv.visitJumpInsn(Opcodes.GOTO, end);
		mv.visitLabel(stmt2Block);
		// else body
		if (i.stmt2 != null) {
			i.stmt2.accept(this);
		}
		mv.visitLabel(end);
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		a.expr.accept(this);
		storeVariable(a.var);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		if (r.expr == null) {
			mv.visitInsn(Opcodes.RETURN);
		} else {
			r.expr.accept(this);
			mv.visitInsn(Opcodes.IRETURN);
		}
		return null;
	}

	@Override
	public Void visitFunCallStmt(FunCallStmt f) {
		for (Expr e : f.exprs) {
			e.accept(this);
		}
		// deal with IO functions
		if (f.name.compareTo("print_c") == 0 || f.name.compareTo("print_i") == 0 || f.name.compareTo("print_s") == 0
				|| f.name.compareTo("read_c") == 0 || f.name.compareTo("read_i") == 0) {
			if (f.name.compareTo("print_s") == 0) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "IO", f.name, "(Ljava/lang/String;)V");
			} else {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, "IO", f.name, getMethodDescriptor(f.proc));
			}
		} else {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Main", f.name, getMethodDescriptor(f.proc));
		}
		if (f.proc.type != Type.VOID) {
			mv.visitInsn(Opcodes.POP);
		}
		return null;
	}
}
