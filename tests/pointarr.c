int main()
{
    int a[5];
    int *b;
    int i;
    b=(int *) a;
    i = 0;
    while (i < 5) {
        b[i] = i + 1;
        i = i + 1;
    }
//    b[0] = 1;
//    b[1] = 2;
//    b[2] = 3;
//    b[3] = 4;
//    b[4] = 5;
//    i = 5;
    while (i > 0) {
        i = i - 1;
        print_i(a[i]);
        print_c(' ');
    }
//    b[1]=4;
//    print_i(a[1]);
    print_c('\n');
}