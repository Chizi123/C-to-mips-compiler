package ast;

public class ValueAtExpr extends Expr {
	public Expr exp;

	public ValueAtExpr(Expr exp) {
		this.exp = exp;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitValueAtExpr(this);
	}
}
