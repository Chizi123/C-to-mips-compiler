package ast;

public class ExprStmt extends Stmt {
	public Expr exp;

	public ExprStmt(Expr exp) {
		this.exp = exp;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitExprStmt(this);
	}
}
