package ast;

public class ArrayType implements Type {
	public Type type;
	public int size;

	public ArrayType(Type type, int size) {
		this.type = type;
		this.size = size;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitArrayType(this);
	}
}
