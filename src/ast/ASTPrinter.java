package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

	private PrintWriter writer;

	public ASTPrinter(PrintWriter writer) {
		this.writer = writer;
	}

	@Override
	public Void visitBlock(Block b) {
		writer.print("Block(");
		if (b.varDecls.size() > 0 && b.stmts.size() > 0) {
			for (VarDecl v : b.varDecls) {
				v.accept(this);
				writer.print(",");
			}
			for (int i = 0; i < b.stmts.size() - 1; i++) {
				b.stmts.get(i).accept(this);
				writer.print(",");
			}
			b.stmts.get(b.stmts.size() - 1).accept(this);
		} else if (b.varDecls.size() > 0 && b.stmts.size() <= 0) {
			for (int i = 0; i < b.varDecls.size() - 1; i++) {
				b.varDecls.get(i).accept(this);
				writer.print(",");
			}
			b.varDecls.get(b.varDecls.size() - 1).accept(this);
		} else if (b.varDecls.size() <= 0 && b.stmts.size() > 0) {
			for (int i = 0; i < b.stmts.size() - 1; i++) {
				b.stmts.get(i).accept(this);
				writer.print(",");
			}
			b.stmts.get(b.stmts.size() - 1).accept(this);
		} else {
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitProcedure(Procedure p) {
		writer.print("Procedure(");
		writer.print(p.type);
		writer.print("," + p.name + ",");
		for (VarDecl vd : p.params) {
			vd.accept(this);
			writer.print(",");
		}
		p.block.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitProgram(Program p) {
		writer.print("Program(");
		for (VarDecl vd : p.varDecls) {
			vd.accept(this);
			writer.print(",");
		}
		for (Procedure proc : p.procs) {
			proc.accept(this);
			writer.print(",");
		}
		p.main.accept(this);
		writer.print(")");
		writer.flush();
		return null;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		writer.print("VarDecl(");
		writer.print(vd.type + ",");
		vd.var.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitVar(Var v) {
		writer.print("Var(");
		writer.print(v.name);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		writer.print("Assign(");
		a.var.accept(this);
		writer.print(",");
		a.expr.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitBinOp(BinOp b) {
		writer.print("BinOp(");
		b.lhs.accept(this);
		writer.print(",");
		writer.print(b.op);
		writer.print(",");
		b.rhs.accept(this);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral c) {
		writer.print("ChrLiteral(");
		writer.print(c.val);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		writer.print("FunCallExpr(");
		writer.print(f.name);
		for (Expr e : f.exprs) {
			writer.print(",");
			e.accept(this);
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitFunCallStmt(FunCallStmt f) {
		writer.print("FunCallStmt(");
		writer.print(f.name);
		for (Expr e : f.exprs) {
			writer.print(",");
			e.accept(this);
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitIf(If i) {
		writer.print("If(");
		i.expr.accept(this);
		writer.print(",");
		i.stmt1.accept(this);
		if (i.stmt2 != null) {
			writer.print(",");
			i.stmt2.accept(this);
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral i) {
		writer.print("IntLiteral(");
		writer.print(i.val);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		writer.print("Return(");
		if (r.expr != null) {
			r.expr.accept(this);
		}
		writer.print(")");
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral s) {
		writer.print("StrLiteral(");
		writer.print(s.val);
		writer.print(")");
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		writer.print("While(");
		w.expr.accept(this);
		writer.print(",");
		w.stmt.accept(this);
		writer.print(")");
		return null;
	}
}
