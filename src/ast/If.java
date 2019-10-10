package ast;

public class If extends Stmt {
	public Expr cond;
	public Stmt st1, st2;

	//IF
	public If(Expr cond, Stmt st1) {
		this.cond = cond;
		this.st1 = st1;
	}

	//IF ELSE
	public If(Expr cond, Stmt st1, Stmt st2) {
		this.cond = cond;
		this.st1 = st1;
		this.st2 = st2;
	}

	@Override
	public <T> T accept(ASTVisitor<T> v) {
		return v.visitIf(this);
	}
}
