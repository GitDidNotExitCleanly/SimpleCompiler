package ast;

import java.util.List;

public class FunCallStmt extends Stmt {

	public final String name;
	public final List<Expr> exprs;
	public Procedure proc;

	public FunCallStmt(String name, List<Expr> exprs) {
		this.name = name;
		this.exprs = exprs;
		this.proc = null;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitFunCallStmt(this);
	}

}
