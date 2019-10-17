package ast;

public class Op implements ASTNode {
	public OpEnum op;

	public Op(OpEnum op) {
		this.op = op;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitOp(this);
	}
}