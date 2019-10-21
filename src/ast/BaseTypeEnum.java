package ast;

public enum BaseTypeEnum implements Type{
	INT, CHAR, VOID;

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return null;
	}
}
