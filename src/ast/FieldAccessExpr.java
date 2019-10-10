package ast;

public class FieldAccessExpr extends Expr {
	public Expr struct;
	public String field;

	public FieldAccessExpr(Expr struct, String field) {
		this.struct = struct;
		this.field = field;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitFieldAccessExpr(this);
	}
}
