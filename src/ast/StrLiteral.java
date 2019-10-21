package ast;

public class StrLiteral extends Expr {
	public String string;

	public StrLiteral(String string) {
		this.string = string;
		this.type = new ArrayType(new BaseType(BaseTypeEnum.CHAR),string.length()+1);
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitStringLiteral(this);
	}
}
