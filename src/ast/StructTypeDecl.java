package ast;

import java.util.List;

public class StructTypeDecl implements ASTNode {
    public StructType st;
    public List<VarDecl> varDeclList;

    public StructTypeDecl(StructType st, List<VarDecl> varDeclList) {
        this.st = st;
        this.varDeclList = varDeclList;
    }

    public <T> T accept(ASTVisitor<T> v) {
        return v.visitStructTypeDecl(this);
    }

}
