
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
  %x = alloca i32, align 4
  %result = alloca i32, align 4
  store i32 0, ptr %x, align 4
; Comienzo de IF_EXPRESSION 
  %if_result_ptr0 = alloca i32, align 4
; Comienza BINARY_EXPRESSION 
  %varName1 = load i32, ptr %x, align 4
  %binaryRes2 = icmp sgt i32 %varName1, 1
; Termina BINARY_EXPRESSION 
  br i1 %binaryRes2, label %then0, label %else1
then0:
  store i32 10, ptr %if_result_ptr0, align 4
  br label %end_if2
else1:
  store i32 20, ptr %if_result_ptr0, align 4
  br label %end_if2
end_if2:
  %if_result3 = load i32, ptr %if_result_ptr0, align 4
; Fin de IF_EXPRESSION 
  store i32 %if_result3, ptr %result, align 4
  %varName4 = load i32, ptr %result, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %varName4) 
  ret i32 0
}
