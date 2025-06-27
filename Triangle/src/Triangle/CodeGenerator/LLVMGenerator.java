package Triangle.CodeGenerator;

import TAM.Machine;
import Triangle.AbstractSyntaxTrees.*;
import Triangle.AbstractSyntaxTrees.Visitor;
import static Triangle.CodeGenerator.Encoder.writeTableDetails;
import Triangle.CodeGenerator.PrimitiveRoutine;
import Triangle.ErrorReporter;
import Triangle.StdEnvironment;
import java.util.List;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public final class LLVMGenerator implements Visitor {
    private StringBuilder code;
    private Map<String, String> locals;
    private List<String> functionArgs;
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
        functionArgs = new ArrayList<>();
        tempCount = 0;
        // Carga las declaraciones y llamadas de las funciones estandar
        this.elaborateStdEnvironment();        
    }
    public String generate(Program ast) {
        code.append("\n; Código LLVM generado por Triangle\n\n");
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
    public Object visitAssignCommand(AssignCommand ast, Object arg) {
        SimpleVname varName;
        TypeDenoter type = ast.V.type;
        
        //En el caso de que sea un array // c[0] = 5
        if(ast.V instanceof SubscriptVname subscriptVname){
            varName = (SimpleVname) subscriptVname.V;
            String ptr = locals.get(varName.I.spelling); // %n
            String index = (String) subscriptVname.E.visit(this, arg);
            String arraySize = (String)(varName.type.visit(this, null));
            
            String tmpReg = newTemp("arrayValue");
            code.append(String.format("  %s = getelementptr inbounds [%s x i32], ptr %s, i64 0, i64 %s\n",tmpReg, arraySize, ptr, index));
            String value = (String) ast.E.visit(this, arg);
            code.append("  store i32 " + value + ", ptr " + tmpReg + ", align 4\n");
                
        } else{
            
            varName = (SimpleVname) ast.V;     
            String ptr = locals.get(varName.I.spelling); // %n
            //Si es un Array // c = [1,2,3,4,5]
            if(type instanceof ArrayTypeDenoter){

                List<String> arrayValues = (List<String>) ast.E.visit(this, null);

                for (int i = 0; i < arrayValues.size(); i++) {

                    String tmpReg = newTemp("index");
                    String value = (String) arrayValues.get(i);

                    code.append(String.format("  %s = getelementptr inbounds [%d x i32], ptr %s, i64 0, i64 %d\n",tmpReg, arrayValues.size(), ptr, i));
                    code.append(String.format("  store i32 %s, ptr %s, align 4\n",value, tmpReg));
                }
                //Dejaré esto por aquí por si al final el assignCommand no se necesite hacer así
                /*
                ArrayExpression array = (ArrayExpression) ast.E;
                int arrayIndex = 0;
                Object[] args = new Object[] { ptr, arrayIndex };
                array.visit(this, args);
                */

            } else{
                String value = (String) ast.E.visit(this, arg);
                code.append("  store i32 " + value + ", ptr " + ptr + ", align 4\n");
            }
        }
            
        return null;
    }

    @Override
    public Object visitCallCommand(CallCommand ast, Object o) {
        functionArgs.clear();
        Integer argsSize = (Integer) ast.APS.visit(this, o);        
        ast.I.visit(this, o);
        return null;
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
        
        // Efvaluamos la condicion, esto se toma del BinaryExpression
        String condBool = (String) ast.E.visit(this, o);
        
        
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
        //Generamos las etiquetas que necesitemos
        code.append("; Comienza WHILE_COMMAND \n");
        String startLabel = newLabel("while_start");
        String endLabel = newLabel("while_end");
        String bodyLabel = newLabel("while_body");
        
        code.append("  br label %" + startLabel + "\n");
        code.append(startLabel + ": \n");
        String condBool = (String) ast.E.visit(this, o); //Se trae el Binary expresison
        
        // branch para ver si la coindicion del while es true
        code.append("  br i1 " + condBool + ", label %" + bodyLabel + ", label %" + endLabel + "\n");
        
        // Si es true, el body del while y se salta otra vez a start
        code.append(bodyLabel + ": \n");
        ast.C.visit(this, o);
        
        code.append("  br label %" + startLabel + "\n");
        
        //Si es false se salta a fin
        code.append(endLabel + ": \n");
        
        code.append("; Termina WHILE_COMMAND \n");
        //No necesita retornar nada 
        return null;
        
    }

    ////////////////////////////// EXPRESION
    
    @Override
    public Object visitArrayExpression(ArrayExpression ast, Object o) {
        code.append("; Comienza ARRAY_EXPRESSION \n");
        ArrayList<Expression> arrayList = (ArrayList<Expression>) ast.AA.visit(this, o);
        return arrayList;
    }

    @Override
    public Object visitBinaryExpression(BinaryExpression ast, Object o) {
        code.append("; Comienza BINARY_EXPRESSION \n");
        String leftReg = (String) ast.E1.visit(this, o);
        String rightReg = (String) ast.E2.visit(this, o);
        String tmpReg = newTemp("binaryRes");
        String operation = ast.O.spelling;
        
        //No se si los tiene todos pero agregue todo lo que pude
        switch (operation){
            case "+":
                code.append("  " + tmpReg + " = add i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "-":
                code.append("  " + tmpReg + " = sub i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "*":
                code.append("  " + tmpReg + " = mul i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "/":
                code.append("  " + tmpReg + " = sdiv i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "<":
                code.append("  " + tmpReg + " = icmp slt i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "<=":
                code.append("  " + tmpReg + " = icmp sle i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case ">":
                code.append("  " + tmpReg + " = icmp sgt i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case ">=":
                code.append("  " + tmpReg + " = icmp sge i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "=":
                code.append("  " + tmpReg + " = icmp eq i32 " + leftReg + ", " + rightReg + "\n");
                break;
            case "!=":
                code.append("  " + tmpReg + " = icmp ne i32 " + leftReg + ", " + rightReg + "\n");
                break;
            default:
                System.out.println("AAAAAAAAAAA operador invalido no se como tirar un erro de triangle si es que tiene c lo menos");
                return "0";
        }
        code.append("; Termina BINARY_EXPRESSION \n");
        return tmpReg;
    }

    @Override
    public Object visitCallExpression(CallExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitCharacterExpression(CharacterExpression ast, Object o) {        
        char ch = ast.CL.spelling.charAt(1); //Obtiene el valor delcentro del char como 'A'        
        int ascii = (int) ch;
        return String.valueOf(ascii);
    }

    @Override
    public Object visitEmptyExpression(EmptyExpression ast, Object o) {
        return "0";
    }

    @Override
    public Object visitIfExpression(IfExpression ast, Object o) {
        // Generamos etiquetas unicas para mostrar el else, el then y el fin dew un if
        code.append("; Comienzo de IF_EXPRESSION \n");
        String thenLabel = newLabel("then");
        String elseLabel = newLabel("else");
        String endIfLabel = newLabel("end_if");
        
        //Reservamos espacio para el resultado del if
        String resultPtr = newTemp("if_result_ptr");
        code.append("  " + resultPtr + " = alloca i32, align 4\n");
        
        // Efvaluamos la condicion, esto se toma del BinaryExpression
        String condBool = (String) ast.E1.visit(this, o);
        
        //Jump basado en condBool
        code.append("  br i1 " + condBool + ", label %" + thenLabel + ", label %" + elseLabel + "\n");
        
        //THEN
        code.append(thenLabel + ":\n");
        String thenVal = (String) ast.E2.visit(this, o);
        code.append("  store i32 " + thenVal + ", ptr " + resultPtr + ", align 4\n");
        code.append("  br label %" + endIfLabel + "\n");
        
        //BLoque else
        code.append(elseLabel + ":\n");
        String elseVal = (String) ast.E3.visit(this, o);
        code.append("  store i32 " + elseVal + ", ptr " + resultPtr + ", align 4\n");
        code.append("  br label %" + endIfLabel + "\n");
        
        // Etiqueta de fin
        code.append(endIfLabel + ":\n");
        
        //Carga el valor del resultado
        String resultVal = newTemp("if_result");
        code.append("  " + resultVal + " = load i32, ptr " + resultPtr + ", align 4\n");
        
        code.append("; Fin de IF_EXPRESSION \n");
        
        return resultVal;
    }

    @Override
    public Object visitIntegerExpression(IntegerExpression ast, Object o) {
        return ast.IL.spelling;
    }

    @Override
    public Object visitLetExpression(LetExpression ast, Object o) {
       /*
            let 
                var x: Integer
            in
                X+2
        */
       code.append("; Comienza LET_EXPRESSION\n");
       ast.D.visit(this, o);
       String exprResult = (String) ast.E.visit(this, o);
       code.append("; Termina LET_EXPRESSION\n"); 
       return exprResult;
    }

    @Override
    public Object visitRecordExpression(RecordExpression ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitUnaryExpression(UnaryExpression ast, Object o) {
        code.append("; Comienza UNARY_EXPRESSION\n");
        String expr = (String) ast.E.visit(this, o);
        String op = ast.O.spelling;
        String result;
        
        switch(op){
            case "-" -> {
                result = newTemp("neg");
                code.append("  " + result + " = sub i32 0, " + expr + "\n");
            }
            case "!" -> {
                //No see si esta implementado estro en trianguloo pero x si acaso lo pongo
                result = newTemp("not");
                code.append("  " + result + " = xor i1 " + expr + ", true\n");
            }
            case "+" -> result = expr;
            default -> {
                result = "0";
                System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA no se algo paso en unary expression" + op);
            }
        }
        code.append("; Termina UNARY_EXPRESSION\n");

        return result;
        
        
    }

    @Override
    public Object visitVnameExpression(VnameExpression ast, Object o) {
        return ast.V.visit(this, o);
    }

    //////////////////////////////////// DECLARATION
    
    @Override
    public Object visitVarDeclaration(VarDeclaration ast, Object arg) {
        String varName = ast.I.spelling;
        String regName = "%" + varName;
        locals.put(varName, regName); 
        if(ast.T instanceof ArrayTypeDenoter){
            String arraySize = (String) ast.T.visit(this, arg);
            code.append("  " + regName + " = alloca [" + arraySize + " x i32], align 16\n");
        } else{
            code.append("  " + regName + " = alloca i32, align 4\n");
        }
        
        return null;
    }
    
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
        ast.D1.visit(this, o);
        ast.D2.visit(this, o);
        return null;
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
        List<String> result = new ArrayList<>();
        String expressionValue = (String) ast.E.visit(this, null);
        result.add(expressionValue);

        List<String> expressionsArray = (List<String>) ast.AA.visit(this, null);
        result.addAll(expressionsArray);

        return result;
    }

    @Override
    public Object visitSingleArrayAggregate(SingleArrayAggregate ast, Object o) {  
        List<String> result = new ArrayList<>();
        String expressionValue = (String) ast.E.visit(this, null);
        result.add(expressionValue);
        return result;
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
        return ast.E.visit (this, o);
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
        return locals.get(((SimpleVname) ast.V).I.spelling);  //Retorna el puntero de la variable        
    }

    //////////////////////////////////// APS - ACTUAL PARAMETER SEQUENCE
    
    @Override
    public Object visitEmptyActualParameterSequence(EmptyActualParameterSequence ast, Object o) {
        return 0;
    }

    @Override
    public Object visitMultipleActualParameterSequence(MultipleActualParameterSequence ast, Object o) {
        String reg1 = (String) ast.AP.visit(this, o);
        functionArgs.add(reg1);
        ast.APS.visit(this, o);
        return o;
    }

    @Override
    public Object visitSingleActualParameterSequence(SingleActualParameterSequence ast, Object o) {
        String reg = (String) ast.AP.visit(this, o);
        functionArgs.add(reg);
        return o;
    }
    
    ////////////////////////////////////// TYPE DENOTER

    @Override
    public Object visitAnyTypeDenoter(AnyTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitArrayTypeDenoter(ArrayTypeDenoter ast, Object o) {
        return ast.IL.spelling;
    }

    @Override
    public Object visitBoolTypeDenoter(BoolTypeDenoter ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitCharTypeDenoter(CharTypeDenoter ast, Object o) {
        if (ast.entity == null) {
        ast.entity = new TypeRepresentation(Machine.characterSize);        
    }
    return new Integer(Machine.characterSize);
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
        if (ast.decl.entity instanceof PrimitiveRoutine) {          
          //Funciones estandar
          String callInstr = ((PrimitiveRoutine) ast.decl.entity).call;
          if (callInstr.contains("@printf")){
              code.append(String.format(callInstr, functionArgs.toArray()));
          } else if (callInstr.contains("@get")){
             String tmpReg = newTemp();
             code.append(String.format(callInstr, tmpReg, tmpReg, functionArgs.get(0)));
          }                              
        } else { 
            //Funciones normales
          this.reporter.reportError("el identificador no se encontro", ast.spelling, ast.position);
        }
        return null;
    }

    @Override
    public Object visitIntegerLiteral(IntegerLiteral ast, Object o) {
        return ast.spelling;
    }

    
    //////////////////////////// OPERATOR
    
    @Override
    public Object visitOperator(Operator ast, Object o) {
        return ast.spelling;
    }
    

    //////////////////////////////// VNAME

    @Override
    public Object visitDotVname(DotVname ast, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public Object visitSimpleVname(SimpleVname ast, Object o) {                
        //Cargar el valor de la variable
        String varName = ast.I.spelling;
        String ptr = locals.get(varName);
        String tmpReg = newTemp("varName");
        code.append("  " + tmpReg + " = load i32, ptr " + ptr + ", align 4\n");
        return tmpReg;              
    }

    @Override
    public Object visitSubscriptVname(SubscriptVname ast, Object o) {
        //Cargar el valor de un array
        SimpleVname vName = (SimpleVname) ast.V;
        String varName = vName.I.spelling;
        String ptr = locals.get(varName);
        String arraySize = (String) vName.type.visit(this, null);
        
        String tmpReg = newTemp("arrayPtr");
        String index = (String) ast.E.visit(this, null);
        
        code.append("  " + tmpReg + " = getelementptr inbounds [" + arraySize +" x i32], ptr " + ptr + ", i64 0, i64 " + index + "\n");
        
        String finalReg = newTemp("arrayValue");
        code.append("  " + finalReg + " = load i32, ptr " + tmpReg + ", align 4\n");
        
        return finalReg;
    }
    
    private final void elaborateStdPrimRoutine (Declaration routineDeclaration, String call) {
        routineDeclaration.entity = new PrimitiveRoutine (call);    
  }

    private final void elaborateStdEnvironment() {        
        //Declarations
        code.append("\n;Constantes \n");
        code.append("@stringFormat.char = private unnamed_addr constant [3 x i8] c\"%c\\00\", align 1 \n");        
        code.append("@stringFormat.int = private unnamed_addr constant [3 x i8] c\"%d\\00\", align 1 \n");        
        code.append("\n;Funciones \n");
        code.append("declare i32 @printf(ptr noundef, ...) #1 \n");
        code.append("declare i32 @getchar() #2 \n");
        code.append("""
                    
                    define dso_local i32 @get() #0 {
                      %1 = alloca i32, align 4
                      %2 = call i32 @getchar()
                      store i32 %2, ptr %1, align 4
                      br label %Get.loop1
                    
                    Get.loop1:  
                      %4 = call i32 @getchar()
                      %5 = icmp ne i32 %4, 10
                      br i1 %5, label %Get.loop2, label %Get.loop3
                    
                    Get.loop2:   
                      br label %Get.loop1
                    
                    Get.loop3:   
                      %8 = load i32, ptr %1, align 4
                      ret i32 %8
                    }\n""");
        
        // Calls
          elaborateStdPrimRoutine(StdEnvironment.getDecl, "  %s = call i32 @get() \n  store i32 %s, ptr %s \n"); //Leer valor y guardarlo
          elaborateStdPrimRoutine(StdEnvironment.putDecl,  "  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.char, i32 noundef %s) \n");
          elaborateStdPrimRoutine(StdEnvironment.getintDecl, "  %s = call i32 @get() \n  store i32 %s, ptr %s \n");
          elaborateStdPrimRoutine(StdEnvironment.putintDecl, "  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %s) \n");
      }
}

  