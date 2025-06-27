
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
%fecha2S = type { i32, i32 }

; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %.fecha2 = alloca %fecha2S, align 4
  %recordAttribute0 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  %arrayValue1 = load i32, ptr %recordAttribute0, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue1) 
  %recordAttribute2 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  store i32 3, ptr %recordAttribute2, align 4
  %recordAttribute3 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  %arrayValue4 = load i32, ptr %recordAttribute3, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue4) 
  ret i32 0
}
