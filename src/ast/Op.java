package ast;

public enum Op implements ASTNode {
	ADD(1), SUB(2), MUL(3), DIV(4), MOD(5), GT(6), LT(7), GE(8), LE(9), NE(10), EQ(11), OR(12), AND(13);

	public int op;

	Op(int op) {
		this.op = op;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitOp(this);
	}
}