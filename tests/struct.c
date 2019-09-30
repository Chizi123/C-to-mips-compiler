struct test
{
    int i;
    int j;
    char *str;
    char *a[5];
};

int main ()
{
	struct test aaa;
	aaa.i = 3;
	aaa.j = 4;
	strcpy("Hello", aaa.str);
	aaa.i = 4;
	aaa.a = 'c';

	return aaa.i;
	return aaa.j;
}