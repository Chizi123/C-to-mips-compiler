package ast;

public class StrLiteral extends Expr {
	public String string;

	public StrLiteral(String string) {
		this.string = string;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitStringLiteral(this);
	}
}
