package ast;

public class Assign extends Stmt {

	public final Var var;
	public final Expr expr;

	public Assign(Var var, Expr expr) {
		this.var = var;
		this.expr = expr;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitAssign(this);
	}

}
