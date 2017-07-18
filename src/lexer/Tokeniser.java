package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

public class Tokeniser {

	private Scanner scanner;

	private int error = 0;

	public int getErrorCount() {
		return this.error;
	}

	public Tokeniser(Scanner scanner) {
		this.scanner = scanner;
	}

	private void error(char c, int line, int col) {
		System.out.println("Lexing error: unrecognised character (" + c + ") at " + line + ":" + col);
		error++;
	}

	public Token nextToken() {
		Token result;
		try {
			result = next();
		} catch (EOFException eof) {
			// end of file, nothing to worry about, just return EOF token
			return new Token(TokenClass.EOF, scanner.getLine(), scanner.getColumn());
		} catch (IOException ioe) {
			ioe.printStackTrace();
			// something went horribly wrong, abort
			System.exit(-1);
			return null;
		}
		return result;
	}

	private Token next() throws IOException {

		int line = scanner.getLine();
		int column = scanner.getColumn();

		// get the next character
		char c = scanner.next();

		// skip white spaces
		if (Character.isWhitespace(c))
			return next();

		/* arithmetic operators */
		if (c == '+')
			return new Token(TokenClass.PLUS, line, column);
		if (c == '-')
			return new Token(TokenClass.MINUS, line, column);
		if (c == '*')
			return new Token(TokenClass.TIMES, line, column);
		if (c == '%')
			return new Token(TokenClass.MOD, line, column);
		if (c == '/') {
			try {
				c = scanner.peek();
			} catch (EOFException eof) {
				return new Token(TokenClass.DIV, line, column);
			}
			if (c == '/') {
				// deal with "//" comment
				scanner.next();
				while (c != '\n' && c != '\r') {
					c = scanner.next();
				}
				return next();
			} else if (c == '*') {
				// deal with "/* */" comment
				scanner.next();
				while (true) {
					while (scanner.next() != '*') {
					}
					if (scanner.peek() == '/') {
						scanner.next();
						break;
					}
				}
				return next();
			} else {
				return new Token(TokenClass.DIV, line, column);
			}
		}

		/* delimiters */
		if (c == '{')
			return new Token(TokenClass.LBRA, line, column);
		if (c == '}')
			return new Token(TokenClass.RBRA, line, column);
		if (c == '(')
			return new Token(TokenClass.LPAR, line, column);
		if (c == ')')
			return new Token(TokenClass.RPAR, line, column);
		if (c == ';')
			return new Token(TokenClass.SEMICOLON, line, column);
		if (c == ',')
			return new Token(TokenClass.COMMA, line, column);

		/* include */
		if (c == '#') {
			StringBuilder sb = new StringBuilder();
			try {
				while (!Character.isWhitespace(c) && c != '\n' && c != '\r') {
					sb.append(c);
					c = scanner.next();
				}
			} catch (EOFException eof) {
				if (sb.toString().compareTo("#include") == 0) {
					return new Token(TokenClass.INCLUDE, line, column);
				} else {
					error('#', line, column);
					return new Token(TokenClass.INVALID, line, column);
				}
			}
			if (sb.toString().compareTo("#include") == 0) {
				return new Token(TokenClass.INCLUDE, line, column);
			} else {
				error('#', line, column);
				return new Token(TokenClass.INVALID, line, column);
			}
		}

		/* assign and comparison */
		if (c == '!') {
			try {
				c = scanner.peek();
			} catch (EOFException eof) {
				error('!', line, column);
				return new Token(TokenClass.INVALID, line, column);
			}
			if (c == '=') {
				scanner.next();
				return new Token(TokenClass.NE, line, column);
			} else {
				error('!', line, column);
				return new Token(TokenClass.INVALID, line, column);
			}
		}
		if (c == '>') {
			try {
				c = scanner.peek();
			} catch (EOFException eof) {
				return new Token(TokenClass.GT, line, column);
			}
			if (c == '=') {
				scanner.next();
				return new Token(TokenClass.GE, line, column);
			} else {
				return new Token(TokenClass.GT, line, column);
			}
		}
		if (c == '<') {
			try {
				c = scanner.peek();
			} catch (EOFException eof) {
				return new Token(TokenClass.LT, line, column);
			}
			if (c == '=') {
				scanner.next();
				return new Token(TokenClass.LE, line, column);
			} else {
				return new Token(TokenClass.LT, line, column);
			}
		}
		if (c == '=') {
			try {
				c = scanner.peek();
			} catch (EOFException eof) {
				return new Token(TokenClass.ASSIGN, line, column);
			}
			if (c == '=') {
				scanner.next();
				return new Token(TokenClass.EQ, line, column);
			} else {
				return new Token(TokenClass.ASSIGN, line, column);
			}
		}

		/* literals */
		if (c == '\"') {
			StringBuilder sb = new StringBuilder();
			try {
				c = scanner.next();
				while (c != '\"') {
					sb.append(c);
					c = scanner.next();
				}
			} catch (EOFException eof) {
				error('\"', line, column);
				return new Token(TokenClass.INVALID, line, column);
			}
			return new Token(TokenClass.STRING_LITERAL, sb.toString(), line, column);
		}
		if (c == '\'') {
			StringBuilder sb = new StringBuilder();
			try {
				c = scanner.next();
				while (c != '\'') {
					sb.append(c);
					c = scanner.next();
				}
			} catch (EOFException eof) {
				error('\'', line, column);
				return new Token(TokenClass.INVALID, line, column);
			}
			return new Token(TokenClass.CHARACTER, sb.toString(), line, column);
		}
		if (Character.isDigit(c)) {
			StringBuilder sb = new StringBuilder();
			sb.append(c);
			try {
				while (Character.isDigit(scanner.peek())) {
					c = scanner.next();
					sb.append(c);
				}
			} catch (EOFException eof) {
				return new Token(TokenClass.NUMBER, sb.toString(), line, column);
			}
			return new Token(TokenClass.NUMBER, sb.toString(), line, column);
		}

		/* identifier, special functions, types, control flow */
		if (Character.isLetter(c) || c == '_') {
			StringBuilder sb = new StringBuilder();
			sb.append(c);
			try {
				while (Character.isLetterOrDigit(scanner.peek()) || scanner.peek() == '_') {
					c = scanner.next();
					sb.append(c);
				}
			} catch (EOFException eof) {
				String result = sb.toString();
				if (result.compareTo("if") == 0) {
					return new Token(TokenClass.IF, line, column);
				} else if (result.compareTo("int") == 0) {
					return new Token(TokenClass.INT, line, column);
				} else if (result.compareTo("return") == 0) {
					return new Token(TokenClass.RETURN, line, column);
				} else if (result.compareTo("read_i") == 0) {
					return new Token(TokenClass.READ, "read_i", line, column);
				} else if (result.compareTo("read_c") == 0) {
					return new Token(TokenClass.READ, "read_c", line, column);
				} else if (result.compareTo("main") == 0) {
					return new Token(TokenClass.MAIN, line, column);
				} else if (result.compareTo("print_s") == 0) {
					return new Token(TokenClass.PRINT, "print_s", line, column);
				} else if (result.compareTo("print_i") == 0) {
					return new Token(TokenClass.PRINT, "print_i", line, column);
				} else if (result.compareTo("print_c") == 0) {
					return new Token(TokenClass.PRINT, "print_c", line, column);
				} else if (result.compareTo("void") == 0) {
					return new Token(TokenClass.VOID, line, column);
				} else if (result.compareTo("char") == 0) {
					return new Token(TokenClass.CHAR, line, column);
				} else if (result.compareTo("else") == 0) {
					return new Token(TokenClass.ELSE, line, column);
				} else if (result.compareTo("while") == 0) {
					return new Token(TokenClass.WHILE, line, column);
				} else {
					return new Token(TokenClass.IDENTIFIER, result, line, column);
				}
			}
			String result = sb.toString();
			if (result.compareTo("if") == 0) {
				return new Token(TokenClass.IF, line, column);
			} else if (result.compareTo("int") == 0) {
				return new Token(TokenClass.INT, line, column);
			} else if (result.compareTo("return") == 0) {
				return new Token(TokenClass.RETURN, line, column);
			} else if (result.compareTo("read_i") == 0) {
				return new Token(TokenClass.READ, "read_i", line, column);
			} else if (result.compareTo("read_c") == 0) {
				return new Token(TokenClass.READ, "read_c", line, column);
			} else if (result.compareTo("main") == 0) {
				return new Token(TokenClass.MAIN, line, column);
			} else if (result.compareTo("print_s") == 0) {
				return new Token(TokenClass.PRINT, "print_s", line, column);
			} else if (result.compareTo("print_i") == 0) {
				return new Token(TokenClass.PRINT, "print_i", line, column);
			} else if (result.compareTo("print_c") == 0) {
				return new Token(TokenClass.PRINT, "print_c", line, column);
			} else if (result.compareTo("void") == 0) {
				return new Token(TokenClass.VOID, line, column);
			} else if (result.compareTo("char") == 0) {
				return new Token(TokenClass.CHAR, line, column);
			} else if (result.compareTo("else") == 0) {
				return new Token(TokenClass.ELSE, line, column);
			} else if (result.compareTo("while") == 0) {
				return new Token(TokenClass.WHILE, line, column);
			} else {
				return new Token(TokenClass.IDENTIFIER, result, line, column);
			}
		}

		// if we reach this point, it means we did not recognise a valid token
		error(c, line, column);
		return new Token(TokenClass.INVALID, line, column);
	}
}
