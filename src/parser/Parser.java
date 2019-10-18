package parser;

import ast.*;

import lexer.Token;
import lexer.Tokeniser;
import lexer.Token.TokenClass;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import static lexer.Token.TokenClass.*;


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
//				nextToken();
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
		List<StructTypeDecl> stds = parseStructDecls();
		List<VarDecl> vds = parseVarDecls(0);
		List<FunDecl> fds = parseFunDecls();
		expect(EOF); nextToken();
		return new Program(stds, vds, fds);
	}

	// includes are ignored, so does not need to return an AST node
	private void parseIncludes() {
		if (accept(INCLUDE)) {
			nextToken();
			expect(STRING_LITERAL); nextToken();
			parseIncludes();
		}
	}

	private List<StructTypeDecl> parseStructDecls() {
		// to be completed ...
		List<StructTypeDecl> out = new LinkedList<>();
		while (accept(STRUCT)) {
			nextToken();
			expect(IDENTIFIER);
			StructType st = new StructType(token.data);
			nextToken();
			expect(LBRA); nextToken();
			List<VarDecl> vdL = parseVarDecls(1);
			expect(RBRA); nextToken();
			expect(SC); nextToken();
			out.add(new StructTypeDecl(st, vdL));
		}
		return out;
	}

	// if "i" is non-zero, a variable is required
	private List<VarDecl> parseVarDecls(int i) {
		List<VarDecl> out = new LinkedList<>();
		if (lookAhead(2).tokenClass == LPAR)
			return out;
		if (parseType(i)) {
			Type t = getType();
			expect(IDENTIFIER);
			String name = token.data;
			nextToken();
			if (accept(LSBR)) {
				nextToken();
				expect(INT_LITERAL);
				t = new ArrayType(t,Integer.parseInt(token.data));
				nextToken();
				expect(RSBR); nextToken();
			}
			out.add(new VarDecl(t,name));
			expect(SC); nextToken();
			out.addAll(parseVarDecls(0));
		}
		return out;
	}

	private List<FunDecl> parseFunDecls() {
		List<FunDecl> out = new LinkedList<>();
		while (parseType(0)) {
			Type t = getType();
			expect(IDENTIFIER);
			String name = token.data;
			nextToken();
			expect(LPAR); nextToken();
			List<VarDecl> params = parseParams();
			expect(RPAR); nextToken();
			Block block = parseBlock();
			out.add(new FunDecl(t, name, params, block));
		}
		return out;
	}

	private boolean parseType(int i) {
		if (i == 0) {
			if (accept(INT, CHAR, VOID) || parseStructType()) {
				return true;
			}
		} else { //must parse type
			if (!parseType(0)) {
				error(INT, VOID, CHAR, STRUCT);
				error++;
				lastErrorToken=token;
			} else
				return true;
		}
		return false;
	}

	private Type getType() {
		Type out;
		if (accept(INT)) {
			out = new BaseType(BaseTypeEnum.INT);
		} else if (accept(CHAR)) {
			out = new BaseType(BaseTypeEnum.CHAR);
		} else if (accept(VOID)) {
			out = new BaseType(BaseTypeEnum.VOID);
		} else {
			out = getStruct();
		}
		nextToken();
		if (accept(ASTERIX)) {
			nextToken();
			out = new PointerType(out);
		}
		return out;
	}

	private boolean parseStructType() {
		if (accept(STRUCT)) {
			nextToken();
			if (accept(IDENTIFIER)) {
				return true;
			}
		}
		return false;
	}

	private StructType getStruct() {
		return new StructType(token.data);
	}

	private List<VarDecl> parseParams() {
		List<VarDecl> out = new LinkedList<>();
		if (parseType(0)) {
			Type t = getType();
			expect(IDENTIFIER);
			out.add(new VarDecl(t, token.data));
			nextToken();
		}
		while (accept(COMMA)) {
			nextToken();
			parseType(1);
			Type t = getType();
			expect(IDENTIFIER);
			out.add(new VarDecl(t, token.data));
			nextToken();
		}
		return out;
	}

	private Block parseBlock() {
		expect(LBRA); nextToken();
		List<VarDecl> vdL = parseVarDecls(0);
		List<Stmt> sL = new LinkedList<>();
		while (!(accept(RBRA) || accept(EOF))) {
			sL.add(parseStmt());
		}
		nextToken();
		return new Block(vdL, sL);
	}

	private Stmt parseStmt() {
		Stmt out;
		if (accept(LBRA)) {
			out = parseBlock();
		} else if (accept(WHILE)) {
			nextToken();
			expect(LPAR); nextToken();
			Expr cond = parseExp(1);
			expect(RPAR); nextToken();
			Stmt loop = parseStmt();
			out = new While(cond,loop);
		} else if (accept(IF)) {
			nextToken();
			expect(LPAR); nextToken();
			Expr cond = parseExp(1);
			expect(RPAR); nextToken();
			Stmt st1 = parseStmt();
			Stmt st2 = null;
			if (accept(ELSE)) {
				nextToken();
				st2 = parseStmt();
			}
			if (st2 != null) {
				out = new If(cond,st1,st2);
			} else {
				out = new If(cond,st1);
			}
		} else if (accept(RETURN)) {
			nextToken();
			Expr e = parseExp(0);
			if (e == null) {
				out = new Return();
			} else {
				out = new Return(e);
			}
			expect(SC); nextToken();
		} else {
			Expr e1 = parseExp(1);
			Expr e2 = null;
			if (accept(ASSIGN)) {
				nextToken();
				e2 = parseExp(1);
			}
			if (e2 == null) {
				out = new ExprStmt(e1);
			} else {
				out = new Assign(e1, e2);
			}
			expect(SC); nextToken();
		}
		return out;
	}

	private Expr parseExp(int i) {
		Expr out;
		out = parseLOB(i);
		out = parseStructArray(out);
		if (accept(OR)) {
			nextToken();
			out = new BinOp(out, parseExp(1), new Op(OpEnum.OR));
		}
		return out;
	}

	// Logic Or Block
	private Expr parseLOB(int i) {
		Expr out;
		out = parseLAB(i);
		if (accept(AND)) {
			nextToken();
			out = new BinOp(out, parseLOB(1), new Op(OpEnum.AND));
		}
		return out;
	}

	// Logic And Block
	private Expr parseLAB(int i) {
		Expr out;
		out = parseEQB(i);
		if (accept(EQ, NE)) {
			nextToken();
			out = new BinOp(out, parseLAB(1), new Op(accept(EQ)?OpEnum.NE:OpEnum.EQ));
		}
		return out;
	}

	// EQuality Block
	private Expr parseEQB(int i) {
		Expr out;
		out = parseCPB(i);
		if (accept(LE, GE, LT, GT)) {
			switch (token.tokenClass) {
				case LE:
					nextToken();
					out = new BinOp(out, parseEQB(1), new Op(OpEnum.LE));
					break;
				case GE:
					nextToken();
					out = new BinOp(out, parseEQB(1), new Op(OpEnum.GE));
					break;
				case LT:
					nextToken();
					out = new BinOp(out, parseEQB(1), new Op(OpEnum.LT));
					break;
				case GT:
					nextToken();
					out = new BinOp(out, parseEQB(1), new Op(OpEnum.GT));
					break;
			}
		}
		return out;
	}

	//ComParison Block
	private Expr parseCPB(int i) {
		Expr out;
		out = parseADB(i);
		if (accept(PLUS, MINUS)) {
			nextToken();
			out = new BinOp(out, parseCPB(1), new Op(accept(PLUS)?OpEnum.SUB:OpEnum.ADD));
		}
		return out;
	}

	// ADd Block
	private Expr parseADB(int i) {
		Expr out;
		out = parseMLB(i);
		if (accept(ASTERIX, DIV, REM)) {
			switch (token.tokenClass) {
				case ASTERIX:
					nextToken();
					out = new BinOp(out, parseADB(1), new Op(OpEnum.MUL));
					break;
				case DIV:
					nextToken();
					out = new BinOp(out, parseADB(1), new Op(OpEnum.DIV));
					break;
				case REM:
					nextToken();
					out = new BinOp(out, parseADB(1), new Op(OpEnum.MOD));
					break;
			}
		}
		return out;
	}

	// MuLtiplication Block
	private Expr parseMLB(int i) {
		Expr out = null;
		if (parseSizeOf()) {
			out = getSizeOf();
		} else if (parseVAt()) {
			out = getVAt();
		} else if (parseIdentorFunc()) {
			out = getIdentorFunc();
		} else if (parseLits()) {
			out = getLits(); nextToken();
		} else if (parseNeg()){
			out = getNeg();
		} else if (parseBracket()) {
			out = getBracket();
		} else {
			if (i != 0) {
				System.out.println("Parsing error: unexpected expression at " + token.position + ", with token: " + token.tokenClass);
				error++;
				lastErrorToken = token;
				nextToken();
			}
		}
		return out;
	}

	private boolean parseSizeOf() {
		if (accept(SIZEOF)) {
			nextToken();
			expect(LPAR); nextToken();
			parseType(1);
			return true;
		}
		return false;
	}

	private SizeOfExpr getSizeOf() {
		Type t = getType();
		expect(RPAR); nextToken();
		return new SizeOfExpr(t);
	}

	// Value At
	private boolean parseVAt() {
		if (accept(ASTERIX)) {
			nextToken();
			return true;
		}
		return false;
	}

	private ValueAtExpr getVAt() {
		return new ValueAtExpr(parseExp(1));
	}

	// Parse Field from identifier or functions
	private boolean parseIdentorFunc() {
		if (accept(IDENTIFIER)) {
			return true;
		}
		return false;
	}

	private Expr getIdentorFunc() {
		String name = token.data;
		nextToken();
		if (accept(LPAR)) {
			nextToken();
			List<Expr> args = new LinkedList<>();
			if (!accept(RPAR)) {
				args.add(parseExp(1));
				while (accept(COMMA)) {
					nextToken();
					args.add(parseExp(1));
				}
			}
			expect(RPAR); nextToken();
			return new FunCallExpr(name, args);
		}
		return new VarExpr(name);
	}

	private boolean parseLits() {
		if (accept(STRING_LITERAL, CHAR_LITERAL, INT_LITERAL)) {
			return true;
		}
		return false;
	}

	private Expr getLits() {
		if (accept(STRING_LITERAL)) {
			return new StrLiteral(token.data);
		} else if (accept(INT_LITERAL)) {
			return new IntLiteral(Integer.parseInt(token.data));
		} else if (accept(CHAR_LITERAL)) {
			return new ChrLiteral(token.data.charAt(0));
		}
		return null;
	}

	private boolean parseNeg() {
		if (accept(MINUS)) {
			nextToken();
			return true;
		}
		return false;
	}

	private Expr getNeg() {
		return new BinOp(new IntLiteral(-1), parseExp(1), new Op(OpEnum.MUL));
	}

	private boolean parseBracket() {
		if (accept(LPAR)) {
			nextToken();
			return true;
		}
		return false;
	}

	private Expr getBracket() {
		if (parseType(0)) {
			Type t = getType();
			expect(RPAR); nextToken();
			return new TypecaseExpr(t,parseExp(1));
		} else {
			Expr e = parseExp(1);
			expect(RPAR); nextToken();
			return e;
		}
	}

	//allows chained structs and arrays, eg a.a[1][2].a
	private Expr parseStructArray(Expr in) {
		if (accept(DOT)) {
			nextToken();
			expect(IDENTIFIER);
			String field = token.data;
			nextToken();
			return parseStructArray(new FieldAccessExpr(in, field));
		} else if (accept(LSBR)) {
			nextToken();
			Expr index = parseExp(1);
			expect(RSBR); nextToken();
			return parseStructArray(new ArrayAccessExpr(in, index));
		}
		return in;
	}
}
