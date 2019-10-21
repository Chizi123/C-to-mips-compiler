package sem;

import ast.*;

import java.util.Hashtable;

public class TypeCheckVisitor extends BaseSemanticVisitor<Type> {

	private Hashtable<String, StructTypeDecl> structs;

	@Override
	public Type visitBaseType(BaseType bt) {
		// To be completed...
		return bt;
	}

	@Override
	public Type visitStructTypeDecl(StructTypeDecl st) {
		structs.put(st.st.name, st);

		for (VarDecl i : st.varDeclList) {
			i.accept(this);
		}
		// To be completed...
		return null;
	}

	@Override
	public Type visitBlock(Block b) {
		for (VarDecl i : b.varDeclList) {
			i.accept(this);
		}

		for (Stmt i : b.stmtList) {
			i.accept(this);
		}
		// To be completed...
		return null;
	}

	private Type funretT;
	@Override
	public Type visitFunDecl(FunDecl p) {
		funretT = p.type;
		//check for void parameters
		for (VarDecl i : p.params) {
			i.accept(this);
		}
		// To be completed...
		p.block.accept(this);
		return null;
	}

	@Override
	public Type visitProgram(Program p) {
		structs = new Hashtable<>();
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

	@Override
	public Type visitVarDecl(VarDecl vd) {
		//check for void variables
		if (vd.type.accept(this) == BaseType.VOID) {
			error("Declaration of variable "+vd.varName+" of type \"VOID\"");
		}
		// To be completed...
		return null;
	}

	@Override
	public Type visitVarExpr(VarExpr v) {
		v.type = v.vd.type;
		// To be completed...
		return v.type;
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
		if (pt.type instanceof StructType) {
			if (!(structs.containsKey(((StructType) pt.type).name))) {
				error("Pointer to undeclared struct type "+((StructType) pt.type).name);
			}
		}
		return pt;
	}

	@Override
	public Type visitStructType(StructType st) {
		if (!(structs.containsKey(st.name))) {
			error("Use of undeclared struct type "+st.name);
		}
		return st;
	}

	@Override
	public Type visitArrayType(ArrayType at) {
		return at;
	}

	@Override
	public Type visitIntLiteral(IntLiteral il) {
		return il.type.accept(this);
	}

	@Override
	public Type visitStringLiteral(StrLiteral sl) {
		//unsure about string length
		return sl.type.accept(this);
	}

	@Override
	public Type visitChrLiteral(ChrLiteral cl) {
		return cl.type.accept(this);
	}

	@Override
	public Type visitFunCallExpr(FunCallExpr fce) {
		fce.type = fce.fd.type;
		return fce.type;
	}

	@Override
	public Type visitBinOp(BinOp bo) {
		Type e1 = bo.E1.accept(this);
		Type e2 = bo.E2.accept(this);
		if (bo.op == Op.NE || bo.op == Op.EQ) {
			if ((e1 instanceof StructType || e1 instanceof ArrayType || e1.accept(this) == BaseType.VOID) &&
					(e2 instanceof StructType || e2 instanceof ArrayType || e2.accept(this) == BaseType.VOID) ||
					(e1.accept(this) != e2.accept(this))) {
				error("Bad argument types for equality comparison");
			} else {
				bo.type = BaseType.INT;
				return bo.type;
			}
		} else {
			if (e1.accept(this) == BaseType.INT && e2.accept(this) == BaseType.INT) {
				bo.type = BaseType.INT;
				return bo.type;
			} else {
				error("BinOp with expressions that aren't INT");
				return null;
			}
		}
		return bo.type;
	}

	@Override
	public Type visitOp(Op o) {
		return null;
	}

	@Override
	public Type visitArrayAccessExpr(ArrayAccessExpr aae) {
		aae.exp.accept(this);
		if ((aae.exp.type instanceof ArrayType || aae.exp.type instanceof PointerType) && aae.index.type.accept(this) == BaseType.INT) {
			aae.type = aae.exp.type.accept(this);
			return aae.type;
		} else {
			error("Array access to instance not array or pointer");
		}
		return null;
	}

	@Override
	public Type visitFieldAccessExpr(FieldAccessExpr fae) {
		Type t = fae.struct.accept(this);
		if (t instanceof PointerType) {
			error("Trying to use a pointer without accessing its value");
			return null;
		}
		StructTypeDecl f = structs.get((((StructType) t).name));
		fae.type = null;
		for (VarDecl i: f.varDeclList) {
			if (i.varName.equals(fae.field)) {
				fae.type = i.type;
				break;
			}
		}
		if (fae.type == null) {
			error("Accessing field "+fae.field+" on struct "+f.st.name+"which doesn't exist");
		}
		return fae.type;
	}

	@Override
	public Type visitValueAtExpr(ValueAtExpr vae) {
		if (vae.exp.accept(this) instanceof PointerType) {
			vae.type = vae.exp.type;
			return vae.type;
		} else {
			error("Pointer reference to instance not pointer");
		}
		return null;
	}

	@Override
	public Type visitSizeOfExpr(SizeOfExpr soe) {
		return BaseType.INT;
	}

	@Override
	public Type visitTypecastExpr(TypecastExpr te) {
		if (te.type.accept(this) == BaseType.INT && te.exp.type.accept(this) == BaseType.CHAR) {
			return BaseType.INT;
		} else if (te.exp.type instanceof ArrayType) {
			if ((te.type) == ((ArrayType) te.exp.type).type) {
				return new PointerType(te.type);
			} else {
				error("Casting array to pointer of different type");
			}
		} else if (te.exp.type instanceof PointerType) {
			return new PointerType(te.type);
		} else {
			error("Invalid Type cast");
		}
		return null;
	}

	@Override
	public Type visitExprStmt(ExprStmt es) {
		return null;
	}

	@Override
	public Type visitWhile(While w) {
		if (w.cond.accept(this) != BaseType.INT) {
			error("While condition not int");
		}
		return null;
	}

	@Override
	public Type visitIf(If i) {
		if (i.cond.accept(this) != BaseType.INT) {
			error("If condition not int");
		}
		return null;
	}

	@Override
	public Type visitAssign(Assign a) {
		Type e1 = a.e1.accept(this);
		Type e2 = a.e2.accept(this);
		if ((e1.accept(this) == BaseType.VOID || e1 instanceof ArrayType) &&
				(e2.accept(this) == BaseType.VOID || e2 instanceof ArrayType)) {
			error("Assignment of Void or Array Types");
		}
		return null;
	}

	@Override
	public Type visitReturn(Return r) {
		if (funretT.accept(this) == BaseType.VOID) {
			if (r.exp != null) {
				error("Trying to return value from void function");
			}
		} else {
			if (r.exp == null) {
				error("no return value specified");
			} else if (funretT.accept(this) != r.exp.accept(this).accept(this)) {
				error("Returning wrong type from function");
			}
		}
		return null;
	}
}
