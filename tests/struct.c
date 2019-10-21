struct test
{
    int i;
    int j;
    char *str;
    char *a[5];
};

int strcpy(char *src, char* dest){}

int main ()
{
	struct test aaa;
	struct test* bbb;
	int a[5][10];
	aaa.i = 3;
	aaa.j = 4;
    (aaa.a)[1];
    (*(aaa.i).i)[4];
	strcpy("Hello", aaa.str);
	aaa.i = 4;
//	*(bbb.a[1])[0].a.a = 'c';
	a[1][1][3] = 2;

	return aaa.i;
	return aaa.j;
	return 0;
}