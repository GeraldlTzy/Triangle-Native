
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

; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %const_char0 = alloca i32
  store i32 58, ptr %const_char0
  %const_int1 = alloca i32
  store i32 3, ptr %const_int1
  %varName2 = load i32, ptr %const_char0, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.char, i32 noundef %varName2) 
  %varName3 = load i32, ptr %const_int1, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %varName3) 
  ret i32 0
}
