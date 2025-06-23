; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %x = alloca i32, align 4
  store i32 5, ptr %x, align 4
; Comienzo de IF_COMMAND 
; Comienza BINARY_EXPRESSION 
  %tmp0 = load i32, ptr %x, align 4
  %binaryRes1 = icmp eq i32 %tmp0, 3
; Termina BINARY_EXPRESSION 
  br i1 %binaryRes1, label %then0, label %else1
then0:
  store i32 10, ptr %x, align 4
  br label %end_if2
else1:
  store i32 0, ptr %x, align 4
  br label %end_if2
end_if2:
; Fin de IF_COMMAND 
  ret i32 0
}
