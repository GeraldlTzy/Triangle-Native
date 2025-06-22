package Triangle.CodeGenerator;
//import AST.*;

import Triangle.AbstractSyntaxTrees.Command;
import Triangle.AbstractSyntaxTrees.*;
import Triangle.ErrorReporter;


public final class LLVMGenerator extends Encoder {
    private StringBuilder code;

    public LLVMGenerator(ErrorReporter reporter) {
        super(reporter);
    }

    public String generate(Program ast) {
        code = new StringBuilder();
        code.append("; Código LLVM generado por Triangle\n\n");
        code.append("define i32 @main() {\n");
        code.append("entry:\n");

        ast.visit(this, null);

        code.append("  ret i32 0\n");
        code.append("}\n");

        return code.toString();
    }

    @Override
    public String visitVarDeclaration(VarDeclaration ast, Object arg) {
        String varName = ast.I.spelling;
        code.insert(0, "@" + varName + " = dso_local global i32 0, align 4\n");
        return null;
    }
}