package sem;

public abstract class Symbol {
	public String name;

	public Symbol(String name) {
		this.name = name;
	}

	public abstract boolean isVarDecl();

	public abstract boolean isProcedure();
}
