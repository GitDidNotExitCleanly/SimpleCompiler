package ast;

public interface ASTVisitor<T> {
	public T visitProgram(Program p);

	public T visitVarDecl(VarDecl vd);

	public T visitVar(Var v);

	public T visitProcedure(Procedure p);

	public T visitFunCallExpr(FunCallExpr f);

	public T visitFunCallStmt(FunCallStmt f);

	public T visitBlock(Block b);

	public T visitBinOp(BinOp b);

	public T visitAssign(Assign a);

	public T visitIf(If i);

	public T visitReturn(Return r);

	public T visitWhile(While w);

	public T visitStrLiteral(StrLiteral s);

	public T visitIntLiteral(IntLiteral i);

	public T visitChrLiteral(ChrLiteral c);
}
