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
        if (Register.tmpRegs.contains(reg))
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
    private Stack<Integer> fSize;
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
                current.add(new offset(i.varName,sizeLast));
                first=false;
            } else {
                current.add(new offset(i.varName, current.getLast().pos + sizeLast));
            }
            sizeLast = findSize(i.type) + (4-findSize(i.type)%4)%4;
        }
        current.add(new offset(current.getLast().pos+sizeLast));
        return null;
    }

    int curr_add = 0;
    @Override
    public Register visitBlock(Block b) {
        if (pass == 0) {
            for (Stmt i : b.stmtList) {
                i.accept(this);
            }
        } else if (pass == 1){
            for (VarDecl i : b.varDeclList) {
                i.accept(this);
            }
            int a;
            int c;
            try {
                c=fSize.peek();
            } catch (EmptyStackException e) {
                c=0;
            }
            fSize.push(curr_add);
            a = fSize.peek();
            writer.println("\tADDI $sp, $sp "+(a-c));
            for (Stmt i : b.stmtList) {
                i.accept(this);
            }
            //Move stack pointer back down
            a = fSize.pop();
            try {
                c=fSize.peek();
            } catch (EmptyStackException e) {
                c=0;
            }
            writer.println("\tSUBI $sp, $sp "+(a-c));
            curr_add=0;
        }
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        if (pass == 0) {
            p.block.accept(this);
        } else if (pass == 1) { //need to figure out what to do with parameters
            int stack_size = 16;
            for (int i = 0; i < p.params.size(); i++) {
                p.params.get(i).offset = stack_size;
                stack_size+=4;
            }
            writer.println(p.name+":");
            curr_add = stack_size;
            p.block.accept(this);
            writer.println("\tjr $ra");
        }
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
        for (VarDecl i : p.varDecls) {
            i.accept(this);
        }
        for (FunDecl i : p.funDecls) {
            i.accept(this);
        }

        //write text segment
        pass=1;
        writer.println(".text");
        writer.println("\tMOVE $fp, $sp");
        fSize=new Stack<>();
        writer.println("\tJAL main");
        writer.println("\tLI $v0, 10\n\tSYSCALL");
	    for (FunDecl i : p.funDecls) {
            i.accept(this);
        }
        return null;
    }

    @Override
    public Register visitVarDecl(VarDecl vd) {
        if (pass == 0) {
            writer.println("\t"+vd.varName+": .space "+findSize(vd.type));
            vd.offset=-1;
        } else if (pass == 1) {
            curr_add+=findSize(vd.type);
            vd.offset=curr_add;
        }
        return null;
    }

    @Override
    public Register visitVarExpr(VarExpr v) {
        if (pass == 0) {

        } else if (pass == 1) {
            Register out = getRegister();
            if (v.vd.offset < -1 && v.vd.offset >= -4) {
                out = Register.paramRegs[-1*v.vd.offset-2];
            } else {
                if (init != -1) {
                    writer.print("\tLW " + out + ", ");
                    if (v.vd.offset == -1) {
                        writer.println(v.name);
                    } else if (v.vd.offset < -1) {
                        writer.println(v.vd.offset + 1 + "($fp)");
                    } else {
                        writer.println(v.vd.offset + "($fp)");
                    }
                } else {
                    if (v.vd.offset == -1)
                        writer.println("\tLA "+out+", "+v.name);
                    else {
                        writer.println("\tLA " + out + ", ($fp)");
                        if (v.vd.offset < -1)
                            writer.println("\tADDI " + out + ", " + out + " " + (v.vd.offset+1));
                        else
                            writer.println("\tADDI " + out + ", " + out + " " + v.vd.offset);
                    }
                }
            }
            return out;
        }
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
            writer.println("\tLI "+reg+", "+il.number);
            return reg;
        }
        return null;
    }

    @Override
    public Register visitStringLiteral(StrLiteral sl) {
        if (pass == 0) {
            sl.id = ID++;
            writer.println("\tString"+sl.id+": .asciiz \""+sl.string+"\"");
        } else if (pass == 1) {
        	Register out = getRegister();
        	writer.println("\tLA "+out+", String"+sl.id);
        	return out;
        }
        return null;
    }

    @Override
    public Register visitChrLiteral(ChrLiteral cl) {
        if (pass == 0) {

        } else if (pass == 1) {
            Register out = getRegister();
            writer.print("\tLI " + out + ", '");
            if (cl.c == '\n') {
                writer.print("\\n");
            } else if (cl.c == '\"') {
                writer.print("\"");
            } else if (cl.c == '\\') {
                writer.print("\\\\");
            } else if (cl.c == '\t') {
                writer.print("\\t");
            } else if (cl.c == '\b') {
                writer.print("\\b");
            } else if (cl.c == '\r') {
                writer.print("\\r");
            } else if (cl.c == '\f') {
                writer.print("\\f");
            } else if (cl.c == '\0') {
                writer.print("\\0");
            } else {
                writer.print(cl.c);
            }
            writer.println("'");
            return out;
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
            try {
                if (fce.args.get(0).type instanceof ArrayType)
                    init = -1;
                Register a1 = fce.args.get(0).accept(this);
                if (fce.args.get(0).type instanceof ArrayType)
                    init = 0;
                writer.println("\tMOVE $a0, "+a1);
                freeRegister(a1);
            } catch (Exception e) {
                // do nothing if there's no arguments to the function
            }
            //run function
            switch (fce.name) {
                case "print_s": //to make generic
                    writer.println("\tLI $v0, 4");
                    writer.println("\tsyscall");
                    break;
                case "print_i":
                    writer.println("\tLI $v0, 1");
                    writer.println("\tSYSCALL");
                    break;
                case "print_c":
                    writer.println("\tLI $v0, 11");
                    writer.println("\tSYSCALL");
                    break;
                case "read_i":
                    writer.println("\tLI $v0, 5");
                    writer.println("\tSYSCALL");
                    break;
                case "read_c":
                    writer.println("\tLI $v0, 12");
                    writer.println("\tSYSCALL");
                    break;
                case "mcmalloc":
                    writer.println("\tLI $v0, 9");
                    writer.println("\tSYSCALL");
                    break;
                default:
                    //store arguments onto stack as regular variables
                    //inefficient but works
                    int stack=16;
                    writer.println("\tSW $a0, 16($sp)"); //save previous evaluation
                    writer.println("\tADDI $sp, $sp, "+(stack));
                    List<Expr> rest;
                    try {
                        rest = fce.args.subList(1, fce.args.size());
                    } catch (IllegalArgumentException e) {
                        rest = new LinkedList();
                    }
                    for (Expr i : rest) {
                        if (i.type instanceof ArrayType)
                            init = -1;
                        Register temp = i.accept(this);
                        if (i.type instanceof ArrayType)
                            init = 0;
                        writer.println("\tSW "+temp+", 4($sp)");
                        writer.println("\tADDI $sp, $sp 4");
                        stack+=4;
                        freeRegister(temp);
                    }
                    writer.println("\tSW $fp, "+(-1*(stack)+4)+"($sp)");
                    writer.println("\tSW $sp, "+(-1*(stack)+8)+"($sp)");
                    writer.println("\tSW $ra, "+(-1*(stack)+12)+"($sp)");
                    writer.println("\tMOVE $fp, $sp");
                    writer.println("\tSUBI $fp, $fp "+(stack));
                    //jump to function
                	writer.println("\tjal "+fce.name);
                	//restore old frame
                	writer.println("\tLW $ra, 12($fp)");
                	writer.println("\tLW $sp, 8($fp)");
                	writer.println("\tLW $fp, 4($fp)");
                    // restore stack to before function call
                	writer.println("\tSUBI $sp, $sp "+stack);
            }
            return Register.v0;
        }
        return null;
    }

    @Override
    public Register visitBinOp(BinOp bo) {
        if (pass == 0) {
			bo.E1.accept(this);
			bo.E2.accept(this);
        } else if (pass == 1) {
            if (bo.op == Op.OR || bo.op == Op.AND) { //short circuit operation
                Register e1 = bo.E1.accept(this);
                String end = "end"+ID;
                String pass = "pass"+ID;
                String fail = "fail"+ID++;
                if (bo.op == Op.OR) {
                    writer.println("\tBNEZ "+e1+", "+pass);
                    freeRegister(e1);
                    e1 = bo.E2.accept(this);
                    writer.println("\tBNEZ "+e1+", "+pass);
                    writer.println("\tJ "+fail);
                } else {
                    writer.println("\tBEQZ "+e1+", "+fail);
                    freeRegister(e1);
                    e1 = bo.E2.accept(this);
                    writer.println("\tBEQZ "+e1+", "+fail);
                    writer.println("\tJ "+pass);
                }
                writer.println(pass+":");
                writer.println("\tLI "+e1+", 1");
                writer.println("\tJ "+end);
                writer.println(fail+":");
                writer.println("\tLI "+e1+", 0");
                writer.println(end+":");
                return e1;
            } else { //other operations
                Register e1 = bo.E1.accept(this); //parser fills left tree
                //store intermediate results onto the stack to save registers
                writer.println("\tSW "+e1+", 4($sp)");
                writer.println("\tADDI $sp, $sp 4");
                freeRegister(e1);
                Register e2 = bo.E2.accept(this);
                //get intermediate results off stack
                e1=getRegister();
                writer.println("\tLW "+e1+", ($sp)");
                writer.println("\tSUBI $sp, $sp 4");
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
                    writer.println("\tSGT "+e1+", "+e1+" "+e2);
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
                }
                freeRegister(e2);
                return e1;
            }
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

    int nest = 0;
    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        if (pass == 0) {

        } else if (pass == 1) {
            boolean nested = init == -1;
            if (!nested) {
                init = -1;
            }
            nest += 1;
            Register addr;
            //hack first check but for some reason assigning type to aae.exp.type isnt working
            if (aae.exp.type instanceof PointerType || (aae.exp instanceof VarExpr && ((VarExpr) aae.exp).vd.type instanceof PointerType)) {
                int tinit = init;
                init = 0;
                addr = aae.exp.accept(this);
                init = tinit;
            } else {
                addr = aae.exp.accept(this);
            }
            int tinit = init;
            init = 0;
            Register off = aae.index.accept(this);
            init = tinit;
            Register temp = getRegister();
            if (aae.exp instanceof VarExpr && aae.exp.type == null) {
                int i = findSize(((VarExpr) aae.exp).vd.type);
                writer.println("\tLI " + temp + ", " + i);
            } else {
                int i = findSize(aae.exp.type);
                writer.println("\tLI " + temp + ", " + i);
            }
            nest -= 1;
            writer.println("\tMUL " + off + ", " + off + " " + temp);
            freeRegister(temp);
            writer.println("\tMFLO " + off);
            writer.println("\tSUB " + addr + ", " + addr + " " + off);
            freeRegister(off);
            if (!nested) {
                writer.println("\tLW " + addr + ", " + "(" + addr + ")");
                init = 0;
            }
            return addr;
        }
        return null;
    }

    int init, off;
    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        if (pass == 0) {

        } else if (pass == 1) {
        	if (fae.struct instanceof VarExpr) {
        	    int off=0;
                for (offset i : structs.get(((StructType) ((VarExpr) fae.struct).type).name)) {
                    if (i.field.equals(fae.field)) {
                        off = i.pos;
                        break;
                    }
                }
                Register out;
                if (init != -1) {
                    out = getRegister();
                    if (((VarExpr) fae.struct).vd.offset != -1) {
                        off = ((VarExpr) fae.struct).vd.offset - off;
                        writer.println("\tLW " + out + ", " + (off) + "($fp)");
                    } else {
                        writer.println("\tLA " + out + ", " + ((VarExpr) fae.struct).name);
                        writer.println("\tADDI "+out+", "+out+" "+findSize(fae.struct.type));
                        writer.println("\tSUBI " + out + ", " + out + " " + off);
                        writer.println("\tLW " + out + ", (" + (out) + ")");
                    }
                } else {
                    out = getRegister();
                    if (((VarExpr) fae.struct).vd.offset != -1) {
                        off = ((VarExpr) fae.struct).vd.offset - off;
                        writer.println("\tADDI " + out + ", $fp " + off);
                    } else {
                        writer.println("\tLA " + out + ", " + ((VarExpr) fae.struct).name);
                        writer.println("\tADDI "+out+", "+out+" "+findSize(fae.struct.type));
                        writer.println("\tSUBI " + out + ", " + out + " " + off);
                    }
                }
        	    return out;
	        } else if (fae.struct instanceof FunCallExpr) {
        	    Register out = fae.struct.accept(this);
        	    for (offset i : structs.get(((StructType) ((FunCallExpr) fae.struct).fd.type).name)) {
        	        if (i.field.equals(fae.field)) {
        	            off = i.pos;
        	            break;
                    }
        	        if (init != -1) {
                        writer.println("\tLW " + out + ", " + (-1 * off) + "(" + out + ")");
                    } else {
        	            writer.println("\tSUBI "+out+", "+out+" "+(off));
                    }
                    return out;
                }

	        } else {
        	    boolean nested;
                nested = init == -1;
        	    if (!nested) {
                    init = -1;
                }
                Register out = fae.struct.accept(this);
                for (offset i : structs.get(((StructType) fae.struct.type).name)) {
                    if (i.field.equals(fae.field)) {
                        off = i.pos;
                        break;
                    }
                }
                if (!nested) {
                    init = 0;
                }
                if (init != -1) {
                    writer.println("\tLW "+out+", "+(-1*off)+"("+out+")");
                } else {

                    writer.println("\tSUBI "+out+", "+out+" "+off);
                }
                return out;
	        }
        }
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        if (pass == 0) {

        } else if (pass == 1) {
            Register addr = vae.exp.accept(this);
            writer.println("\tLW "+addr+", ("+addr+")");
            return addr;
        }
        return null;
    }

    @Override
    public Register visitSizeOfExpr(SizeOfExpr soe) {
        if (pass == 0) {

        } else if (pass == 1) {
            Register out = getRegister();
            writer.println("\tLI "+out+", "+findSize(soe.type));
            return out;
        }
        return null;
    }

    @Override
    public Register visitTypecastExpr(TypecastExpr te) {
        if (pass == 0) {
            te.exp.accept(this);
        } else if (pass == 1) {
            return te.exp.accept(this);
        }
        return null;
    }

    @Override
    public Register visitExprStmt(ExprStmt es) {
        Register temp = es.exp.accept(this);
        freeRegister(temp);
        return null;
    }

    @Override
    public Register visitWhile(While w) {
        if (pass == 0) {
            w.cond.accept(this);
            w.loop.accept(this);
        } else if (pass == 1) {
            String WhileSID = "WhileS"+ID;
            String WhileEID = "WhileE"+ID++;
            writer.println(WhileSID+":");
            Register c = w.cond.accept(this);
            writer.println("\tBEQZ "+c+", "+WhileEID);
            freeRegister(c);
            w.loop.accept(this);
            writer.println("\tJ "+WhileSID);
            writer.println(WhileEID+":");
        }
        return null;
    }

    @Override
    public Register visitIf(If i) {
        if (pass == 0) {
            i.cond.accept(this);
            i.st1.accept(this);
            if (i.st2 != null) {
                i.st2.accept(this);
            }
        } else if (pass == 1) {
            String IfID = "If"+ID;
            String Case1 = "Then_"+ID;
            String Case2 = "Else_"+ID;
            String End = "End_"+ID++;
            Register res = i.cond.accept(this);
            writer.println("\tBEQZ "+res+", "+Case2);
            freeRegister(res);
            i.st1.accept(this);
            writer.println("\tJ "+End);
            writer.println(Case2+":");
            if (i.st2 != null)
                i.st2.accept(this);
            writer.println(End+":");
        }
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        if (pass == 0) {
            a.e1.accept(this);
            a.e2.accept(this);
        } else if (pass == 1) {
            Register out;
            if (a.e2.type instanceof StructType && a.e2 instanceof VarExpr) {
                Register e1 = getRegister();
                Register e2 = getRegister();
                if (((VarExpr) a.e1).vd.offset == -1) {
                    writer.println("\tLA " + e1 + ", " + ((VarExpr) a.e1).name);
                } else {
                    writer.println("\tLI " + e1 + ", " + ((VarExpr) a.e1).vd.offset);
                    writer.println("\tADD " + e1 + ", " + e1 + " $fp");
                }
                if (((VarExpr) a.e2).vd.offset == -1) {
                    writer.println("\tLA " + e2 + ", " + ((VarExpr) a.e2).name);
                } else {
                    writer.println("\tLI " + e2 + ", " + ((VarExpr) a.e2).vd.offset);
                    writer.println("\tADD " + e2 + ", " + e2 + " $fp");
                }
                for (int i = structs.get(((StructType) a.e2.type).name).getLast().pos; i > 0; i -= 4) {
                    Register temp = getRegister();
                    writer.println("\tLW " + temp + ", " + i + "(" + e2 + ")");
                    writer.println("\tSW " + temp + ", " + i + "(" + e1 + ")");
                    freeRegister(temp);
                }
                freeRegister(e1);
                freeRegister(e2);
                return null;
            } else if (a.e1.type instanceof PointerType && a.e2.type instanceof PointerType) {
                writer.println("# pointer assignment");
                init = -1;
                Register e2 = a.e2.accept(this);
                Register e1 = a.e1.accept(this);
                init = 0;
                writer.println("\tSW "+e2+", ("+e1+")");
                freeRegister(e1);
                freeRegister(e2);
            } else {
                out = a.e2.accept(this);
                if (a.e1 instanceof ValueAtExpr) {
                	init = -1;
                	Register addr = ((ValueAtExpr) a.e1).exp.accept(this);
                	init = 0;
                	writer.println("\tLW "+addr+", ("+addr+")");
					writer.println("\tSW "+out+", ("+addr+")");
					freeRegister(addr);
                } else if (a.e1 instanceof ArrayAccessExpr) {
                    init = -1;
                    Register addr = a.e1.accept(this);
                    init = 0;
                    writer.println("\tSW " + out + ", (" + addr + ")");
                    freeRegister(addr);
                } else if (a.e1 instanceof VarExpr) {
                    writer.print("\tSW " + out + ", ");
                    int off = ((VarExpr) a.e1).vd.offset;
                    if (off == -1) {
                        writer.println(((VarExpr) a.e1).name);
                    } else {
                        writer.println(off + "($fp)");
                    }
                } else if (a.e1 instanceof FieldAccessExpr) {
                    if (((FieldAccessExpr) a.e1).struct instanceof VarExpr) {
                        Register addr = getRegister();
                        int off = 0;
                        for (offset i : structs.get(((StructType) ((VarExpr) ((FieldAccessExpr) a.e1).struct).type).name)) {
                            if (i.field.equals(((FieldAccessExpr) a.e1).field)) {
                                off = i.pos;
                                break;
                            }
                        }
                        if (((VarExpr) ((FieldAccessExpr) a.e1).struct).vd.offset == -1) {
                            writer.println("\tLA " + addr + ", " + ((VarExpr) ((FieldAccessExpr) a.e1).struct).name);
                            writer.println("\tADDI " + addr + ", " + addr + " " + findSize(a.e1.type));
                            writer.println("\tSUBI " + addr + ", " + addr + " " + off);
                            writer.println("\tSW " + out + ", (" + addr + ")");
                        } else {
                            writer.println("\tLA " + addr + ", " + ((VarExpr) ((FieldAccessExpr) a.e1).struct).vd.offset + "($fp)");
                            writer.println("\tSUBI " + addr + ", " + addr + " " + off);
                            writer.println("\tSW " + out + ", (" + addr + ")");
                        }
                        freeRegister(addr);
                    } else {
                        boolean nested;
                        nested = init == -1;
                        if (!nested)
                            init = -1;
                        Register addr = a.e1.accept(this);
                        if (!nested)
                            init = 0;
                        if (init == -1) {
                            return addr;
                        } else {
                            int off = 0;
                            for (offset i : structs.get(((StructType) ((FieldAccessExpr) a.e1).struct.type).name)) {
                                if (i.field.equals(((FieldAccessExpr) a.e1).field)) {
                                    off = i.pos;
                                    break;
                                }
                            }
                            writer.println("\tSUBI " + addr + ", " + addr + " " + off);
                            writer.println("\tSW " + out + ", (" + addr + ")");
                            freeRegister(addr);
                        }
                    }
                } else {
                    System.out.println("Something Wrong, unknown LHS of assignemnt");
                }
                freeRegister(out);
            }
        }
        return null;
    }

    @Override
    public Register visitReturn(Return r) {
    	if (pass == 0) {

	    } else if (pass == 1) {
		    Register out;
		    if (r.exp != null) {
			    out = r.exp.accept(this);
			    writer.println("\tMOVE $v0, "+out);
			    freeRegister(out);
		    }
		    writer.println("\tJR $ra");
	    }
        return null;
    }

    int lnest = 0;
    private int findSize(Type t) {
        if (t instanceof PointerType) {
            return 4;
        } else if (t instanceof BaseType) {
            if (((BaseType) t).type == BaseType.INT.type) {
                return 4;
            } else if (((BaseType) t).type == BaseType.CHAR.type) {
                return 4; //TODO Efficiency, need to come back and make code align to nearest byte
            }
        } else if (t instanceof ArrayType) {
            if (lnest == nest) {
                return ((ArrayType) t).size * findSize(((ArrayType) t).type);
            } else {
                lnest+=1;
                int i = findSize(((ArrayType) t).type);
                lnest-=1;
                return i;
            }
//            int i = ((ArrayType) t).size * findSize(((ArrayType) t).type);
//            return i + (4-i%4)%4;
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