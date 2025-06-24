; Código LLVM generado por Triangle

@.str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
declare i32 @printf(ptr, ...)


define i32 @main() {
entry:
  %x = alloca i32, align 4
  store i32 0, ptr %x, align 4
; Comienza WHILE_COMMAND 
  br label %while_start0
while_start0: 
; Comienza BINARY_EXPRESSION 
  %tmp0 = load i32, ptr %x, align 4
  %binaryRes1 = icmp slt i32 %tmp0, 5
; Termina BINARY_EXPRESSION 
  br i1 %binaryRes1, label %while_body2, label %while_end1
while_body2: 
; Comienza BINARY_EXPRESSION 
  %tmp2 = load i32, ptr %x, align 4
  %binaryRes3 = add i32 %tmp2, 1
; Termina BINARY_EXPRESSION 
  store i32 %binaryRes3, ptr %x, align 4
  
  %print0 = load i32, ptr %x, align 4
  call i32 (ptr, ...) @printf(ptr @.str, i32 %print0)
  
  br label %while_start0
while_end1: 
; Termina WHILE_COMMAND 

  ret i32 0
}
