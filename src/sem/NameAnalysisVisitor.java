package sem;

import java.util.ArrayList;

import ast.*;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	private Scope currentScope;

	public NameAnalysisVisitor() {
		this.currentScope = new Scope();
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
	public Void visitVarDecl(VarDecl vd) {
		Symbol vs = this.currentScope.lookupCurrent(vd.var.name);
		if (vs == null) {
			this.currentScope.put(new VarSymbol(vd));
		} else {
			error("[Name Analysis] Invalid overload of '" + vd.var.name + "'");
		}
		return null;
	}

	@Override
	public Void visitVar(Var v) {
		Symbol vs = this.currentScope.var_lookup(v.name);
		if (vs != null) {
			v.varDecl = ((VarSymbol) vs).varDecl;
		} else {
			error("[Name Analysis] Variable '" + v.name + "' is not declared");
		}
		return null;
	}

	@Override
	public Void visitProcedure(Procedure p) {
		Symbol ps = this.currentScope.lookupCurrent(p.name);
		if (ps == null) {
			this.currentScope.put(new ProcSymbol(p));
		} else {
			error("[Name Analysis] Invalid overload of '" + p.name + "'");
		}
		Scope outerScope = this.currentScope;
		this.currentScope = new Scope(outerScope);
		for (VarDecl vd : p.params) {
			vd.accept(this);
		}
		for (VarDecl vd : p.block.varDecls) {
			vd.accept(this);
		}
		for (Stmt s : p.block.stmts) {
			s.accept(this);
		}
		this.currentScope = outerScope;
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr f) {
		Symbol ps = this.currentScope.proc_lookup(f.name);
		if (ps != null && f.exprs.size() == ((ProcSymbol) ps).proc.params.size()) {
			for (Expr e : f.exprs) {
				e.accept(this);
			}
			f.proc = ((ProcSymbol) ps).proc;
		} else {
			// deal with IO functions (simulating linking)
			if (f.name.compareTo("read_c") == 0) {
				if (f.exprs.size() == 0) {
					f.proc = new Procedure(Type.CHAR, null, new ArrayList<VarDecl>(), null);
				} else {
					error("[Name Analysis] Procedure '" + f.name + "' has too many parameters");
				}
			} else if (f.name.compareTo("read_i") == 0) {
				if (f.exprs.size() == 0) {
					f.proc = new Procedure(Type.INT, null, new ArrayList<VarDecl>(), null);
				} else {
					error("[Name Analysis] Procedure '" + f.name + "' has too many parameters");
				}
			} else {
				error("[Name Analysis] Procedure '" + f.name + "' is not declared");
			}
		}
		return null;
	}

	@Override
	public Void visitFunCallStmt(FunCallStmt f) {
		Symbol ps = this.currentScope.proc_lookup(f.name);
		if (ps != null && f.exprs.size() == ((ProcSymbol) ps).proc.params.size()) {
			for (Expr e : f.exprs) {
				e.accept(this);
			}
			f.proc = ((ProcSymbol) ps).proc;
		} else {
			// deal with IO functions (simulating linking)
			if (f.name.compareTo("print_c") == 0 || f.name.compareTo("print_i") == 0
					|| f.name.compareTo("print_s") == 0) {
				if (f.exprs.size() == 1) {
					for (Expr e : f.exprs) {
						e.accept(this);
					}
					f.proc = new Procedure(Type.VOID, null, new ArrayList<VarDecl>(), null);
					if (f.name.compareTo("print_c") == 0) {
						f.proc.params.add(new VarDecl(Type.CHAR, new Var("c")));
					} else if (f.name.compareTo("print_i") == 0) {
						f.proc.params.add(new VarDecl(Type.INT, new Var("i")));
					} else {
						f.proc.params.add(new VarDecl(Type.VOID, new Var("v")));
					}
				} else {
					if (f.exprs.size() < 1) {
						error("[Name Analysis] Procedure '" + f.name + "' has too few parameters");
					} else {
						error("[Name Analysis] Procedure '" + f.name + "' has too many parameters");
					}
				}
			} else if (f.name.compareTo("read_c") == 0 || f.name.compareTo("read_i") == 0) {
				if (f.exprs.size() == 0) {
					if (f.name.compareTo("read_c") == 0) {
						f.proc = new Procedure(Type.CHAR, null, new ArrayList<VarDecl>(), null);
					} else {
						f.proc = new Procedure(Type.INT, null, new ArrayList<VarDecl>(), null);
					}
				} else {
					error("[Name Analysis] Procedure '" + f.name + "' has too many parameters");
				}
			} else {
				error("[Name Analysis] Procedure '" + f.name + "' is not declared");
			}
		}
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		Scope outerScope = this.currentScope;
		this.currentScope = new Scope(outerScope);
		for (VarDecl vd : b.varDecls) {
			vd.accept(this);
		}
		for (Stmt s : b.stmts) {
			s.accept(this);
		}
		this.currentScope = outerScope;
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
		Scope outerScope = this.currentScope;
		this.currentScope = new Scope(outerScope);
		i.stmt1.accept(this);
		if (i.stmt2 != null) {
			this.currentScope = new Scope(outerScope);
			i.stmt2.accept(this);
		}
		this.currentScope = outerScope;
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
		Scope outerScope = this.currentScope;
		this.currentScope = new Scope(outerScope);
		w.stmt.accept(this);
		this.currentScope = outerScope;
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
