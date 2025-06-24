
;Constantes 
@stringFormat.char = private unnamed_addr constant [3 x i8] c"%c\00", align 1 
@stringFormat.int = private unnamed_addr constant [3 x i8] c"%d\00", align 1 

;Funciones 
declare i32 @printf(ptr noundef, ...) #1 

; Código LLVM generado por Triangle

define i32 @main() {
entry:
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef 8) 
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.char, i32 noundef 68) 
  ret i32 0
}
