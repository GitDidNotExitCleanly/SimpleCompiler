package sem;

import java.util.Hashtable;
import java.util.Map;

public class Scope {
	private Scope outer;
	private Map<String, Symbol> symbolTable;

	public Scope(Scope outer) {
		this.outer = outer;
		this.symbolTable = new Hashtable<String, Symbol>();
	}

	public Scope() {
		this(null);
	}

	public Symbol proc_lookup(String name) {
		Symbol result = lookupCurrent(name);
		if (result != null && result.isProcedure()) {
			return result;
		} else {
			if (this.outer != null) {
				return this.outer.proc_lookup(name);
			} else {
				return null;
			}
		}
	}

	public Symbol var_lookup(String name) {
		Symbol result = lookupCurrent(name);
		if (result != null && result.isVarDecl()) {
			return result;
		} else {
			if (this.outer != null) {
				return this.outer.var_lookup(name);
			} else {
				return null;
			}
		}
	}

	public Symbol lookupCurrent(String name) {
		return symbolTable.get(name);
	}

	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
