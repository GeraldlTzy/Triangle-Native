; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %n = alloca i32, align 4
  store i32 56, ptr %n, align 4
  %tmp0 = load i32, ptr %n, align 4
  %tmp1 = add i32 %tmp0, 1
  store i32 %tmp1, ptr %n, align 4
  ret i32 0
}
