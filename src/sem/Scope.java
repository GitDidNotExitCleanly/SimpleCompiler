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

	public Symbol lookup(String name) {
		Symbol result = lookupCurrent(name);
		if (result == null && this.outer != null) {
			return this.outer.lookup(name);
		}
		else {
			return result;
		}
	}

	public Symbol lookupCurrent(String name) {
		return symbolTable.get(name);
	}

	public void put(Symbol sym) {
		symbolTable.put(sym.name, sym);
	}
}
