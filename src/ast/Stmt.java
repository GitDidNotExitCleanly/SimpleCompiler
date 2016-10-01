package ast;

public abstract class Stmt implements Tree {
	@Override
	public abstract <T> T accept(ASTVisitor<T> v);
}
