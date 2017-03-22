package gen;

import java.util.Hashtable;
import java.util.Map;

import ast.VarDecl;

public class LocalVariableTable {

	private Map<VarDecl, Integer> table;

	public LocalVariableTable() {
		this.table = new Hashtable<VarDecl, Integer>();
	}

	public void put(VarDecl vd) {
		int currentIndex = this.table.size();
		this.table.put(vd, currentIndex);
	}

	public Integer get(VarDecl vd) {
		return this.table.get(vd);
	}

	public void remove(VarDecl vd) {
		this.table.remove(vd);
	}
}
