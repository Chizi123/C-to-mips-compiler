package sem;

import ast.*;

import java.util.ArrayDeque;
import java.util.Hashtable;
import java.util.LinkedList;

public class NameAnalysisVisitor extends BaseSemanticVisitor<Void> {

	private ArrayDeque<LinkedList<funvar>> vars;
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
			return null;
		} else {
			structs.put(sts.st.name, new LinkedList<>());
//			vars.getLast().add(new funvar(sts.st.name));
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

	private boolean fun2block;
	@Override
	public Void visitBlock(Block b) {
		//add new scope level for block
		if (fun2block) {
			fun2block = false;
		} else {
			vars.push(new LinkedList<>());
		}

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
		for (funvar i : vars.getLast()) {
			if (i.name.equals(p.name)) {
				error("Double declaration of function with name: " + p.name);
				declared = true;
			}
		}
		if (!declared) {
			vars.getLast().add(new funvar(p.name,p));
		}

		//add parameters to list of function parameters above global variables
		vars.push(new LinkedList<>());
		for (VarDecl i : p.params) {
			i.accept(this);
		}

		//visit block of function
		fun2block = true;
		p.block.accept(this);

		//remove function parameters
//		vars.pop();
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
		vars.getLast().add(new funvar("print_s", new FunDecl(BaseType.VOID,"print_s",new LinkedList<>(),new Block(new LinkedList<>(),new LinkedList<>()))));
		vars.getLast().getLast().fun.params.add(new VarDecl(new PointerType(BaseType.CHAR), "s"));
		vars.getLast().add(new funvar("print_i", new FunDecl(BaseType.VOID,"print_i",new LinkedList<>(),new Block(new LinkedList<>(),new LinkedList<>()))));
		vars.getLast().getLast().fun.params.add(new VarDecl(BaseType.INT, "i"));
		vars.getLast().add(new funvar("print_c", new FunDecl(BaseType.VOID,"print_c",new LinkedList<>(),new Block(new LinkedList<>(),new LinkedList<>()))));
		vars.getLast().getLast().fun.params.add(new VarDecl(BaseType.CHAR, "c"));
		vars.getLast().add(new funvar("read_c", new FunDecl(BaseType.CHAR,"read_c",new LinkedList<>(),new Block(new LinkedList<>(),new LinkedList<>()))));
		vars.getLast().add(new funvar("read_i", new FunDecl(BaseType.INT,"read_i",new LinkedList<>(),new Block(new LinkedList<>(),new LinkedList<>()))));
		vars.getLast().add(new funvar("mcmalloc", new FunDecl(new PointerType(BaseType.VOID),"mcmalloc",new LinkedList<>(),new Block(new LinkedList<>(),new LinkedList<>()))));
	}

	@Override
	public Void visitVarDecl(VarDecl vd) {
		boolean declared = false;
		for (funvar j : vars.getFirst()) {
			if (j.name.equals(vd.varName)) {
				error("Double variable declaration of variable "+j.name);
				declared = true;
			}
		}
		if (!declared) {
			vars.getFirst().add(new funvar(vd.varName,vd));
		}
		// To be completed...
		return null;
	}

	@Override
	public Void visitVarExpr(VarExpr v) {
		boolean declared = false;
		found:
		for (LinkedList<funvar> i : vars) {
			for (funvar j : i) {
				if (j.name.equals(v.name)) {
					if (j.var == null) {
						error("Trying to shadow function +"+j.name+"with a variable");
					} else {
						v.vd = j.var;
						declared = true;
						break found;
					}
				}
			}
		}
		if (!declared) {
			error("Undeclared variable "+v.name);
			v.vd = new VarDecl(BaseType.VOID,v.name);
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
		boolean declared = false;
		for (funvar i : vars.getLast()) {
			if (i.name.equals(fce.name) && i.fun != null) {
				declared = true;
				fce.fd = i.fun;
			}
		}
		if (!declared) {
			error("Function call to undeclared function "+fce.name);
		}
		for (Expr i : fce.args) {
			i.accept(this);
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
	public Void visitTypecastExpr(TypecastExpr te) {
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

class funvar {
	public String name;
	public FunDecl fun;
	public VarDecl var;

	public funvar(String name, FunDecl fun) {
		this.name = name;
		this.fun = fun;
	}

	public funvar(String name, VarDecl var) {
		this.name = name;
		this.var = var;
	}

//	public funvar(String name) {
//		this.name = name;
//	}
}