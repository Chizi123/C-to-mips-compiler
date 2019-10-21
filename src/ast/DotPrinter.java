package ast;

import java.io.PrintWriter;

public class DotPrinter implements ASTVisitor<String> {

    private PrintWriter writer;
    private int NodeCnt;
    private FieldAccessExpr fae;

    public DotPrinter(PrintWriter writer) {
            this.writer = writer;
    }

    @Override
    public String visitBlock(Block b) {
        String BlockNodeId = "Node" + NodeCnt++;
        writer.println(BlockNodeId + " [label=\"Block\"];");
        for (VarDecl v : b.varDeclList) {
            writer.println(BlockNodeId+" -> "+v.accept(this)+";");
        }
        for (Stmt s : b.stmtList) {
            writer.println(BlockNodeId+" -> "+s.accept(this)+";");
        }
        return BlockNodeId;
    }

    @Override
    public String visitFunDecl(FunDecl fd) {
        String FunDeclId = "Node"+NodeCnt++;
        writer.println(FunDeclId+" [label=\"Function("+fd.name+")\"];");
        writer.println(FunDeclId+" -> "+fd.type.accept(this)+";");
        for (VarDecl vd : fd.params) {
            writer.println(FunDeclId+" -> "+vd.accept(this)+";");
        }
        writer.println(FunDeclId+" -> "+fd.block.accept(this)+";");
        return FunDeclId;
    }

    @Override
    public String visitProgram(Program p) {
        writer.println("digraph ast {");
        NodeCnt=0;
        String ProgramId = "Node"+NodeCnt++;
        writer.println(ProgramId+" [label=\"Program\"];");
        for (StructTypeDecl std : p.structTypeDecls) {
            writer.println(ProgramId+" -> "+std.accept(this)+";");
        }
        for (VarDecl vd : p.varDecls) {
            writer.println(ProgramId+" -> "+vd.accept(this)+";");
        }
        for (FunDecl fd : p.funDecls) {
            writer.println(ProgramId+" -> "+fd.accept(this)+";");
        }
        writer.println("}");
	    writer.flush();
        return ProgramId;
    }

    @Override
    public String visitVarDecl(VarDecl vd){
        String VDId = "Node"+NodeCnt++;
        writer.println(VDId+" [label=\"VarDecl("+vd.varName+")\"];");
        writer.println(VDId+" -> "+vd.type.accept(this)+";");
        return VDId;
    }

    @Override
    public String visitVarExpr(VarExpr v) {
        writer.println("Node"+NodeCnt+" [label=\"VarExpr("+v.name+")\"];");
        return "Node"+NodeCnt++;
    }

    @Override
    public String visitBaseType(BaseType bt) {
        writer.print("Node"+NodeCnt+" [label=\"Type(");
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
        writer.println(")\"];");
        return "Node"+NodeCnt++;
    }

    @Override
    public String visitStructTypeDecl(StructTypeDecl st) {
        String STDId = "Node"+NodeCnt++;
        writer.println(STDId+" [label=\"StructTypeDecl("+st.st.name+")\"];");
        for (VarDecl v : st.varDeclList) {
            writer.println(STDId+" -> "+v.accept(this)+";");
        }
        return STDId;
    }

    @Override
    public String visitStructType(StructType st) {
        String STId = "Node"+NodeCnt++;
        writer.println(STId+" [label=\"StructType("+st.name+")\"];");
        return STId;
    }

    @Override
    public String visitPointerType(PointerType pt) {
        String PTId = "Node"+NodeCnt++;
        writer.println(PTId+" [label=\"PointerType\"];");
        writer.println(PTId+" -> "+pt.type.accept(this)+";");
        return PTId;
    }

    @Override
    public String visitArrayType(ArrayType at) {
        String ATId = "Node"+NodeCnt++;
        writer.println(ATId+" [label=\"ArrayType("+at.size+")\"];");
        writer.println(ATId+" -> "+at.type.accept(this)+";");
        return ATId;
    }

    @Override
    public String visitIntLiteral(IntLiteral il) {
        writer.println("Node"+NodeCnt+" [label=\"IntLiteral("+il.number+")\"];");
        return "Node"+NodeCnt++;
    }

    @Override
    public String visitStringLiteral(StrLiteral sl) {
        writer.println("Node"+NodeCnt+" [label=\"StringLiteral("+sl.string+")\"];");
        return "Node"+NodeCnt++;
    }

    @Override
    public String visitChrLiteral(ChrLiteral cl) {
        writer.println("Node"+NodeCnt+" [label=\"ChrLiteral("+cl.c+")\"];");
        return "Node"+NodeCnt++;
    }

    @Override
    public String visitFunCallExpr(FunCallExpr fce) {
        String FCId = "Node"+NodeCnt++;
        writer.println(FCId+" [label=\"FunCallExpr("+fce.name+")\"];");
        for (Expr e : fce.args) {
            writer.println(FCId+" -> "+e.accept(this)+";");
        }
        return FCId;
    }

    @Override
    public String visitBinOp(BinOp bo) {
        String BOId = "Node"+NodeCnt++;
        writer.println(BOId+" [label=\"BinOp\"];");
        writer.println(BOId+" -> "+bo.E1.accept(this)+";");
        writer.println(BOId+" -> "+bo.op.accept(this)+";");
        writer.println(BOId+" -> "+bo.E2.accept(this)+";");
        return BOId;
    }

    @Override
    public String visitOp(Op o) {
        writer.print("Node"+NodeCnt+" [label=\"");
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
        writer.println("\"];");
        return "Node"+NodeCnt++;
    }

    @Override
    public String visitArrayAccessExpr(ArrayAccessExpr aae) {
        String AAId = "Node"+NodeCnt++;
        writer.println(AAId+" [label=\"ArrayAccessExpr\"];");
        writer.println(AAId+" -> "+aae.exp.accept(this)+";");
        writer.println(AAId+" -> "+aae.index.accept(this)+";");
        return AAId;
    }

    @Override
    public String visitFieldAccessExpr(FieldAccessExpr fae) {
        String FAId = "Node"+NodeCnt++;
        writer.println(FAId+" [label=\"FieldAccessExpr\"];");
        writer.println(FAId+" -> "+fae.struct.accept(this)+";");
        writer.println("Node"+NodeCnt+" [label=\"field("+fae.field+")\"];");
        writer.println(FAId+" -> "+"Node"+NodeCnt+";");
        NodeCnt++;
        return FAId;
    }

    @Override
    public String visitValueAtExpr(ValueAtExpr vae) {
        String VAId = "Node"+NodeCnt++;
        writer.println(VAId+" [label=\"ValueAtExpr\"];");
        writer.println(VAId+" -> "+vae.exp.accept(this)+";");
        return VAId;
    }

    @Override
    public String visitSizeOfExpr(SizeOfExpr soe) {
        String SOId = "Node"+NodeCnt++;
        writer.println(SOId+" [label=\"SizeOfExpr\"];");
        writer.println(SOId+" -> "+soe.type.accept(this)+";");
        return SOId;
    }

    @Override
    public String visitTypecastExpr(TypecastExpr te) {
        String TCId = "Node"+NodeCnt++;
        writer.println(TCId+" [label=\"TypecastExpr\"];");
        writer.println(TCId+" -> "+te.type.accept(this)+";");
        writer.println(TCId+" -> "+te.exp.accept(this)+";");
        return TCId;
    }

    @Override
    public String visitExprStmt(ExprStmt es) {
        String EId = "Node"+NodeCnt++;
        writer.println(EId+" [label=\"ExprStmt\"];");
        writer.println(EId+" -> "+es.exp.accept(this)+";");
        return EId;
    }

    @Override
    public String visitWhile(While w) {
        String WId = "Node"+NodeCnt++;
        writer.println(WId+" [label=\"While\"];");
        writer.println(WId+" -> "+w.cond.accept(this)+";");
        writer.println(WId+" -> "+w.loop.accept(this)+";");
        return WId;
    }

    @Override
    public String visitIf(If i) {
        String IId = "Node"+NodeCnt++;
        writer.println(IId+" [label=\"If\"];");
        writer.println(IId+" -> "+i.cond.accept(this)+";");
        writer.println(IId+" -> "+i.st1.accept(this)+";");
        if (i.st2 != null) {
            writer.println(IId+" -> "+i.st2.accept(this)+";");
        }
        return IId;
    }

    @Override
    public String visitAssign(Assign a) {
        String AId = "Node"+NodeCnt++;
        writer.println(AId+" [label=\"Assign\"];");
        writer.println(AId+" -> "+a.e1.accept(this)+";");
        writer.println(AId+" -> "+a.e2.accept(this)+";");
        return AId;
    }

    @Override
    public String visitReturn(Return r) {
        String RId = "Node"+NodeCnt++;
        writer.println(RId+" [label=\"Return\"];");
        if (r.exp != null)
            writer.println(RId+" -> "+r.exp.accept(this)+";");
        return RId;
    }
}
