package ast;

public class ArrayAccessExpr extends Expr {
	public Expr exp, index;

	public ArrayAccessExpr(Expr exp, Expr index) {
		this.exp = exp;
		this.index = index;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitArrayAccessExpr(this);
	}
}
