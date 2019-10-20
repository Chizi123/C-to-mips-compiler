package sem;

import ast.*;

import java.util.ArrayDeque;
import java.util.Hashtable;
import java.util.LinkedList;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	private ArrayDeque<LinkedList<String>> vars;
	private Hashtable<String,LinkedList<String>> structs;

	@Override
	public Void visitBaseType(BaseType bt) {
		// To be completed...
		return null;
	}

	@Override
	public Void visitStructTypeDecl(StructTypeDecl sts) {
		boolean found = false;
		//struct already declared
		if (structs.keySet().contains(sts.st.name)) {
			error("Double declaration of struct "+sts.st.name);
		} else {
			structs.put(sts.st.name, new LinkedList<>());
			vars.getLast().add(sts.st.name);
		}

		//variable declaration doubling in structs
		for (VarDecl i : sts.varDeclList) {
			for (String j : structs.get(sts.st.name)) {
				if (j.equals(i.varName)) {
					error("Struct "+sts.st.name+" has the same field, "+j+" declared twice.");
					found = true;
				}
			}
			if (!found) {
				structs.get(sts.st.name).add(i.varName);
			}
		}
		// To be completed...
		return null;
	}

	@Override
	public Void visitBlock(Block b) {
		//add new scope level for block
		vars.push(new LinkedList<>());

		//check variable declaration in block
		for (VarDecl i : b.varDeclList) {
			i.accept(this);
		}

		//check variable usage in block
		for (Stmt i : b.stmtList) {
			i.accept(this);
		}

		//remove block from current scope
		vars.pop();
		// To be completed...
		return null;
	}

	@Override
	public Void visitFunDecl(FunDecl p) {
		// add function name to list of global identifiers
		boolean declared = false;
		for (String i : vars.getLast()) {
			if (i.equals(p.name)) {
				error("Double declaration of function: " + p.name);
				declared = true;
			}
		}
		if (!declared) {
			vars.getLast().add(p.name);
		}

		//add parameters to list of function parameters above global variables
		vars.push(new LinkedList<>());
		for (VarDecl i : p.params) {
			i.accept(this);
		}

		//visit block of function
		p.block.accept(this);

		//remove function parameters
		vars.pop();
		// To be completed...
		return null;
	}


	@Override
	public Void visitProgram(Program p) {
		structs = new Hashtable<>();
		vars = new ArrayDeque<>();
		vars.push(new LinkedList<>());
		add_canned_functions();

		for (StructTypeDecl i : p.structTypeDecls) {
			i.accept(this);
		}

		for (VarDecl i : p.varDecls) {
			i.accept(this);
		}

		for (FunDecl i : p.funDecls) {
			i.accept(this);
		}
		// To be completed...
		return null;
	}

	private void add_canned_functions() {
		vars.getLast().add("print_s");
		vars.getLast().add("print_i");
		vars.getLast().add("print_c");
		vars.getLast().add("read_c");
		vars.getLast().add("read_i");
		vars.getLast().add("mcmalloc");
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		boolean declared = false;
		for (String j : vars.getFirst()) {
			if (j.equals(vd.varName)) {
				error("Double variable declaration in block");
				declared = true;
			}
		}
		if (!declared) {
			vars.getFirst().add(vd.varName);
		}
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		boolean declared = false;
		for (LinkedList<String> i : vars) {
			if (i.contains(v.name)) {
				declared = true;
				break;
			}
		}
		if (!declared) {
			error("Undeclared variable "+v.name);
		}
		// To be completed...
		return null;
	}

	// To be completed...


	public NameAnalysisVisitor() {
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
	public Void visitPointerType(PointerType pt) {
		return null;
	}

	@Override
	public Void visitStructType(StructType st) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayType at) {
		return null;
	}

	@Override
	public Void visitIntLiteral(IntLiteral il) {
		return null;
	}

	@Override
	public Void visitStringLiteral(StrLiteral sl) {
		return null;
	}

	@Override
	public Void visitChrLiteral(ChrLiteral cl) {
		return null;
	}

	@Override
	public Void visitFunCallExpr(FunCallExpr fce) {
		if (!vars.getLast().contains(fce.name)) {
			for (String i : vars.getLast()) {
				System.out.println(i);
			}
			error("Function call to undeclared function "+fce.name);
		}
		return null;
	}

	@Override
	public Void visitBinOp(BinOp bo) {
		bo.E1.accept(this);
		bo.E2.accept(this);
		return null;
	}

	@Override
	public Void visitOp(Op o) {
		return null;
	}

	@Override
	public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
		aae.exp.accept(this);
		aae.index.accept(this);
		return null;
	}

	@Override
	public Void visitFieldAccessExpr(FieldAccessExpr fae) {
		fae.struct.accept(this);
		return null;
	}

	@Override
	public Void visitValueAtExpr(ValueAtExpr vae) {
		vae.exp.accept(this);
		return null;
	}

	@Override
	public Void visitSizeOfExpr(SizeOfExpr soe) {
		return null;
	}

	@Override
	public Void visitTypecastExpr(TypecaseExpr te) {
		te.exp.accept(this);
		return null;
	}

	@Override
	public Void visitExprStmt(ExprStmt es) {
		es.exp.accept(this);
		return null;
	}

	@Override
	public Void visitWhile(While w) {
		w.cond.accept(this);
		w.loop.accept(this);
		return null;
	}

	@Override
	public Void visitIf(If i) {
		i.cond.accept(this);
		i.st1.accept(this);
		if (i.st2!=null)
			i.st2.accept(this);
		return null;
	}

	@Override
	public Void visitAssign(Assign a) {
		a.e1.accept(this);
		a.e2.accept(this);
		return null;
	}

	@Override
	public Void visitReturn(Return r) {
		if (r.exp!=null)
			r.exp.accept(this);
		return null;
	}
}
