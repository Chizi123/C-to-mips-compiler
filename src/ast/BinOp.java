package ast;

public class BinOp extends Expr {
	public Expr E1, E2;
	public Op op;

	public BinOp(Expr e1, Expr e2, Op op) {
		E1 = e1;
		E2 = e2;
		this.op = op;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitBinOp(this);
	}
}
