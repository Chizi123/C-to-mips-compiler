#include "minic-stdlib.h"

int main ()
{
    print_s("hello world!\n");
    print_s("This is working\n");
    print_i(4+3+4);
    print_i(3<2);
    print_i(2>1);
    print_i(1==1);
    print_i(0!=1);
    print_i(4-(3+4));
    print_i(3<=2);
    print_i(2>=1);
    print_i(1&&1);
    print_i(0||1);
}