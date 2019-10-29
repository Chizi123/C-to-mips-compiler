package gen;

import ast.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

public class CodeGenerator implements ASTVisitor<Register> {

    /*
     * Simple register allocator.
     */

    // contains all the free temporary registers
    private Stack<Register> freeRegs = new Stack<Register>();

    public CodeGenerator() {
        freeRegs.addAll(Register.tmpRegs);
    }

    private class RegisterAllocationError extends Error {}

    private Register getRegister() {
        try {
            return freeRegs.pop();
        } catch (EmptyStackException ese) {
            throw new RegisterAllocationError(); // no more free registers, bad luck!
        }
    }

    private void freeRegister(Register reg) {
        freeRegs.push(reg);
    }

    private PrintWriter writer; // use this writer to output the assembly instructions

    public void emitProgram(Program program, File outputFile) throws FileNotFoundException {
        writer = new PrintWriter(outputFile);

        visitProgram(program);
        writer.close();
    }

    private int pass;
    private int ID;
    private int fSize;
    private HashMap<String, LinkedList<offset>> structs;

    @Override
    public Register visitBaseType(BaseType bt) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitStructTypeDecl(StructTypeDecl st) {
        structs.put(st.st.name,new LinkedList<>());
        LinkedList<offset> current = structs.get(st.st.name);
        int sizeLast = 0;
        boolean first = true;
        for (VarDecl i : st.varDeclList) {
            if (first) {
                current.add(new offset(i.varName,0));
            } else {
                current.add(new offset(i.varName, current.getLast().pos + sizeLast));
            }
            sizeLast = findSize(i.type);
        }
        current.add(new offset(current.getLast().pos+sizeLast));
        return null;
    }

    @Override
    public Register visitBlock(Block b) {
        if (pass == 0) {
            for (Stmt i : b.stmtList) {
                i.accept(this);
            }
        } else if (pass == 1){
            //save old frame pointer and stack pointer
            writer.println("\tSW $fp, 4($sp)");
            writer.println("\tSW $sp, 8($sp)");
            writer.println("\tMOVE $fp, $sp");
            fSize=8;
            for (VarDecl i : b.varDeclList) {
                i.accept(this);
            }
            for (Stmt i : b.stmtList) {
                i.accept(this);
            }
        }
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        if (pass == 0) {
            p.block.accept(this);
        } else if (pass == 1) { //need to figure out what to do with parameters
            writer.println(p.name+":");
//            if()
            p.block.accept(this);
        }
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitProgram(Program p) {
        //get struct sizes and offsets
        structs = new HashMap<>();
        for (StructTypeDecl i : p.structTypeDecls) {
            i.accept(this);
        }
        //look through for global variables and strings
        pass = 0; ID = 0;
        writer.println(".data");
        writer.println("\tString"+(ID++)+": .asciiz \"\\n\"");
        for (VarDecl i : p.varDecls) {
            i.accept(this);
        }
        for (FunDecl i : p.funDecls) {
            i.accept(this);
        }

        //write text segment
        pass=1;
        writer.println(".text");
        for (FunDecl i : p.funDecls) {
            i.accept(this);
        }
        // TODO: to complete
        //default exit
        writer.println("\tLI $v0, 10\n\tSYSCALL");
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if (pass == 0) {
            writer.println("\t"+vd.varName+": .space "+findSize(vd.type));
        } else if (pass == 1) {
            vd.offset=fSize;
            fSize+=findSize(vd.type);
        }
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        if (pass == 0) {

        }
        // TODO: to complete
        return null;
    }

    @Override
    public Register visitPointerType(PointerType pt) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitStructType(StructType st) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitArrayType(ArrayType at) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitIntLiteral(IntLiteral il) {
        if (pass == 0) {

        } else if (pass == 1) {
            Register reg = getRegister();
            writer.println("\tli "+reg+", "+il.number);
            return reg;
        }
        return null;
    }

    @Override
    public Register visitStringLiteral(StrLiteral sl) {
        if (pass == 0) {
            sl.id = ID++;
            writer.println("\tString"+sl.id+": .asciiz \""+sl.string+"\"");
        }
        return null;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitFunCallExpr(FunCallExpr fce) {
        if (pass == 0) {
            for (Expr i : fce.args) {
                i.accept(this);
            }
        } else if (pass == 1) {
            switch (fce.name) {
                case "print_s": //to make generic
                    writer.println("\tLI $v0, 4");
                    writer.println("\tLA $a0, String"+((StrLiteral) fce.args.get(0)).id);
                    writer.println("\tsyscall");
                    break;
                case "print_i":
                    Register reg = fce.args.get(0).accept(this);
                    writer.println("\tMOVE $a0, "+reg);
                    freeRegister(reg);
                    writer.println("\tLI $v0, 1");
                    writer.println("\tSYSCALL");
                    //newline
                    writer.println("\tLA $a0, String0");
                    writer.println("\tLI $v0, 4");
                    writer.println("\tSYSCALL");
                    break;
                case "print_c":
                    break;
                case "read_i":
                    break;
                case "read_c":
                    break;
                case "mcmalloc":
                    break;
                default:
                    break;
            }
        }
        return null;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        if (pass == 0) {

        } else if (pass == 1) {
            Register e1 = bo.E1.accept(this); //parser fills left tree
            Register e2 = bo.E2.accept(this);
            if (bo.op == Op.DIV || bo.op == Op.MUL) {
                if (bo.op == Op.DIV) {
                    writer.println("\tDIV "+e1+", "+e1+" "+e2);
                } else {
                    writer.println("\tMUL "+e1+", "+e1+" "+e2);
                }
                writer.println("\tMFLO "+e1);
            } else if (bo.op == Op.MOD) {
                writer.println("\tDIV "+e1+", "+e1+" "+e2);
                writer.println("\tMFHI "+e1);
            } else if (bo.op == Op.ADD) {
                writer.println("\tADD "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.SUB) {
                writer.println("\tSUB "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.GT) {
                writer.println("\tSGE "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.LT) {
                writer.println("\tSLT "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.GE) {
                writer.println("\tSGE "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.LE) {
                writer.println("\tSLE "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.NE) {
                writer.println("\tSEQ "+e1+", "+e1+" "+e2);
                Register temp = getRegister();
                writer.println("\tLI "+temp+", 1");
                writer.println("\tSUB "+e1+", "+temp+" "+e1);
                freeRegister(temp);
            } else if (bo.op == Op.EQ) {
                writer.println("\tSEQ "+e1+", "+e1+" "+e2);
            } else if (bo.op == Op.OR) { //sequence wise or, not bitwise
                writer.println("\tSEQ "+e1+", $zero "+e1);
                writer.println("\tSEQ "+e2+", $zero "+e2);
                Register temp2 = getRegister();
                writer.println("\tLI "+temp2+", 1");
                writer.println("\tSUB "+e2+", "+temp2+" "+e2);
                writer.println("\tSUB "+e1+", "+temp2+" "+e1);
                writer.println("\tAND "+e1+", "+e2+" "+e1);
                writer.println("\tSUB "+e1+", "+temp2+" "+e1);
                freeRegister(temp2);
            } else if (bo.op == Op.AND) { //sequence wise or, not bitwise
                writer.println("\tSEQ "+e1+", $zero "+e1);
                writer.println("\tSEQ "+e2+", $zero "+e2);
                Register temp2 = getRegister();
                writer.println("\tLI "+temp2+", 1");
                writer.println("\tSUB "+e1+", "+temp2+" "+e1);
                writer.println("\tSUB "+e2+", "+temp2+" "+e2);
                writer.println("\tOR "+e1+", "+e1+" "+e2);
                writer.println("\tSUB "+e1+", "+temp2+" "+e1);
                freeRegister(temp2);
            }
            freeRegister(e2);
            return e1;
        }
        return null;
    }

    @Override
    public Register visitOp(Op o) {
        if (pass == 0) {
        } else if (pass == 1) {
        }
        return null;
    }

    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr soe) {
        if (pass == 0) {

        }
        return null;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr te) {
        if (pass == 0) {
            te.exp.accept(this);
        }
        return null;
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        es.exp.accept(this);
        return null;
    }

    @Override
    public Register visitWhile(While w) {
        w.cond.accept(this);
        w.loop.accept(this);
        return null;
    }

    @Override
    public Register visitIf(If i) {
        i.cond.accept(this);
        i.st1.accept(this);
        if (i.st2 != null) {
            i.st2.accept(this);
        }
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        if (pass == 0) {
            a.e1.accept(this);
            a.e2.accept(this);
        }
        return null;
    }

    @Override
    public Register visitReturn(Return r) {
        if (r.exp != null) {
            r.exp.accept(this);
        }
        return null;
    }

    private int findSize(Type t) {
        if (t instanceof PointerType) {
            return 4;
        } else if (t instanceof BaseType) {
            if (((BaseType) t).type == BaseType.INT.type) {
                return 4;
            } else if (((BaseType) t).type == BaseType.CHAR.type) {
                return 1;
            }
        } else if (t instanceof ArrayType) {
            return ((ArrayType) t).size * findSize(((ArrayType) t).type);
        } else if (t instanceof StructType) {
            return structs.get(((StructType) t).name).getLast().pos;
        }
        return 4;
    }
}

class offset {
    public String field;
    public Integer pos;

    public offset(String field, Integer pos) {
        this.field = field;
        this.pos = pos;
    }

    public offset(Integer pos) {
        this.pos = pos;
    }
}