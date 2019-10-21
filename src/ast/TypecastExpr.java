package ast;

public class TypecastExpr extends Expr {
	public Type type;
	public Expr exp;

	public TypecastExpr(Type type, Expr exp) {
		this.type = type;
		this.exp = exp;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitTypecastExpr(this);
	}
}
