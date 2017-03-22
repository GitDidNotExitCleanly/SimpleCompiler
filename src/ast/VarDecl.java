package ast;

public class VarDecl implements Tree {
	public final Type type;
	public final Var var;
	public boolean isField;

	public VarDecl(Type type, Var var) {
		this.type = type;
		this.var = var;
		this.isField = false;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitVarDecl(this);
	}
}
