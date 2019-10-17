package ast;

public class BaseType implements Type {
    public BaseTypeEnum type;

    public BaseType(BaseTypeEnum type) {
        this.type = type;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }
}