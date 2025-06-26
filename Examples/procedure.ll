
; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %.x = alloca i32, align 4
  store i32 10, ptr %.x, align 4
  %tmp2 = call i32 @suma( ptr %.x, i32 5)
  %varValue3 = load i32, ptr %.x, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %varValue3) 
  ret i32 0
}

;Constantes 
@stringFormat.char = private unnamed_addr constant [3 x i8] c"%c\00", align 1 
@stringFormat.int = private unnamed_addr constant [3 x i8] c"%d\00", align 1 

;Funciones 
declare i32 @printf(ptr noundef, ...) #1 
declare i32 @getchar() #2 

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
}

define dso_local i32 @suma(ptr %suma.x, i32 %suma.n) { 
; Comienza BINARY_EXPRESSION 
  %varValue0 = load i32, ptr %suma.x, align 4
  %binaryRes1 = add i32 %varValue0, %suma.n
; Termina BINARY_EXPRESSION 
  store i32 %binaryRes1, ptr %suma.x, align 4
  ret i32 0 
}
