package ast;

public class VarDecl implements Tree {
	public final Type type;
	public final Var var;
	public Integer levels;

	public VarDecl(Type type, Var var) {
		this.type = type;
		this.var = var;
		this.levels = null;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitVarDecl(this);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else {
			return this.type == ((VarDecl) obj).type && this.var.name.compareTo(((VarDecl) obj).var.name) == 0
					&& this.levels == ((VarDecl) obj).levels;
		}
	}
}
