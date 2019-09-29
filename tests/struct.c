struct test
{
    int i;
    int j;
    char *str;
};

int main ()
{
	struct test aaa;
	aaa.i = 3;
	aaa.j = 4;
	strcpy("Hello", aaa.str);
}