package sem;

import ast.VarDecl;

public class VarSymbol extends Symbol {

	public VarDecl varDecl;

	public VarSymbol(VarDecl varDecl) {
		super(varDecl.var.name);
		this.varDecl = varDecl;
	}

	@Override
	public boolean isVar() {
		return true;
	}

	@Override
	public boolean isProc() {
		return false;
	}

}
