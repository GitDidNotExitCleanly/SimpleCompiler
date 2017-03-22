package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitProgram(Program p) {
		for (VarDecl vd : p.varDecls) {
			vd.accept(this);
		}
		for (Procedure proc : p.procs) {
			proc.accept(this);
		}
		p.main.accept(this);
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		if (vd.type == Type.VOID) {
			error("[Type Checker] Variable '" + vd.var.name + "' cannot be type 'void'");
		}
		return null;
	}

	@Override
	public Type visitProcedure(Procedure p) {
		for (VarDecl vd : p.params) {
			vd.accept(this);
		}
		Type blockT = p.block.accept(this);
		if (p.type != Type.VOID) {
			if (blockT != p.type) {
				error("[Type Checker] Return type of procedure '" + p.name + "' does not match the signature");
			}
		} else {
			if (blockT == null) {
				// add return statement to the procedure without a return
				// statement
				p.block.stmts.add(new Return(null));
			} else {
				if (blockT != Type.VOID) {
					error("[Type Checker] Return type of procedure '" + p.name + "' does not match the signature");
				}
			}
		}
		return null;
	}

	/* EXPRESSION */

	@Override
	public Type visitIntLiteral(IntLiteral i) {
		return Type.INT;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral c) {
		return Type.CHAR;
	}

	@Override
	public Type visitStrLiteral(StrLiteral s) {
		return Type.VOID;
	}

	@Override
	public Type visitVar(Var v) {
		return v.varDecl.type;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr f) {
		for (int i = 0; i < f.exprs.size(); i++) {
			Type exprT = f.exprs.get(i).accept(this);
			Type paramT = f.proc.params.get(i).type;
			if (exprT != paramT) {
				error("[Type Checker] Function call '" + f.name + "' does not match its signature");
			}
		}
		return f.proc.type;
	}

	@Override
	public Type visitBinOp(BinOp b) {
		Type lhsT = b.lhs.accept(this);
		Type rhsT = b.rhs.accept(this);
		if (b.op == Op.ADD || b.op == Op.SUB || b.op == Op.MUL || b.op == Op.DIV || b.op == Op.MOD) {
			if (lhsT != Type.INT) {
				error("[Type Checker] Left expression of binary operation '" + b.op + "' cannot be type " + lhsT);
			}
			if (rhsT != Type.INT) {
				error("[Type Checker] Right expression of binary operation '" + b.op + "' cannot be type " + rhsT);
			}
		} else {
			if (lhsT != rhsT) {
				error("[Type Checker] Left expression has different type with Right expression in binary operation '"
						+ b.op + "'");
			}
		}
		return Type.INT;
	}

	/* STATEMENT */

	@Override
	public Type visitBlock(Block b) {
		for (VarDecl vd : b.varDecls) {
			vd.accept(this);
		}
		Type outputT = null;
		for (Stmt s : b.stmts) {
			Type stmtT = s.accept(this);
			if (outputT == null && stmtT != null) {
				outputT = stmtT;
			}
		}
		return outputT;
	}

	@Override
	public Type visitFunCallStmt(FunCallStmt f) {
		for (int i = 0; i < f.exprs.size(); i++) {
			Type exprT = f.exprs.get(i).accept(this);
			Type paramT = f.proc.params.get(i).type;
			if (exprT != paramT) {
				error("[Type Checker] Function call '" + f.name + "' does not match its signature");
			}
		}
		return null;
	}

	@Override
	public Type visitWhile(While w) {
		Type exprT = w.expr.accept(this);
		if (exprT != Type.INT) {
			error("[Type Checker] Expression should be type 'int' instead of type '" + exprT + "' in WHILE block");
		}
		w.stmt.accept(this);
		return null;
	}

	@Override
	public Type visitIf(If i) {
		Type exprT = i.expr.accept(this);
		if (exprT != Type.INT) {
			error("[Type Checker] Expression should be type 'int' instead of type '" + exprT + "' in IF block");
		}
		Type stmt1T = i.stmt1.accept(this);
		Type stmt2T = null;
		if (i.stmt2 != null) {
			stmt2T = i.stmt2.accept(this);
		}
		if (stmt1T == stmt2T) {
			return stmt1T;
		} else {
			return null;
		}
	}

	@Override
	public Type visitAssign(Assign a) {
		Type varT = a.var.varDecl.type;
		Type exprT = a.expr.accept(this);
		if (varT != exprT) {
			error("[Type Checker] The type of variable '" + a.var.name + "' does not match in ASSIGNMENT");
		}
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		if (r.expr != null) {
			return r.expr.accept(this);
		} else {
			return Type.VOID;
		}
	}
}
