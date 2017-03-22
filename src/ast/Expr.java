package ast;

public abstract class Expr implements Tree {

	@Override
	public abstract <T> T accept(ASTVisitor<T> v);
}
