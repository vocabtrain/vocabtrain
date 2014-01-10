#!/bin/zsh
states=(selected pressed deactivated)
for state in $states; do
	current_states=("${(@)states:#$state}")
	./gimp-remove-layer.sh $current_states
	for file in *.png; do mv $file ../$file:r_$state.png; done
done
./gimp-remove-layer.sh $states
for file in *.png; do mv $file ../$file:r_default.png; done
cd ..
for i in ic_dash_*deactivated*.png; do
	mv $i `echo $i | sed 's@deactivated@disabled@g'`;
done
