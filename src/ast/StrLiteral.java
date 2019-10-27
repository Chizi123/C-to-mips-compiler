package ast;

public class StrLiteral extends Expr {
	public String string;
	public int id;

	public StrLiteral(String string) {
		this.string = string;
		this.type = new ArrayType(BaseType.CHAR,string.length()+1);
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitStringLiteral(this);
	}
}
