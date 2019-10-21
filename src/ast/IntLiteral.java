package ast;

public class IntLiteral extends Expr {
	public int number;

	public IntLiteral(int number) {
		this.number = number;
		this.type = BaseType.INT;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitIntLiteral(this);
	}
}
