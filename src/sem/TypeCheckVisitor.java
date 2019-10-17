package sem;

import ast.*;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitBlock(Block b) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitFunDecl(FunDecl p) {
		// To be completed...
		return null;
	}


	@Override
	public Type visitProgram(Program p) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarDecl(VarDecl vd) {
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		// To be completed...
		return null;
	}

	// To be completed...


	public TypeCheckVisitor() {
		super();
	}

	@Override
	public int getErrorCount() {
		return super.getErrorCount();
	}

	@Override
	protected void error(String message) {
		super.error(message);
	}

	@Override
	public Type visitPointerType(PointerType pt) {
		return null;
	}

	@Override
	public Type visitStructType(StructType st) {
		return null;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		return null;
	}

	@Override
	public Type visitIntLiteral(IntLiteral il) {
		return null;
	}

	@Override
	public Type visitStringLiteral(StrLiteral sl) {
		return null;
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		return null;
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		return null;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
		return null;
	}

	@Override
	public Type visitOp(Op o) {
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		return null;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		return null;
	}

	@Override
	public Type visitTypecastExpr(TypecaseExpr te) {
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
		return null;
	}

	@Override
	public Type visitWhile(While w) {
		return null;
	}

	@Override
	public Type visitIf(If i) {
		return null;
	}

	@Override
	public Type visitAssign(Assign a) {
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		return null;
	}
}
