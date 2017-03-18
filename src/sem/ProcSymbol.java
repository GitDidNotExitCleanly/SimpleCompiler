package sem;

import ast.Procedure;

public class ProcSymbol extends Symbol {

	public Procedure proc;

	public ProcSymbol(Procedure proc) {
		super(proc.name);
		this.proc = proc;
	}

	@Override
	public boolean isVarDecl() {
		return false;
	}

	@Override
	public boolean isProcedure() {
		return true;
	}
}
