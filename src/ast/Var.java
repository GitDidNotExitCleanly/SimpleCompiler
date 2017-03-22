package ast;

public class Var extends Expr {
	public final String name;
	public VarDecl varDecl;

	public Var(String name) {
		this.name = name;
		this.varDecl = null;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitVar(this);
	}
}
