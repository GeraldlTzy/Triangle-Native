 #include <stdio.h>

 int get(){
     int c = getchar();
     while (getchar() != '\n'){}
     return c;
 }


int main (){
    char a;
    a = get();
    printf("%c", a);
    a = get();
    printf("%c", a);
}

