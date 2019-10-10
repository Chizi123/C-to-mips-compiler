package ast;

public class Return extends Stmt {
	public Expr exp;

	public Return() {
	}

	public Return(Expr exp) {
		this.exp = exp;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitReturn(this);
	}
}
