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
                current.add(new offset(i.varName,0));
            } else {
                current.add(new offset(i.varName, current.getLast().pos + sizeLast));
            }
            sizeLast = findSize(i.type) + (4-findSize(i.type)%4)%4;
        }
        current.add(new offset(current.getLast().pos+sizeLast));
        return null;
    }

    int curr_add;
    @Override
    public Register visitBlock(Block b) {
        if (pass == 0) {
            for (Stmt i : b.stmtList) {
                i.accept(this);
            }
        } else if (pass == 1){
            curr_add=0;
            for (VarDecl i : b.varDeclList) {
                i.accept(this);
            }
            fSize.push(curr_add);
            writer.println("\tADDI $sp, $sp "+fSize.peek());
            for (Stmt i : b.stmtList) {
                i.accept(this);
            }
            //Move stack pointer back down
            writer.println("\tSUBI $sp, $sp "+fSize.pop());

//            writer.println("\tjr $ra");
        }
        return null;
    }

    @Override
    public Register visitFunDecl(FunDecl p) {
        if (pass == 0) {
            p.block.accept(this);
        } else if (pass == 1) { //need to figure out what to do with parameters
            int stack_size = 0;
            for (int i = p.params.size()-1; i > 0; i--) {
                if (i < 3) {
                    p.params.get(i).offset = -i - 2;
                } else {
                    p.params.get(i).offset = stack_size;
                }
                stack_size-=4;
            }
            writer.println(p.name+":");
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
                switch (findSize(v.vd.type)) {
                    case 1:
                        writer.print("\tLB ");
                        break;
                    case 4:
                        writer.print("\tLW ");
                        break;
                    default:
                        System.out.println("Some thing has gone wrong, Weird variable size");
                        break;
                }
                writer.print(out + ", ");
                if (v.vd.offset == -1) {
                    writer.println(v.name);
                } else if (v.vd.offset < -1) {
                    writer.println(v.vd.offset+1+"($fp)");
                } else {
                    writer.println(v.vd.offset + "($fp)");
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
            //get arguments
        	for (int i = 0; i < fce.args.size(); i++) {
        		Register aux = fce.args.get(i).accept(this);
                writer.println("\tSW "+aux+", 4($sp)");
                writer.println("\tADDI $sp, $sp 4");
                if (aux != Register.v0)
                    freeRegister(aux);
	        }
        	//move aruguments into input
            int stack_size = 0; //temporarily move all arguments onto stack
            //TODO need to figure out how to restore stack when done
            for (int i = fce.args.size()-1; i >= 0; i--) {
                if (i<3) {
                    Register aux = getRegister();
                    writer.println("\tLW "+aux+", "+stack_size+"($sp)");
                    writer.println("\tMOVE " + Register.paramRegs[i] + ", " + aux);
                    freeRegister(aux);
                } else {
//                    fce.fd.params.get(i).offset = stack_size+1;
                    //TODO handling of other arguments, already on stack
                }
                stack_size-=4;
            }
            //run function
            Register out;
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
                    break;
                case "read_c":
                    break;
                case "mcmalloc":
                    break;
                default:
                    //save old frame
                    writer.println("\tSW $fp, 4($sp)");
                    writer.println("\tSW $sp, 8($sp)");
                    writer.println("\tSW $ra, 12($sp)");
                    writer.println("\tMOVE $fp, $sp");
                    writer.println("\tADDI $sp, $sp 12");
                    //jump to function
                	writer.println("\tjal "+fce.name);
                	//restore old frame
                	writer.println("\tLW $ra, 12($fp)");
                	writer.println("\tLW $sp, 8($fp)");
                	writer.println("\tLW $fp, 4($fp)");
                	return Register.v0;
            }
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

    int addr;
    @Override
    public Register visitArrayAccessExpr(ArrayAccessExpr aae) {
        if (pass == 0) {

        } else if (pass == 1) {
        	Register out;
        	if (aae.exp instanceof ValueAtExpr) {
				addr = -1;
				aae.exp.accept(this);
	        } else if (aae.exp instanceof ArrayAccessExpr) {
        		Register index = ((ArrayAccessExpr) aae.exp).index.accept(this);

	        } else if (aae.exp instanceof VarExpr) {
        		if (addr == -1) {
			        out = getRegister();
			        switch (findSize(aae.type)) {
				        case 4:
					        writer.print("\tLW ");
					        break;
				        case 1:
					        writer.print("\tLB");
			        }
			        writer.println("");
		        } else {

		        }
	        } else if (aae.exp instanceof FunCallExpr) {

	        } else {
        		System.out.println("Problem in array access");
	        }
        }
        return null;
    }

    @Override
    public Register visitFieldAccessExpr(FieldAccessExpr fae) {
        if (pass == 0) {

        } else if (pass == 1) {
        	if (fae.struct.type instanceof StructType) {

	        } else if (fae.struct instanceof FunCallExpr) {

	        } else if (fae.struct instanceof FieldAccessExpr) {

	        }
        }
        return null;
    }

    @Override
    public Register visitValueAtExpr(ValueAtExpr vae) {
        if (pass == 0) {

        } else if (pass == 1) {
        	if (addr == -1) {

	        } else {
		        vae.exp.accept(this);
	        }
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
        es.exp.accept(this);
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
            String Case2 = "Else_"+ID++;
            Register res = i.cond.accept(this);
            writer.println("\tBEQZ "+res+", "+Case2);
            i.st1.accept(this);
            writer.println(Case2+":");
            if (i.st2 != null)
                i.st2.accept(this);
        }
        return null;
    }

    @Override
    public Register visitAssign(Assign a) {
        if (pass == 0) {
            a.e1.accept(this);
            a.e2.accept(this);
        } else if (pass == 1) {
            Register out = a.e2.accept(this);
            if (a.e1 instanceof ValueAtExpr ) {

            } else if (a.e1 instanceof ArrayAccessExpr) {
//                switch ()
            } else if (a.e1 instanceof VarExpr) {
                switch (findSize(((VarExpr) a.e1).vd.type)) {
                    case 1:
                        writer.print("\tSB ");
                        break;
                    case 4:
                        writer.print("\tSW ");
                        break;
                    default:
                        System.out.println("Some thing has gone wrong, Weird variable size");
                        break;
                }
                writer.print(out+", ");
                int off = ((VarExpr) a.e1).vd.offset;
                if (off == -1) {
                    writer.println(((VarExpr) a.e1).name);
                } else {
                    writer.println(off+"($fp)");
                }
                //((VarExpr) a.e1).vd.offset+"($fp)");
            } else if (a.e1 instanceof FieldAccessExpr) {

            } else {
                System.out.println("Something Wrong, unknown LHS of assignemnt");
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
		    writer.println("\tjr $ra");
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
            int i = ((ArrayType) t).size * findSize(((ArrayType) t).type);
            return i + (4-i%4)%4;
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