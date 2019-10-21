package ast;

public interface Type extends ASTNode {

    <T> T accept(ASTVisitor<T> v);

}
