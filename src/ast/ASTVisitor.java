package ast;

public interface ASTVisitor<T> {
    T visitBaseType(BaseType bt);
    T visitStructTypeDecl(StructTypeDecl st);
    T visitBlock(Block b);
    T visitFunDecl(FunDecl p);
    T visitProgram(Program p);
    T visitVarDecl(VarDecl vd);
    T visitVarExpr(VarExpr v);

    // to complete ... (should have one visit method for each concrete AST node class)
    T visitPointerType(PointerType pt);
    T visitStructType(StructType st);
    T visitArrayType(ArrayType at);
    T visitIntLiteral(IntLiteral il);
    T visitStringLiteral(StrLiteral sl);
    T visitChrLiteral(ChrLiteral cl);
    T visitFunCallExpr(FunCallExpr fce);
    T visitBinOp(BinOp bo);
    T visitOp(Op o);
    T visitArrayAccessExpr(ArrayAccessExpr aae);
    T visitFieldAccessExpr(FieldAccessExpr fae);
    T visitValueAtExpr(ValueAtExpr vae);
    T visitSizeOfExpr(SizeOfExpr soe);
    T visitTypecastExpr(TypecastExpr te);
    T visitExprStmt(ExprStmt es);
    T visitWhile(While w);
    T visitIf(If i);
    T visitAssign(Assign a);
    T visitReturn(Return r);
}
