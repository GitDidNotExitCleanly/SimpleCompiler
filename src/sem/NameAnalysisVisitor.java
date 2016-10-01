package sem;

import java.util.ArrayList;
import java.util.List;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	private Scope scope;
	private int levels;

	public NameAnalysisVisitor() {
		this.scope = new Scope();
		this.levels = 0;
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		Symbol vs = scope.lookupCurrent(vd.var.name);
		if (vs == null) {
			vd.levels = levels;
			scope.put(new VarSymbol(vd));
		} else {
			if (vs.isProc()) {
				error("Procedure \"" + vd.var.name + "\" is already declared, Use other names for the variable");
			} else if (vs.isVar()) {
				error("Variable \"" + vd.var.name + "\" is already declared in the same scope");
			} else {
				error("Unexpected error");
			}
		}
		return null;
	}

	@Override
	public Void visitVar(Var v) {
		Symbol vs = scope.lookup(v.name);
		if (vs == null) {
			error("Variable \"" + v.name + "\" is not declared");
		} else if (!vs.isVar()) {
			error("Procedure \"" + v.name + "\" is already declared, Use other names for the variable");
		} else {
			v.varDecl = ((VarSymbol) vs).varDecl;
		}
		return null;
	}

	@Override
	public Void visitProcedure(Procedure p) {
		Symbol ps = scope.lookupCurrent(p.name);
		if (ps == null) {
			scope.put(new ProcSymbol(p));
		} else {
			if (ps.isProc()) {
				error("Procedure \"" + p.name + "\" is already declared");
			} else if (ps.isVar()) {
				error("Variable \"" + p.name + "\" is already declared, Use other names for the procedure");
			} else {
				error("Unexpected error");
			}
		}
		Scope oldScope = scope;
		scope = new Scope(oldScope);
		levels++;
		for (VarDecl vd : p.params) {
			vd.accept(this);
		}
		p.block.accept(this);
		scope = oldScope;
		levels--;
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		Symbol ps = scope.lookup(f.name);
		if (ps == null) {
			// if it is IO function, pass
			List<VarDecl> params = new ArrayList<VarDecl>();
			if (f.name.compareTo("print_c") == 0) {
				params.add(new VarDecl(Type.CHAR, new Var("c")));
				f.p = new Procedure(Type.VOID, "print_c", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("print_i") == 0) {
				params.add(new VarDecl(Type.INT, new Var("i")));
				f.p = new Procedure(Type.VOID, "print_i", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("print_s") == 0) {
				params.add(new VarDecl(Type.STRING, new Var("s")));
				f.p = new Procedure(Type.VOID, "print_s", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("read_c") == 0) {
				params.add(new VarDecl(Type.CHAR, new Var("c")));
				f.p = new Procedure(Type.CHAR, "read_c", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("read_i") == 0) {
				params.add(new VarDecl(Type.INT, new Var("i")));
				f.p = new Procedure(Type.INT, "print_i", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else {
				error("Procedure \"" + f.name + "\" is not declared");
			}
		} else if (!ps.isProc()) {
			error("Variable \"" + f.name + "\" is already declared, Use other names for the procedure");
		} else {
			f.p = ((ProcSymbol) ps).proc;
			for (Expr e : f.exprs) {
				e.accept(this);
			}
		}
		return null;
	}

	@Override
	public Void visitFunCallStmt(FunCallStmt f) {
		Symbol ps = scope.lookup(f.name);
		if (ps == null) {
			// if it is IO function, pass
			List<VarDecl> params = new ArrayList<VarDecl>();
			if (f.name.compareTo("print_c") == 0) {
				params.add(new VarDecl(Type.CHAR, new Var("c")));
				f.p = new Procedure(Type.VOID, "print_c", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("print_i") == 0) {
				params.add(new VarDecl(Type.INT, new Var("i")));
				f.p = new Procedure(Type.VOID, "print_i", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("print_s") == 0) {
				params.add(new VarDecl(Type.STRING, new Var("s")));
				f.p = new Procedure(Type.VOID, "print_s", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("read_c") == 0) {
				params.add(new VarDecl(Type.CHAR, new Var("c")));
				f.p = new Procedure(Type.CHAR, "read_c", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else if (f.name.compareTo("read_i") == 0) {
				params.add(new VarDecl(Type.INT, new Var("i")));
				f.p = new Procedure(Type.INT, "print_i", params, null);
				for (Expr e : f.exprs) {
					e.accept(this);
				}
				return null;
			} else {
				error("Procedure \"" + f.name + "\" is not declared");
			}
		} else if (!ps.isProc()) {
			error("Variable \"" + f.name + "\" is already declared, Use other names for the procedure");
		} else {
			f.p = ((ProcSymbol) ps).proc;
			for (Expr e : f.exprs) {
				e.accept(this);
			}
		}
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		Scope oldScope = scope;
		scope = new Scope(oldScope);
		levels++;
		for (VarDecl vd : b.varDecls) {
			vd.accept(this);
		}
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		scope = oldScope;
		levels--;
		return null;
	}

	@Override
	public Void visitProgram(Program p) {
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
	public Void visitAssign(Assign a) {
		a.var.accept(this);
		a.expr.accept(this);
		return null;
	}

	@Override
	public Void visitBinOp(BinOp b) {
		b.lhs.accept(this);
		b.rhs.accept(this);
		return null;
	}

	@Override
	public Void visitIf(If i) {
		i.expr.accept(this);
		i.stmt1.accept(this);
		if (i.stmt2 != null) {
			i.stmt2.accept(this);
		}
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		if (r.expr != null) {
			r.expr.accept(this);
		}
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		w.expr.accept(this);
		w.stmt.accept(this);
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral i) {
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral c) {
		return null;
	}

	@Override
	public Void visitStrLiteral(StrLiteral s) {
		return null;
	}
}
