package ast;

public class PointerType implements Type {
	public Type type;

	public PointerType(Type type) {
		this.type = type;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitPointerType(this);
	}
}
