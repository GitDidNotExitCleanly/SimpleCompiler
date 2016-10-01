package gen;

import java.util.ArrayList;
import java.util.List;

import ast.Var;
import ast.VarDecl;

public class LocalVariableTable {

	private List<VarDecl> arr;

	public LocalVariableTable() {
		this.arr = new ArrayList<VarDecl>();
	}

	public int size() {
		return this.arr.size();
	}

	public void add(Var v) {
		this.arr.add(v.varDecl);
	}

	public int indexOf(Var v) {
		return this.arr.indexOf(v.varDecl);
	}
}
