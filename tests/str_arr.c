char main()
{
    char c[7];
    int i;
    c[0] = 'a';
    c[1] = 'b';
    c[2] = 'c';
    c[3] = 'c';
    c[4] = 'd';
    c[5] = c[4];
    c[6] = 'f';
    c[7] = 'g';
    return c[8];
}