package ast;

import java.io.PrintWriter;

public class ASTPrinter implements ASTVisitor<Void> {

    private PrintWriter writer;

    public ASTPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public Void visitBlock(Block b) {
        writer.print("Block(");
        String delimiter = "";
        for (VarDecl v : b.varDeclList) {
            writer.print(delimiter);
            v.accept(this);
            delimiter=",";
        }
        for (Stmt s : b.stmtList) {
            writer.print(delimiter);
            s.accept(this);
            delimiter=",";
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunDecl(FunDecl fd) {
        writer.print("FunDecl(");
        fd.type.accept(this);
        writer.print(","+fd.name+",");
        for (VarDecl vd : fd.params) {
            vd.accept(this);
            writer.print(",");
        }
        fd.block.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitProgram(Program p) {
        writer.print("Program(");
        String delimiter = "";
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.print(delimiter);
            delimiter = ",";
            std.accept(this);
        }
        for (VarDecl vd : p.varDecls) {
            writer.print(delimiter);
            delimiter = ",";
            vd.accept(this);
        }
        for (FunDecl fd : p.funDecls) {
            writer.print(delimiter);
            delimiter = ",";
            fd.accept(this);
        }
        writer.print(")");
	    writer.flush();
        return null;
    }

    @Override
    public Void visitVarDecl(VarDecl vd){
        writer.print("VarDecl(");
        vd.type.accept(this);
        writer.print(","+vd.varName);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitVarExpr(VarExpr v) {
        writer.print("VarExpr(");
        writer.print(v.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBaseType(BaseType bt) {
        switch (bt.type) {
            case 1:
                writer.print("INT");
                break;
            case 2:
                writer.print("CHAR");
                break;
            case 3:
                writer.print("VOID");
                break;
        }
        return null;
    }

    @Override
    public Void visitStructTypeDecl(StructTypeDecl st) {
        writer.print("StructTypeDecl(");
        st.st.accept(this);
        for (VarDecl v : st.varDeclList) {
            writer.print(",");
            v.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStructType(StructType st) {
        writer.print("StructType(");
        writer.print(st.name);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitPointerType(PointerType pt) {
        writer.print("PointerType(");
        pt.type.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitArrayType(ArrayType at) {
        writer.print("ArrayType(");
        at.type.accept(this);
        writer.print(",");
        writer.print(at.size);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIntLiteral(IntLiteral il) {
        writer.print("IntLiteral(");
        writer.print(il.number);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitStringLiteral(StrLiteral sl) {
        writer.print("StrLiteral(");
        writer.print(sl.string);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitChrLiteral(ChrLiteral cl) {
        writer.print("ChrLiteral(");
        writer.print(cl.c);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFunCallExpr(FunCallExpr fce) {
        writer.print("FunCallExpr(");
        writer.print(fce.name);
        if (fce.args.size()!=0) {
            writer.print(",");
            fce.args.get(0).accept(this);
            for (Expr e : fce.args.subList(1,fce.args.size())) {
                writer.print(",");
                e.accept(this);
            }
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitBinOp(BinOp bo) {
        writer.print("BinOp(");
        bo.E1.accept(this);
        writer.print(",");
        bo.op.accept(this);
        writer.print(",");
        bo.E2.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitOp(Op o) {
        switch (o.op) {
            case 1:
                writer.print("ADD");
                break;
            case 2:
                writer.print("SUB");
                break;
            case 3:
                writer.print("MUL");
                break;
            case 4:
                writer.print("DIV");
                break;
            case 5:
                writer.print("MOD");
                break;
            case 6:
                writer.print("GT");
                break;
            case 7:
                writer.print("LT");
                break;
            case 8:
                writer.print("GE");
                break;
            case 9:
                writer.print("LE");
                break;
            case 10:
                writer.print("NE");
                break;
            case 11:
                writer.print("EQ");
                break;
            case 12:
                writer.print("OR");
                break;
            case 13:
                writer.print("AND");
                break;
        }
        return null;
    }

    @Override
    public Void visitArrayAccessExpr(ArrayAccessExpr aae) {
        writer.print("ArrayAccessExpr(");
        aae.exp.accept(this);
        writer.print(",");
        aae.index.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitFieldAccessExpr(FieldAccessExpr fae) {
        writer.print("FieldAccessExpr(");
        fae.struct.accept(this);
        writer.print(",");
        writer.print(fae.field);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitValueAtExpr(ValueAtExpr vae) {
        writer.print("ValueAtExpr(");
        vae.exp.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitSizeOfExpr(SizeOfExpr soe) {
        writer.print("SizeOfExpr(");
        soe.type.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitTypecastExpr(TypecastExpr te) {
        writer.print("TypecastExpr(");
        te.type.accept(this);
        writer.print(",");
        te.exp.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitExprStmt(ExprStmt es) {
        writer.print("ExprStmt(");
        es.exp.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitWhile(While w) {
        writer.print("While(");
        w.cond.accept(this);
        writer.print(",");
        w.loop.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitIf(If i) {
        writer.print("If(");
        i.cond.accept(this);
        writer.print(",");
        i.st1.accept(this);
        if (i.st2 != null) {
            writer.print(",");
            i.st2.accept(this);
        }
        writer.print(")");
        return null;
    }

    @Override
    public Void visitAssign(Assign a) {
        writer.print("Assign(");
        a.e1.accept(this);
        writer.print(",");
        a.e2.accept(this);
        writer.print(")");
        return null;
    }

    @Override
    public Void visitReturn(Return r) {
        writer.print("RETURN(");
        if (r.exp != null)
            r.exp.accept(this);
        writer.print(")");
        return null;
    }
}
