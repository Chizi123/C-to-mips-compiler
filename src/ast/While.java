package ast;

public class While extends Stmt {
	public Expr cond;
	public Stmt loop;

	public While(Expr cond, Stmt loop) {
		this.cond = cond;
		this.loop = loop;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitWhile(this);
	}
}
