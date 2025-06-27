
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
  %.c = alloca [5 x i32], align 16
  %.x = alloca i32, align 4
; Comienza ARRAY_EXPRESSION 
  %index0 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 0
  store i32 4, ptr %index0, align 4
  %index1 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 1
  store i32 4, ptr %index1, align 4
  %index2 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 2
  store i32 5, ptr %index2, align 4
  %index3 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 3
  store i32 6, ptr %index3, align 4
  %index4 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 4
  store i32 7, ptr %index4, align 4
  %arrayPtr5 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 4
  %arrayValue6 = load i32, ptr %arrayPtr5, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue6) 
  %arrayValue7 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 4
  store i32 5, ptr %arrayValue7, align 4
  %arrayPtr8 = getelementptr inbounds [5 x i32], ptr %.c, i64 0, i64 4
  %arrayValue9 = load i32, ptr %arrayPtr8, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue9) 
  store i32 3, ptr %.x, align 4
  %varValue10 = load i32, ptr %.x, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %varValue10) 
  ret i32 0
}
