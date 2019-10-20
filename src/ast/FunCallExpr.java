package ast;

import java.util.List;

public class FunCallExpr extends Expr{
	public String name;
	public List<Expr> args;
	public FunDecl fd;

	public FunCallExpr(String name, List<Expr> args) {
		this.name = name;
		this.args = args;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitFunCallExpr(this);
	}
}
