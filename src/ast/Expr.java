package ast;

public abstract class Expr implements Tree {

	public Type type;

	@Override
	public abstract <T> T accept(ASTVisitor<T> v);
}
