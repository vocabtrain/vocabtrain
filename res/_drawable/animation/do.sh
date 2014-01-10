#!/bin/zsh
for i in `seq -f "%02.f" 1 23`; do
	./gimp-remove-layer.sh $i
	for file in *.png; do mv $file ../$file:r_$i.png; done
done

