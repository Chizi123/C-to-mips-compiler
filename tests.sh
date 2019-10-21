#!/bin/bash

for i in tests/*.c
do
	echo $i
	java -cp bin Main -$1 $i dummy.out
done
