package ast;

public class ChrLiteral extends Expr {
	public char c;

	public ChrLiteral(char c) {
		this.c = c;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitChrLiteral(this);
	}
}
