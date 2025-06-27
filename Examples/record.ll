
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
%fechaS = type { i32, i32 }
%fecha2S = type { i32, i32 }

; Código LLVM generado por Triangle

define i32 @main() {
entry:
  %.fecha = alloca %fechaS, align 4
  %.fecha2 = alloca %fecha2S, align 4
  %recordIndex0 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 0
  store i32 7, ptr %recordIndex0, align 4
  %recordIndex1 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 1
  store i32 4, ptr %recordIndex1, align 4
  %recordAttribute2 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 1
  %arrayValue3 = load i32, ptr %recordAttribute2, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue3) 
  %recordAttribute4 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 1
  store i32 6, ptr %recordAttribute4, align 4
  %recordAttribute5 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 1
  %arrayValue6 = load i32, ptr %recordAttribute5, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue6) 
  %recordAttribute7 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  %recordAttribute8 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 1
  %arrayValue9 = load i32, ptr %recordAttribute8, align 4
  store i32 %arrayValue9, ptr %recordAttribute7, align 4
  %recordAttribute10 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  %arrayValue11 = load i32, ptr %recordAttribute10, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue11) 
  %recordAttribute12 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  %recordAttribute13 = getelementptr inbounds nuw %fechaS, ptr %.fecha, i32 0, i32 0
  %arrayValue14 = load i32, ptr %recordAttribute13, align 4
  store i32 %arrayValue14, ptr %recordAttribute12, align 4
  %recordAttribute15 = getelementptr inbounds nuw %fecha2S, ptr %.fecha2, i32 0, i32 0
  %arrayValue16 = load i32, ptr %recordAttribute15, align 4
  call i32 (ptr, ...) @printf(ptr noundef @stringFormat.int, i32 noundef %arrayValue16) 
  ret i32 0
}
