; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %x = alloca i32, align 4
  store i32 5, ptr %x, align 4
; Comienzo de IF_COMMAND 
  %tmp0 = load i32, ptr %x, align 4
  %tmp1 = add i32 %tmp0, 3
  %cond2 = icmp ne i32 %tmp1, 0
  br i1 %cond2, label %then0, label %else1
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
