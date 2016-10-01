package parser;

import ast.Assign;
import ast.BinOp;
import ast.Block;
import ast.ChrLiteral;
import ast.Expr;
import ast.FunCallExpr;
import ast.FunCallStmt;
import ast.If;
import ast.IntLiteral;
import ast.Op;
import ast.Procedure;
import ast.Program;
import ast.Return;
import ast.Stmt;
import ast.StrLiteral;
import ast.Type;
import ast.Var;
import ast.VarDecl;
import ast.While;
import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author cdubach
 */
public class Parser {

	private Token token;

	// use for backtracking (useful for distinguishing decls from procs when
	// parsing a program for instance)
	private Queue<Token> buffer = new LinkedList<>();

	private final Tokeniser tokeniser;

	public Parser(Tokeniser tokeniser) {
		this.tokeniser = tokeniser;
	}

	public Program parse() {
		// get the first token
		nextToken();
		return parseProgram();
	}

	public int getErrorCount() {
		return error;
	}

	private int error = 0;
	private Token lastErrorToken;

	private void error(TokenClass... expected) {

		if (lastErrorToken == token) {
			// skip this error, same token causing trouble
			return;
		}

		StringBuilder sb = new StringBuilder();
		String sep = "";
		for (TokenClass e : expected) {
			sb.append(sep);
			sb.append(e);
			sep = "|";
		}
		System.out.println("Parsing error: expected (" + sb + ") found (" + token + ") at " + token.position);

		error++;
		lastErrorToken = token;
	}

	/*
	 * Look ahead the i^th element from the stream of token. i should be >= 1
	 */
	private Token lookAhead(int i) {
		// ensures the buffer has the element we want to look ahead
		while (buffer.size() < i)
			buffer.add(tokeniser.nextToken());
		assert buffer.size() >= i;

		int cnt = 1;
		for (Token t : buffer) {
			if (cnt == i)
				return t;
			cnt++;
		}

		assert false; // should never reach this
		return null;
	}

	/*
	 * Consumes the next token from the tokeniser or the buffer if not empty.
	 */
	private void nextToken() {
		if (!buffer.isEmpty())
			token = buffer.remove();
		else
			token = tokeniser.nextToken();
	}

	/*
	 * If the current token is equals to the expected one, then skip it,
	 * otherwise report an error. Returns the expected token or null if an error
	 * occurred.
	 */
	private Token expect(TokenClass... expected) {
		for (TokenClass e : expected) {
			if (e == token.tokenClass) {
				Token cur = token;
				nextToken();
				return cur;
			}
		}

		error(expected);
		return null;
	}

	/*
	 * Returns true if the current token is equals to any of the expected ones.
	 */

	private boolean accept(TokenClass... expected) {
		boolean result = false;
		for (TokenClass e : expected)
			result |= (e == token.tokenClass);
		return result;
	}

	private Program parseProgram() {
		parseIncludes();
		List<VarDecl> varDecls = parseDecls();
		List<Procedure> procs = parseProcs();
		Procedure main = parseMain();
		expect(TokenClass.EOF);
		return new Program(varDecls, procs, main);
	}

	// includes are ignored, so does not need to return an AST node
	private void parseIncludes() {
		if (accept(TokenClass.INCLUDE)) {
			nextToken();
			expect(TokenClass.STRING_LITERAL);
			parseIncludes();
		}
	}

	private List<VarDecl> parseDecls() {
		return parseVardecls();
	}

	private List<Procedure> parseProcs() {
		return parseProcrep();
	}

	private Procedure parseMain() {
		expect(TokenClass.VOID);
		expect(TokenClass.MAIN);
		expect(TokenClass.LPAR);
		expect(TokenClass.RPAR);
		Block block = parseBody();
		return new Procedure(Type.VOID, "main", new ArrayList<VarDecl>(), block);
	}

	private List<Procedure> parseProcrep() {
		List<Procedure> procs = new ArrayList<Procedure>();
		if (accept(TokenClass.INT, TokenClass.CHAR)) {
			procs.add(parseProc());
			procs.addAll(parseProcrep());
		} else if (accept(TokenClass.VOID)) {
			Token lookahead = lookAhead(1);
			if (lookahead.tokenClass == TokenClass.IDENTIFIER) {
				procs.add(parseProc());
				procs.addAll(parseProcrep());
			} else if (lookahead.tokenClass == TokenClass.MAIN) {
			} else {
				nextToken();
				error(TokenClass.IDENTIFIER, TokenClass.MAIN);
			}
		} else {
			error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);
		}
		return procs;
	}

	private List<Stmt> parseStmtlist() {
		List<Stmt> stmts = new ArrayList<Stmt>();
		if (accept(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.IDENTIFIER, TokenClass.RETURN,
				TokenClass.PRINT, TokenClass.READ)) {
			stmts.add(parseStmt());
			stmts.addAll(parseStmtlist());
		} else if (accept(TokenClass.RBRA)) {
		} else {
			error(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.IDENTIFIER, TokenClass.RETURN,
					TokenClass.PRINT, TokenClass.READ, TokenClass.RBRA);
		}
		return stmts;
	}

	private Procedure parseProc() {
		Type type = parseType();
		String name = token.data;
		expect(TokenClass.IDENTIFIER);
		expect(TokenClass.LPAR);
		List<VarDecl> varDecls = parseParams();
		expect(TokenClass.RPAR);
		Block block = parseBody();
		return new Procedure(type, name, varDecls, block);
	}

	private Type parseType() {
		if (accept(TokenClass.INT)) {
			nextToken();
			return Type.INT;
		} else if (accept(TokenClass.CHAR)) {
			nextToken();
			return Type.CHAR;
		} else if (accept(TokenClass.VOID)) {
			nextToken();
			return Type.VOID;
		} else {
			error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID);
		}
		return null;
	}

	private List<VarDecl> parseParams() {
		if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {
			return parseParamlist();
		} else if (accept(TokenClass.RPAR)) {
		} else {
			error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.RPAR);
		}
		return new ArrayList<VarDecl>();
	}

	private List<VarDecl> parseParamlist() {
		List<VarDecl> varDecls = new ArrayList<VarDecl>();
		varDecls.add(parseTypeident());
		varDecls.addAll(parseTypeidentrep());
		return varDecls;
	}

	private List<VarDecl> parseTypeidentrep() {
		List<VarDecl> varDecls = new ArrayList<VarDecl>();
		if (accept(TokenClass.COMMA)) {
			nextToken();
			varDecls.add(parseTypeident());
			varDecls.addAll(parseTypeidentrep());
		} else if (accept(TokenClass.RPAR)) {
		} else {
			error(TokenClass.COMMA, TokenClass.RPAR);
		}
		return varDecls;
	}

	private Block parseBody() {
		expect(TokenClass.LBRA);
		List<VarDecl> varDecls = parseVardecls();
		List<Stmt> stmts = parseStmtlist();
		expect(TokenClass.RBRA);
		return new Block(varDecls, stmts);
	}

	private VarDecl parseTypeident() {
		Type type = parseType();
		if (accept(TokenClass.IDENTIFIER)) {
			Var var = new Var(token.data);
			nextToken();
			return new VarDecl(type, var);
		} else {
			error(TokenClass.IDENTIFIER);
		}
		return null;
	}

	private Stmt parseElsestmt() {
		if (accept(TokenClass.ELSE)) {
			nextToken();
			return parseStmt();
		} else if (accept(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.IDENTIFIER, TokenClass.RETURN,
				TokenClass.PRINT, TokenClass.READ)) {
		} else {
			error(TokenClass.ELSE, TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.IDENTIFIER,
					TokenClass.RETURN, TokenClass.PRINT, TokenClass.READ);
		}
		return null;
	}

	private Expr parseLexp() {
		Expr lhs = parseTerm();
		Op op = null;
		Expr rhs = null;
		if (accept(TokenClass.PLUS)) {
			op = Op.ADD;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.MINUS)) {
			op = Op.SUB;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE, TokenClass.NE, TokenClass.EQ,
				TokenClass.SEMICOLON, TokenClass.RPAR)) {
		} else {
			error(TokenClass.PLUS, TokenClass.MINUS, TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE,
					TokenClass.NE, TokenClass.EQ, TokenClass.SEMICOLON, TokenClass.RPAR);
		}
		return lhs;
	}

	private Expr parseTerm() {
		Expr lhs = parseFactor();
		Op op = null;
		Expr rhs = null;
		if (accept(TokenClass.DIV)) {
			op = Op.DIV;
			nextToken();
			rhs = parseTerm();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.TIMES)) {
			op = Op.MUL;
			nextToken();
			rhs = parseTerm();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.MOD)) {
			op = Op.MOD;
			nextToken();
			rhs = parseTerm();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.PLUS, TokenClass.MINUS, TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE,
				TokenClass.NE, TokenClass.EQ, TokenClass.SEMICOLON, TokenClass.RPAR)) {
		} else {
			error(TokenClass.DIV, TokenClass.TIMES, TokenClass.MOD, TokenClass.PLUS, TokenClass.MINUS, TokenClass.GT,
					TokenClass.LT, TokenClass.GE, TokenClass.LE, TokenClass.NE, TokenClass.EQ, TokenClass.SEMICOLON,
					TokenClass.RPAR);
		}
		return lhs;
	}

	private FunCallExpr parseFuncall() {
		if (accept(TokenClass.IDENTIFIER)) {
			String name = token.data;
			nextToken();
			expect(TokenClass.LPAR);
			List<Expr> exprs = parseArglist();
			expect(TokenClass.RPAR);
			return new FunCallExpr(name, exprs);
		} else {
			error(TokenClass.IDENTIFIER);
		}
		return null;
	}

	private List<Expr> parseArglist() {
		List<Expr> exprs = new ArrayList<Expr>();
		if (accept(TokenClass.IDENTIFIER)) {
			exprs.add(new Var(token.data));
			nextToken();
			exprs.addAll(parseArgrep());
		} else if (accept(TokenClass.RPAR)) {
		} else {
			error(TokenClass.IDENTIFIER, TokenClass.RPAR);
		}
		return exprs;
	}

	private List<Expr> parseArgrep() {
		List<Expr> exprs = new ArrayList<Expr>();
		if (accept(TokenClass.COMMA)) {
			nextToken();
			if (accept(TokenClass.IDENTIFIER)) {
				exprs.add(new Var(token.data));
				nextToken();
				exprs.addAll(parseArgrep());
			} else {
				error(TokenClass.IDENTIFIER);
			}
		} else if (accept(TokenClass.RPAR)) {
		} else {
			error(TokenClass.COMMA, TokenClass.RPAR);
		}
		return exprs;
	}

	private Expr parseExp() {
		Expr lhs = parseLexp();
		Op op = null;
		Expr rhs = null;
		if (accept(TokenClass.GT)) {
			op = Op.GT;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.LT)) {
			op = Op.LT;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.GE)) {
			op = Op.GE;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.LE)) {
			op = Op.LE;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.NE)) {
			op = Op.NE;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.EQ)) {
			op = Op.EQ;
			nextToken();
			rhs = parseLexp();
			return new BinOp(lhs, op, rhs);
		} else if (accept(TokenClass.RPAR)) {
			return lhs;
		} else {
			error(TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE, TokenClass.NE, TokenClass.EQ,
					TokenClass.RPAR);
		}
		return null;
	}

	private Stmt parseStmt() {
		if (accept(TokenClass.LBRA)) {
			nextToken();
			List<VarDecl> varDecls = parseVardecls();
			List<Stmt> stmts = parseStmtlist();
			expect(TokenClass.RBRA);
			return new Block(varDecls, stmts);
		} else if (accept(TokenClass.WHILE)) {
			nextToken();
			expect(TokenClass.LPAR);
			Expr expr = parseExp();
			expect(TokenClass.RPAR);
			Stmt stmt = parseStmt();
			return new While(expr, stmt);
		} else if (accept(TokenClass.IF)) {
			nextToken();
			expect(TokenClass.LPAR);
			Expr expr = parseExp();
			expect(TokenClass.RPAR);
			Stmt stmt1 = parseStmt();
			Stmt stmt2 = parseElsestmt();
			return new If(expr, stmt1, stmt2);
		} else if (accept(TokenClass.RETURN)) {
			nextToken();
			if (accept(TokenClass.SEMICOLON)) {
				nextToken();
				return new Return(null);
			} else {
				Expr expr = parseLexp();
				expect(TokenClass.SEMICOLON);
				return new Return(expr);
			}
		} else if (accept(TokenClass.PRINT)) {
			String name = token.data;
			nextToken();
			expect(TokenClass.LPAR);
			List<Expr> exprs = new ArrayList<Expr>();
			if (accept(TokenClass.STRING_LITERAL)) {
				exprs.add(new StrLiteral(token.data));
				nextToken();
				expect(TokenClass.RPAR);
				expect(TokenClass.SEMICOLON);
			} else if (accept(TokenClass.LPAR, TokenClass.MINUS, TokenClass.IDENTIFIER, TokenClass.NUMBER,
					TokenClass.CHARACTER, TokenClass.READ)) {
				exprs.add(parseLexp());
				expect(TokenClass.RPAR);
				expect(TokenClass.SEMICOLON);
			} else {
				error(TokenClass.STRING_LITERAL, TokenClass.LPAR, TokenClass.MINUS, TokenClass.IDENTIFIER,
						TokenClass.NUMBER, TokenClass.CHARACTER, TokenClass.READ);
			}
			return new FunCallStmt(name, exprs);
		} else if (accept(TokenClass.READ)) {
			String name = token.data;
			nextToken();
			expect(TokenClass.LPAR);
			expect(TokenClass.RPAR);
			expect(TokenClass.SEMICOLON);
			return new FunCallStmt(name, new ArrayList<Expr>());
		} else if (accept(TokenClass.IDENTIFIER)) {
			Var var = new Var(token.data);
			Token lookahead = lookAhead(1);
			if (lookahead.tokenClass == TokenClass.ASSIGN) {
				nextToken();
				nextToken();
				Expr expr = parseLexp();
				expect(TokenClass.SEMICOLON);
				return new Assign(var, expr);
			} else {
				FunCallExpr funCallExpr = parseFuncall();
				expect(TokenClass.SEMICOLON);
				return new FunCallStmt(funCallExpr.name, funCallExpr.exprs);
			}
		} else {
			error(TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.RETURN, TokenClass.PRINT,
					TokenClass.READ, TokenClass.IDENTIFIER);
		}
		return null;
	}

	private Expr parseFactor() {
		if (accept(TokenClass.LPAR)) {
			nextToken();
			Expr expr = parseLexp();
			expect(TokenClass.RPAR);
			return expr;
		} else if (accept(TokenClass.MINUS)) {
			nextToken();
			if (accept(TokenClass.IDENTIFIER)) {
				Var var = new Var(token.data);
				nextToken();
				return new BinOp(new IntLiteral(0), Op.SUB, var);
			} else if (accept(TokenClass.NUMBER)) {
				IntLiteral il = new IntLiteral(Integer.valueOf(token.data));
				nextToken();
				return new BinOp(new IntLiteral(0), Op.SUB, il);
			} else {
				error(TokenClass.IDENTIFIER, TokenClass.NUMBER);
			}
		} else if (accept(TokenClass.NUMBER)) {
			IntLiteral il = new IntLiteral(Integer.valueOf(token.data));
			nextToken();
			return il;
		} else if (accept(TokenClass.CHARACTER)) {
			ChrLiteral cl = new ChrLiteral(token.data.charAt(0));
			nextToken();
			return cl;
		} else if (accept(TokenClass.READ)) {
			String name = token.data;
			nextToken();
			expect(TokenClass.LPAR);
			expect(TokenClass.RPAR);
			return new FunCallExpr(name, new ArrayList<Expr>());
		} else if (accept(TokenClass.IDENTIFIER)) {
			Token lookahead = lookAhead(1);
			if (lookahead.tokenClass == TokenClass.LPAR) {
				return parseFuncall();
			} else if (lookahead.tokenClass == TokenClass.DIV || lookahead.tokenClass == TokenClass.TIMES
					|| lookahead.tokenClass == TokenClass.MOD || lookahead.tokenClass == TokenClass.PLUS
					|| lookahead.tokenClass == TokenClass.MINUS || lookahead.tokenClass == TokenClass.GT
					|| lookahead.tokenClass == TokenClass.LT || lookahead.tokenClass == TokenClass.GE
					|| lookahead.tokenClass == TokenClass.LE || lookahead.tokenClass == TokenClass.NE
					|| lookahead.tokenClass == TokenClass.EQ || lookahead.tokenClass == TokenClass.SEMICOLON
					|| lookahead.tokenClass == TokenClass.RPAR) {
				Var var = new Var(token.data);
				nextToken();
				return var;
			} else {
				nextToken();
				error(TokenClass.LPAR, TokenClass.DIV, TokenClass.TIMES, TokenClass.MOD, TokenClass.PLUS,
						TokenClass.MINUS, TokenClass.GT, TokenClass.LT, TokenClass.GE, TokenClass.LE, TokenClass.NE,
						TokenClass.EQ, TokenClass.SEMICOLON, TokenClass.RPAR);
			}
		} else {
			error(TokenClass.LPAR, TokenClass.MINUS, TokenClass.NUMBER, TokenClass.CHARACTER, TokenClass.READ,
					TokenClass.IDENTIFIER);
		}
		return null;
	}

	private List<VarDecl> parseVardecls() {
		List<VarDecl> varDecls = new ArrayList<VarDecl>();
		if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID)) {
			Token lookahead1 = lookAhead(1);
			if (lookahead1.tokenClass == TokenClass.IDENTIFIER) {
				Token lookahead2 = lookAhead(2);
				if (lookahead2.tokenClass == TokenClass.SEMICOLON) {
					varDecls.add(parseTypeident());
					expect(TokenClass.SEMICOLON);
					varDecls.addAll(parseVardecls());
				} else if (lookahead2.tokenClass == TokenClass.LPAR) {
				} else {
					nextToken();
					error(TokenClass.SEMICOLON, TokenClass.LPAR);
				}
			} else if (lookahead1.tokenClass == TokenClass.MAIN) {
			} else {
				nextToken();
				error(TokenClass.IDENTIFIER);
			}
		} else if (accept(TokenClass.RBRA, TokenClass.LBRA, TokenClass.WHILE, TokenClass.IF, TokenClass.IDENTIFIER,
				TokenClass.RETURN, TokenClass.PRINT, TokenClass.READ)) {
		} else {
			error(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID, TokenClass.RBRA, TokenClass.LBRA, TokenClass.WHILE,
					TokenClass.IF, TokenClass.IDENTIFIER, TokenClass.RETURN, TokenClass.PRINT, TokenClass.READ);
		}
		return varDecls;
	}
}
