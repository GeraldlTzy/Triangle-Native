
;Constantes 
@stringFormat.char = private unnamed_addr constant [3 x i8] c"%c\00", align 1 
@stringFormat.int = private unnamed_addr constant [3 x i8] c"%d\00", align 1 

;Funciones 
declare i32 @printf(ptr noundef, ...) #1 

; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %c = alloca i32, align 4
  %i = alloca i32, align 4
  store i32 58, ptr %c, align 4
  store i32 3, ptr %i, align 4
  %tmp0 = load i32, ptr %c, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.char, i32 noundef %tmp0) 
  %tmp1 = load i32, ptr %i, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %tmp1) 
  ret i32 0
}
