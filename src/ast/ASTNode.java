package ast;

public interface ASTNode {
    <T> T accept(ASTVisitor<T> v);
}
