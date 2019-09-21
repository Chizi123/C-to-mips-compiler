package lexer;

import lexer.Token.TokenClass;

import java.io.EOFException;
import java.io.IOException;

/**
 * @author cdubach
 */
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
        System.out.println("Lexing error: unrecognised character ("+c+") at "+line+":"+col);
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

    /*
     * To be completed
     */
    private Token next() throws IOException {

        int line = scanner.getLine();
        int column = scanner.getColumn();

        // get the next character
        char c = scanner.next();

        // skip white spaces
        if (Character.isWhitespace(c))
            return next();

        // recognises the plus operator
        if (c == '+')
            return new Token(TokenClass.PLUS, line, column);

        // ... to be completed
        // Comments
        // Line Comment
        if (c == '/' && scanner.peek() == '/') {
            while (line == scanner.getLine()) scanner.next();
            return next();
        }
        //Block Comment
        if (c == '/' && scanner.peek() == '*') {
            int sLine = line;
            int sCol = column;
            scanner.next();
            column++;
            try {
                while (true) {
                    if (c == '*' && scanner.peek() == '/') break;
                    c = scanner.next();
                    if (c == '\n') {
                        line++;
                        column = 0;
                    }
                    else {
                        column++;
                    }
                }
            } catch (EOFException e) {
                System.out.println("Unfinished block comment starting at " + sLine + ":" + sCol);
                error++;
            }
            scanner.next();
            column++;
            return next();
        }

        // Math ops
        if (c == '-') return new Token(TokenClass.MINUS, line, column);
        if (c == '*') return new Token(TokenClass.ASTERIX, line, column);
        if (c == '/') return new Token(TokenClass.DIV, line, column);
        if (c == '%') return new Token(TokenClass.REM, line, column);
        if (c == '.') return new Token(TokenClass.DOT, line, column);

        // Delimiters
        if (c == '{') return new Token(TokenClass.LBRA, line, column);
        if (c == '}') return new Token(TokenClass.RBRA, line, column);
        if (c == '(') return new Token(TokenClass.LPAR, line, column);
        if (c == ')') return new Token(TokenClass.RPAR, line, column);
        if (c == '[') return new Token(TokenClass.LSBR, line, column);
        if (c == ']') return new Token(TokenClass.RSBR, line, column);
        if (c == ';') return new Token(TokenClass.SC, line, column);
        if (c == ',') return new Token(TokenClass.COMMA, line, column);

        // Logical operators
        if (c == '&' && scanner.peek() == '&') {
            scanner.next();
            return new Token(TokenClass.AND, line, column);
        }
        if (c == '|' && scanner.peek() == '|') {
            scanner.next();
            return new Token(TokenClass.OR, line, column);
        }

        // Comparisons
        if (c == '=' && scanner.next() == '=') {
            scanner.next();
            return new Token(TokenClass.EQ, line, column);
        }
        if (c == '!' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.NE, line, column);
        }
        if (c == '<' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.LE, line, column);
        }
        if (c == '<') return new Token(TokenClass.LT, line, column);
        if (c == '>' && scanner.peek() == '=') {
            scanner.next();
            return new Token(TokenClass.GE, line, column);
        }
        if (c == '>') return new Token(TokenClass.GT, line, column);

        // Assignment
        if (c == '=') return new Token(TokenClass.ASSIGN, line, column);

        StringBuilder out = new StringBuilder();
        // Identifier, Types and Keywords
        if (Character.isLetter(c) || c == '_') {
            out.append(c);
            c = scanner.peek();
            while (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
                c = scanner.next();
                out.append(c);
                column++;
                c = scanner.peek();
            }
            switch (out.toString()) {
                case "int":
                    return new Token(TokenClass.INT, line, column-2);
                case "void":
                    return new Token(TokenClass.VOID, line, column-3);
                case "char":
                    return new Token(TokenClass.CHAR, line, column-3);
                case "if":
                    return new Token(TokenClass.IF, line, column-1);
                case "else":
                    return new Token(TokenClass.ELSE, line, column-3);
                case "while":
                    return new Token(TokenClass.WHILE, line, column-4);
                case "return":
                    return new Token(TokenClass.RETURN, line, column-5);
                case "struct":
                    return new Token(TokenClass.STRUCT, line, column-5);
                case "sizeof":
                    return new Token(TokenClass.SIZEOF, line, column-5);
                default:
                    return new Token(TokenClass.IDENTIFIER, out.toString(), line, column+1-out.toString().length());
            }
        }

        // Literals
        if (c == '\"') {
            while ((c = scanner.next()) != '\"') {
                if (c == '\\')
                    c = scanner.next();
                out.append(c);
                column++;
            }
            return new Token(TokenClass.STRING_LITERAL, out.toString(), line, column);
        }
        if (Character.isDigit(c)) {
            out.append(c);
            c = scanner.peek();
            while (Character.isDigit(c)) {
                c = scanner.next();
                out.append(c);
                c = scanner.peek();
                column++;
            }
            return new Token(TokenClass.INT_LITERAL, out.toString(), line, column);
        }
        if (c == '\'') {
            c = scanner.next();
            column++;
            if (c == '\\') {
                c = scanner.next();
                column++;
                switch (c) {
                    case 'n':
                        c = '\n';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case 'b':
                        c = '\b';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 'f':
                        c = '\f';
                        break;
                    case '0':
                        c = '\0';
                        break;
                    default: // '\'', '\"', '\\', other sequences where the second character is the wanted one
                        break;
                }
            }
            char temp = scanner.next();
            column++;
            if (temp == '\'') {
                return new Token(TokenClass.CHAR_LITERAL, Character.toString(c), line, column);
            } else {
                System.out.println("Bad character definition at "+line+":"+column);
                error++;
                return new Token(TokenClass.INVALID, line, column);
            }
        }

        // Include
        if (c == '#') {
            int hcol = column;
            do {
                out.append(c);
                c = scanner.next();
                column++;
            } while (Character.isLetter(c));
            switch (out.toString()) {
                case "#include":
                    return new Token(TokenClass.INCLUDE, line, column-6);
                default:
                    System.out.println("Unrecognised # statement at "+line+":"+hcol);
                    error++;
                    return new Token(TokenClass.INVALID, line, column);
            }
        }

//        // Reference pointer - Not needed
//        if (c == '&') return new Token(TokenClass.REF, line, column);

//        // Label
//        if (c == ':') return new Token(TokenClass.COL, line, column);

        // if we reach this point, it means we did not recognise a valid token
        error(c, line, column);
        return new Token(TokenClass.INVALID, line, column);
    }
}
