#include "minic-stdlib.h"

int square(int i)
{
    return i*i;
}
int main ()
{
    int i;
    i = 1;
    i = i +2;
    i = i * i * i *i*i*i+i+i+i+(-i)+i+i+i;
    print_i(i);
    print_c('\n');
    print_i(square(9999));
    print_c('\n');
    print_s((char*) "hello world!\n");
//    print_s("This is working\n");
//    print_i(4+3+4+5+6+7+8+9+10+11);
//    print_c('\n');
//    print_i(3<2);
//    print_c('\n');
//    print_i(2>1);
//    print_c('\n');
//    print_i(1==1);
//    print_c('\n');
//    print_i(0!=1);
//    print_c('\n');
//    print_i(4-3-4);
//    print_c('\n');
//    print_i(3<=2);
//    print_c('\n');
//    print_i(2>=1);
//    print_c('\n');
//    print_i(1&&1);
//    print_c('\n');
//    print_i(0&&1);
//    print_c('\n');
//    print_i(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+(1+1)))))))))))))))))))))))))))))));
//    print_c('\n');
//    print_i(5*4);
//    print_c('\n');
//    print_i(5/4);
//    print_c('\n');
//    print_i(5%4);
//    print_c('\n');
//    print_i((1||0)+(0||1)+(1&&1)+(1&&1));
//    print_c('\n');
//    print_c('c');
//    print_c('\n');
}