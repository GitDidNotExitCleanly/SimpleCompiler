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
		if (type == Type.VOID) {
			return "V";
		} else if (type == Type.INT) {
			return "I";
		} else if (type == Type.CHAR) {
			return "C";
		} else {
			return "Ljava/lang/String;";
		}
	}

	private String getMethodDescriptor(Procedure p) {
		StringBuilder desc = new StringBuilder();
		desc.append("(");
		if (p.name.compareTo("main") == 0) {
			desc.append("[Ljava/lang/String;");
		} else {
			for (VarDecl vd : p.params) {
				desc.append(getTypeInternalName(vd.type));
			}
		}
		desc.append(")");
		desc.append(getTypeInternalName(p.type));
		return desc.toString();
	}

	@Override
	public Void visitProgram(Program p) {
		cw.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "Main", null, "java/lang/Object", null);

		for (VarDecl vd : p.varDecls) {
			cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, vd.var.name, getTypeInternalName(vd.type), null,
					null).visitEnd();
		}

		for (Procedure proc : p.procs) {
			mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, proc.name, getMethodDescriptor(proc), null,
					null);
			proc.accept(this);
			mv = null;
		}

		mv = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, p.main.name, getMethodDescriptor(p.main), null,
				null);
		p.main.accept(this);
		mv = null;

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
		local = new LocalVariableTable();
		p.block.accept(this);
		local = null;
		if (p.type == Type.VOID) {
			mv.visitInsn(Opcodes.RETURN);
		}
		mv.visitMaxs(0, 0);
		mv.visitEnd();
		return null;
	}

	@Override
	public Void visitFunCallStmt(FunCallStmt f) {
		for (Expr e : f.exprs) {
			e.accept(this);
		}
		if (f.name.compareTo("print_c") == 0 || f.name.compareTo("print_i") == 0 || f.name.compareTo("print_s") == 0
				|| f.name.compareTo("read_c") == 0 || f.name.compareTo("read_i") == 0) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "IO", f.name, getMethodDescriptor(f.p));
		} else {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Main", f.name, getMethodDescriptor(f.p));
			if (f.p.type != Type.VOID) {
				mv.visitInsn(Opcodes.POP);
			}
		}
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
/*
		a.expr.accept(this);
		if (a.var.varDecl.levels == 0) {
			mv.visitFieldInsn(Opcodes.PUTSTATIC, "Main", a.var.name, getTypeInternalName(a.var.varDecl.type));
		} else {
			int slot = local.indexOf(a.var);
			if (slot == -1) {
				local.add(a.var);
				slot = local.size() - 1;
			}
			mv.visitVarInsn(Opcodes.ISTORE, slot);
		}
*/
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
	public Void visitVar(Var v) {
/*
		if (v.varDecl.levels == 0) {
			mv.visitFieldInsn(Opcodes.GETSTATIC, "Main", v.name, getTypeInternalName(v.varDecl.type));
		} else {
			int slot = local.indexOf(v);
			if (slot == -1) {
				local.add(v);
				slot = local.size() - 1;
			}
			mv.visitVarInsn(Opcodes.ILOAD, slot);
		}
*/
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		for (Expr e : f.exprs) {
			e.accept(this);
		}
		if (f.name.compareTo("print_c") == 0 || f.name.compareTo("print_i") == 0 || f.name.compareTo("print_s") == 0
				|| f.name.compareTo("read_c") == 0 || f.name.compareTo("read_i") == 0) {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "IO", f.name, getMethodDescriptor(f.p));
		} else {
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, "Main", f.name, getMethodDescriptor(f.p));
		}
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral s) {
		mv.visitLdcInsn(s.val);
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral i) {
		mv.visitIntInsn(Opcodes.BIPUSH, i.val);
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral c) {
		mv.visitIntInsn(Opcodes.BIPUSH, c.val);
		return null;
	}

	///////////////////////////////////////////////// not working
	@Override
	public Void visitIf(If i) {
		// System.out.println("visitIf");

		if (i.stmt2 != null) {
			Label elseStmt = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, elseStmt);
			i.stmt1.accept(this);
			Label end = new Label();
			mv.visitJumpInsn(Opcodes.GOTO, end);
			mv.visitLabel(elseStmt);
			i.stmt2.accept(this);
			mv.visitLabel(end);
		} else {
			Label end = new Label();
			mv.visitJumpInsn(Opcodes.IFEQ, end);
			i.stmt1.accept(this);
			mv.visitLabel(end);
		}
		return null;
	}

	//////////////////////////////////////////////// condition(jump) is not
	//////////////////////////////////////////////// correct
	@Override
	public Void visitWhile(While w) {
		// System.out.println("visitWhile");

		Label test = new Label();
		Label loop = new Label();
		mv.visitJumpInsn(Opcodes.GOTO, test);
		mv.visitLabel(loop);
		w.stmt.accept(this);
		mv.visitLabel(test);
		w.expr.accept(this);
		mv.visitJumpInsn(Opcodes.IFNE, loop);
		return null;
	}

	@Override
	public Void visitBinOp(BinOp b) {
		// System.out.println("visitBinOp");

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
		case GE:
		case GT:
		case LE:
		case LT:
		case NE:
		default:
			// not correct !!!!
			mv.visitInsn(Opcodes.ISUB);
			break;
		}
		return null;
	}
}
