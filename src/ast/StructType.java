package ast;

public class StructType implements Type{
	public String name;

	public StructType(String name) {
		this.name = name;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitStructType(this);
	}
}
