package Triangle.CodeGenerator;

import Triangle.AbstractSyntaxTrees.*;
import Triangle.AbstractSyntaxTrees.Visitor;
import Triangle.ErrorReporter;
import java.util.HashMap;
import java.util.Map;

public final class LLVMGenerator implements Visitor {
    private StringBuilder code;
    private Map<String, String> locals;
    private int tempCount;
    private int labelCount = 0; //Para generar nombre unigos en las etiquetas
    
    // Genera un registro tipo %tmpX
    private String newTemp() {
        return "%tmp" + (tempCount++);
    }
    // Overload de newTemp para poder ponerle un nombre cualquiera al registro
    //Esto hace mas facil el debug pq genera .ll mas legible
    private String newTemp(String name) {
        return "%" + name + (tempCount++);
    }
    
    //Genera una nueva label con un nombre unico
    //Toma name como parametro para darle un nombre significatico a la etiqueta
    //name solo tiene fines de debug, y leer el codigo llvm, no tiene nada funcioonal
    private String newLabel(String name) {
        return name + (labelCount++);
    }
    //Overload de newLabel si no importa el nombre de la etiqueta
    private String newLabel(){
        return "label" + (labelCount++);
    }
    

    private final ErrorReporter reporter;

    public LLVMGenerator(ErrorReporter reporter) {
        this.reporter = reporter;
        this.code = new StringBuilder();
        locals = new HashMap<>();
        tempCount = 0;
    }
    public String generate(Program ast) {
        code.append("; Código LLVM generado por Triangle\n\n");
        code.append("define i32 @main() {\n");
        code.append("entry:\n");

        ast.visit(this, null);

        code.append("  ret i32 0\n");
        code.append("}\n");

        return code.toString();
    }

    @Override
    public Object visitProgram(Program ast, Object arg) {
        ast.C.visit(this, arg);
        return null;
    }
    
    //////////////////////// COMMANDS

    @Override
    public Object visitLetCommand(LetCommand ast, Object arg) {
        ast.D.visit(this, arg);
        ast.C.visit(this, arg);
        return null;
    }
    @Override
    public Object visitVarDeclaration(VarDeclaration ast, Object arg) {
        String varName = ast.I.spelling;
        String regName = "%" + varName;
        locals.put(varName, regName);
        code.append("  " + regName + " = alloca i32, align 4\n");
        return null;
    }

  @Override
    public Object visitAssignCommand(AssignCommand ast, Object arg) {
        String varName = ((SimpleVname) ast.V).I.spelling;
        String ptr = locals.get(varName); // %n
        String value = (String) ast.E.visit(this, arg);

        code.append("  store i32 " + value + ", ptr " + ptr + ", align 4\n");
        return null;
    }

    @Override
    public Object visitCallCommand(CallCommand ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    @Override
    public Object visitEmptyCommand(EmptyCommand ast, Object o) {
        return null;
    }

    @Override
    public Object visitIfCommand(IfCommand ast, Object o) {
        // Generamos etiquetas unicas para mostrar el else, el then y el fin dew un if
        code.append("; Comienzo de IF_COMMAND \n");
        String thenLabel = newLabel("then");
        String elseLabel = newLabel("else");
        String endIfLabel = newLabel("end_if");
        
        // Efvaluamos la condicion, asumimos que i32 0 es false y que otro i32 no 0 es true
        String condValue = (String) ast.E.visit(this, o);
        
        //Convertimos i32 a i1 para el jump
        String condBool = newTemp("cond");
        code.append("  " + condBool + " = icmp ne i32 " + condValue + ", 0\n");
        
        //Jump basado en condBool
        code.append("  br i1 " + condBool + ", label %" + thenLabel + ", label %" + elseLabel + "\n");
        
        //THEN
        code.append(thenLabel + ":\n");
        ast.C1.visit(this, o);
        //salto a fin
        code.append("  br label %" + endIfLabel + "\n");
        
        //BLoque else
        code.append(elseLabel + ":\n");
        ast.C2.visit(this, o);
        //salto a fin
        code.append("  br label %" + endIfLabel + "\n");
        
        // Etiqueta de fin
        code.append(endIfLabel + ":\n");
        
        code.append("; Fin de IF_COMMAND \n");
        
        return null;
        
    }

    @Override
    public Object visitSequentialCommand(SequentialCommand ast, Object o) {
        ast.C1.visit(this, o);
        ast.C2.visit(this, o);
        return null;
    }

    @Override
    public Object visitWhileCommand(WhileCommand ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    ////////////////////////////// EXPRESION
    
    @Override
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression ast, Object o) {
        String leftReg = (String) ast.E1.visit(this, o);
        String rightReg = (String) ast.E2.visit(this, o);

        String tmpReg = newTemp();
        code.append("  " + tmpReg + " = add i32 " + leftReg + ", " + rightReg + "\n");
        return tmpReg;
    }

    @Override
    public Object visitCallExpression(CallExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitCharacterExpression(CharacterExpression ast, Object o) {
        char ch = ast.CL.spelling.charAt(1); //Obtiene el valor delcentro del char como 'A'
        int  ascii = (int) ch;
        return String.valueOf(ascii);
    }

    @Override
    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return "0";
    }

    @Override
    public Object visitIfExpression(IfExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        return ast.IL.spelling;
    }

    @Override
    public Object visitLetExpression(LetExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitRecordExpression(RecordExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitVnameExpression(VnameExpression ast, Object o) {
        return ast.V.visit(this, o);
    }

    //////////////////////////////////// DECLARATION
    
    @Override
    public Object visitBinaryOperatorDeclaration(BinaryOperatorDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitConstDeclaration(ConstDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitFuncDeclaration(FuncDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitProcDeclaration(ProcDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSequentialDeclaration(SequentialDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitTypeDeclaration(TypeDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitUnaryOperatorDeclaration(UnaryOperatorDeclaration ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    ////////////////////////////////// AGGREGATE
    
    @Override
    public Object visitMultipleArrayAggregate(MultipleArrayAggregate ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitMultipleRecordAggregate(MultipleRecordAggregate ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSingleRecordAggregate(SingleRecordAggregate ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    ///////////////////////////////// FP - FORMAL PARAMETER

    @Override
    public Object visitConstFormalParameter(ConstFormalParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitFuncFormalParameter(FuncFormalParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitProcFormalParameter(ProcFormalParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitVarFormalParameter(VarFormalParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    ///////////////////////////////////// FPS - FORMAL PARAMETER SEQUENCE
    
    @Override
    public Object visitEmptyFormalParameterSequence(EmptyFormalParameterSequence ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitMultipleFormalParameterSequence(MultipleFormalParameterSequence ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSingleFormalParameterSequence(SingleFormalParameterSequence ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    ///////////////////////////////////// AP - ACTUAL PARAMETER

    @Override
    public Object visitConstActualParameter(ConstActualParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitFuncActualParameter(FuncActualParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitProcActualParameter(ProcActualParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitVarActualParameter(VarActualParameter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    //////////////////////////////////// APS - ACTUAL PARAMETER SEQUENCE
    
    @Override
    public Object visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSingleActualParameterSequence(SingleActualParameterSequence ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    ////////////////////////////////////// TYPE DENOTER

    @Override
    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitErrorTypeDenoter(ErrorTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSimpleTypeDenoter(SimpleTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitIntTypeDenoter(IntTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitRecordTypeDenoter(RecordTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitMultipleFieldTypeDenoter(MultipleFieldTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSingleFieldTypeDenoter(SingleFieldTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    /////////////////////////////////////////////// LITERAL
    
    
    @Override
    public Object visitCharacterLiteral(CharacterLiteral ast, Object o) {
        char ch = ast.spelling.charAt(1); // Obtiene el valor del centro de un carater, tipo 'A'; obtiene solo A
        int ascii = (int) ch;
        return String.valueOf(ascii); //Tira el valor:
    }

    @Override
    public Object visitIdentifier(Identifier ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
        return ast.spelling;
    }

    
    //////////////////////////// OPERATOR
    
    @Override
    public Object visitOperator(Operator ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    

    //////////////////////////////// VNAME

    @Override
    public Object visitDotVname(DotVname ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSimpleVname(SimpleVname ast, Object o) {
        String varName = ast.I.spelling;
        String ptr = locals.get(varName);
        String tmpReg = newTemp();
        code.append("  " + tmpReg + " = load i32, ptr " + ptr + ", align 4\n");
        return tmpReg;
    }

    @Override
    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}