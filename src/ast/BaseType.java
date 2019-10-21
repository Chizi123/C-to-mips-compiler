package ast;

public enum BaseType implements Type {
    INT(1), CHAR(2), VOID(3);

//    public BaseTypeEnum type;

//    public BaseType(BaseTypeEnum type) {
//        this.type = type;
//    }

    public int type;

    BaseType(int type) {
        this.type = type;
    }

    @Override
    public <T> T accept(ASTVisitor<T> v) {
        return v.visitBaseType(this);
    }
}