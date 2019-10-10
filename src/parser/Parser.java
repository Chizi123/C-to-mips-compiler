package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * @author cdubach
 */
public class Parser {

	private Token token;

	// use for backtracking (useful for distinguishing decls from procs when parsing a program for instance)
	private Queue<Token> buffer = new LinkedList<>();

	private final Tokeniser tokeniser;


	public Parser(Tokeniser tokeniser) {
		this.tokeniser = tokeniser;
	}

	public void parse() {
		// get the first token
		nextToken();

		parseProgram();
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
	 * Look ahead the i^th element from the stream of token.
	 * i should be >= 1
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
	 * If the current token is equals to the expected one, then skip it, otherwise report an error.
	 * Returns the expected token or null if an error occurred.
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


	private void parseProgram() {
		parseIncludes();
		parseStructDecls();
		parseVarDecls(0);
		parseFunDecls();
		expect(TokenClass.EOF);
	}

	// includes are ignored, so does not need to return an AST node
	private void parseIncludes() {
		if (accept(TokenClass.INCLUDE)) {
			nextToken();
			expect(TokenClass.STRING_LITERAL);
			parseIncludes();
		}
	}

	private void parseStructDecls() {
		// to be completed ...
		while (accept(TokenClass.STRUCT)) {
			nextToken();
			expect(TokenClass.IDENTIFIER);
			expect(TokenClass.LBRA);
			parseVarDecls(1);
			expect(TokenClass.RBRA);
			expect(TokenClass.SC);
		}
	}

	// if "i" is non-zero, a variable is required
	private void parseVarDecls(int i) {
		if (lookAhead(2).tokenClass == TokenClass.LPAR)
			return;
		if (parseType(i)) {
			expect(TokenClass.IDENTIFIER);
			if (accept(TokenClass.LSBR)) {
				nextToken();
				expect(TokenClass.INT_LITERAL);
				expect(TokenClass.RSBR);
			}
			expect(TokenClass.SC);
			parseVarDecls(0);
		}
	}

	private void parseFunDecls() {
		while (parseType(0)) {
			expect(TokenClass.IDENTIFIER);
			expect(TokenClass.LPAR);
			parseParams();
			expect(TokenClass.RPAR);
			parseBlock();
		}
	}

	private boolean parseType(int i) {
		if (i == 0) {
			if (accept(TokenClass.INT, TokenClass.CHAR, TokenClass.VOID) || parseStructType()) {
				nextToken();
				if (accept(TokenClass.ASTERIX))
					nextToken();
				return true;
			}
		} else { //must parse type
			if (!parseType(0)) {
				error(TokenClass.INT, TokenClass.VOID, TokenClass.CHAR, TokenClass.STRUCT);
				error++;
				lastErrorToken=token;
			} else
				return true;
		}
		return false;
	}

	private boolean parseStructType() {
		if (accept(TokenClass.STRUCT)) {
			nextToken();
			if (accept(TokenClass.IDENTIFIER)) {
				return true;
			}
		}
		return false;
	}

	private void parseParams() {
		if (parseType(0)) {
			expect(TokenClass.IDENTIFIER);
		}
		while (accept(TokenClass.COMMA)) {
			nextToken();
			parseType(1);
			expect(TokenClass.IDENTIFIER);
		}
	}

	private void parseBlock() {
		expect(TokenClass.LBRA);
		parseVarDecls(0);
		while (!(accept(TokenClass.RBRA) || accept(TokenClass.EOF))) {
			parseStmt();
		}
		nextToken();
	}

	private void parseStmt() {
		if (accept(TokenClass.LBRA)) {
			parseBlock();
		} else if (accept(TokenClass.WHILE)) {
			nextToken();
			expect(TokenClass.LPAR);
			parseExp(1);
			expect(TokenClass.RPAR);
			parseStmt();
		} else if (accept(TokenClass.IF)) {
			nextToken();
			expect(TokenClass.LPAR);
			parseExp(1);
			expect(TokenClass.RPAR);
			parseStmt();
			if (accept(TokenClass.ELSE)) {
				nextToken();
				parseStmt();
			}
		} else if (accept(TokenClass.RETURN)) {
			nextToken();
			parseExp(0);
			expect(TokenClass.SC);
		} else {
			parseExp(1);
			if (accept(TokenClass.ASSIGN)) {
				nextToken();
				parseExp(1);
			}
			expect(TokenClass.SC);
		}
	}

	private void parseExp(int i) {
		parseLOB(i);
		parseStructArray();
		if (accept(TokenClass.OR)) {
			nextToken();
			parseExp(1);
		}
	}

	// Logic Or Block
	private void parseLOB(int i) {
		parseLAB(i);
		if (accept(TokenClass.AND)) {
			nextToken();
			parseLOB(1);
		}
	}

	// Logic And Block
	private void parseLAB(int i) {
		parseEQB(i);
		if (accept(TokenClass.EQ, TokenClass.NE)) {
			nextToken();
			parseLAB(1);
		}
	}

	// EQuality Block
	private void parseEQB(int i) {
		parseCPB(i);
		if (accept(TokenClass.LE, TokenClass.GE, TokenClass.LT, TokenClass.GT)) {
			nextToken();
			parseEQB(1);
		}
	}

	//ComParison Block
	private void parseCPB(int i) {
		parseADB(i);
		if (accept(TokenClass.PLUS, TokenClass.MINUS)) {
			nextToken();
			parseCPB(1);
		}
	}

	// ADd Block
	private void parseADB(int i) {
		parseMLB(i);
		if (accept(TokenClass.ASTERIX, TokenClass.DIV, TokenClass.REM)) {
			nextToken();
			parseADB(1);
		}
	}

	// MuLtiplication Block
	private void parseMLB(int i) {
		if (!parseSizeOf() &&
				!parseVAt() &&
				!parseIdentorFunc() &&
				!parseLits() &&
				!parseNeg() &&
				!parseBracket()) {
			if (i != 0) {
				System.out.println("Parsing error: unexpected expression at " + token.position + ", with token: " + token.tokenClass);
				error++;
				lastErrorToken = token;
				nextToken();
			}
		}
	}

	private boolean parseSizeOf() {
		if (accept(TokenClass.SIZEOF)) {
			nextToken();
			expect(TokenClass.LPAR);
			parseType(1);
			expect(TokenClass.RPAR);
			return true;
		}
		return false;
	}

	// Value At
	private boolean parseVAt() {
		if (accept(TokenClass.ASTERIX)) {
			nextToken();
			parseExp(1);
			return true;
		}
		return false;
	}

	// Parse Field from identifier or functions
	private boolean parseIdentorFunc() {
		if (accept(TokenClass.IDENTIFIER)) {
			nextToken();
			if (accept(TokenClass.LPAR)) {
				nextToken();
				if (!accept(TokenClass.RPAR)) {
					parseExp(1);
					while (accept(TokenClass.COMMA)) {
						nextToken();
						parseExp(1);
					}
				}
				expect(TokenClass.RPAR);
			}
			return true;
		}
		return false;
	}

	private boolean parseLits() {
		if (accept(TokenClass.STRING_LITERAL, TokenClass.CHAR_LITERAL, TokenClass.INT_LITERAL)) {
			nextToken();
			return true;
		}
		return false;
	}

	private boolean parseNeg() {
		if (accept(TokenClass.MINUS)) {
			nextToken();
			parseExp(1);
			return true;
		}
		return false;
	}

	private boolean parseBracket() {
		if (accept(TokenClass.LPAR)) {
			nextToken();
			if (parseType(0)) {
				expect(TokenClass.RPAR);
				parseExp(1);
			} else {
				parseExp(1);
				expect(TokenClass.RPAR);
			}
			return true;
		}
		return false;
	}

	//allows chained structs and arrays, eg a.a[1][2].a
	private void parseStructArray() {
		if (accept(TokenClass.DOT)) {
			nextToken();
			expect(TokenClass.IDENTIFIER);
			parseStructArray();
		} else if (accept(TokenClass.LSBR)) {
			nextToken();
			parseExp(1);
			expect(TokenClass.RSBR);
			parseStructArray();
		}
	}
}
