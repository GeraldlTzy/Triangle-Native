; Código LLVM generado por Triangle

@.str = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
declare i32 @printf(ptr, ...)


define i32 @main() {
entry:
  %x = alloca i32, align 4
  store i32 3, ptr %x, align 4
; Comienzo de IF_COMMAND 
; Comienza BINARY_EXPRESSION 
  %tmp0 = load i32, ptr %x, align 4
  %binaryRes1 = icmp eq i32 %tmp0, 3
; Termina BINARY_EXPRESSION 
  br i1 %binaryRes1, label %then0, label %else1
then0:

  store i32 10, ptr %x, align 4

  %print0 = load i32, ptr %x, align 4
  call i32 (ptr, ...) @printf(ptr @.str, i32 %print0)

  br label %end_if2
else1:
  store i32 0, ptr %x, align 4

  %print1 = load i32, ptr %x, align 4
  call i32 (ptr, ...) @printf(ptr @.str, i32 %print0)

  br label %end_if2
end_if2:
; Fin de IF_COMMAND 
  ret i32 0
}
