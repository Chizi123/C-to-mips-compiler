int mul(int i, int j)
{
    return i * j;
}

int square(int i)
{
    return mul(i,i);
}

int cube(int i)
{
//    return mul(mul(i,i), i);
//    return mul(i,i) * i;
//    return square(i) * square(i);
    return i*square(i);
}

int bmul(int a, int b, int c, int d, int e, int f)
{
    return a*b*c*d*e*f;
}

int main()
{
//    int i;
    print_s((char *) "Hello there\n");
    print_i(mul(square(4),square(3)));
    print_c('\n');
    print_i(4*4*3*3);
    print_c('\n');
    print_i(square(5));
    print_c('\n');
    print_i(cube(3));
    print_c('\n');
    print_i(bmul(2,3,4,5,6,7));
    print_c('\n');
    print_i(2*3*4*5*6*7);
    print_c('\n');
}