#!/bin/zsh

function distribute()
{
	RES=(120 160 240 320)
	DIRS=(ldpi mdpi hdpi xhdpi)
	for i in `seq 1 4`; do
		res=$RES[$i]
		((l=$2*$res/160))
		convert $1 -resize ${l}x${l}\! ../drawable-$DIRS[i]/$1
	done
}
function distributeFlags()
{
	RES=(120 160 240 320)
	DIRS=(ldpi mdpi hdpi xhdpi)
	for i in `seq 1 4`; do
		res=$RES[$i]
		((l=$2*$res/160))
		convert $1 -resize ${l}x1000 ../drawable-$DIRS[i]/$1
	done
}

gimp -n -i -b - <<EOF
(let* ( (file's (cadr (file-glob "*.xcf" 1))) (filename "") (image 0) (layer 0) )
  (while (pair? file's) 
    (set! image (car (gimp-file-load RUN-NONINTERACTIVE (car file's) (car file's))))
    (set! layer (car (gimp-image-merge-visible-layers image CLIP-TO-IMAGE)))
    (set! filename (string-append (substring (car file's) 0 (- (string-length (car file's)) 4)) ".png"))
    (gimp-file-save RUN-NONINTERACTIVE image layer filename filename)
    (gimp-image-delete image)
    (set! file's (cdr file's))
    )
  (gimp-quit 0)
  )
EOF

for i in *.svg; do inkscape $i --export-png=$i:r.png; done

for i in ic_menu*.png; do
	distribute "$i" 48
done

distribute 'ic_home.png' 48

for i in ic_pref*.png; do
	distribute "$i" 48
done

for i in ic_dashbasic_*.png; do
	distribute "$i" 86
done

for i in ic_dash_*.png; do
	distribute "$i" 64
done

for i in ic_training*.png; do
	distribute "$i" 48
done

for i in ic_sync_tab*.png; do
	distribute "$i" 32
done

for i in ic_flags*.png; do
	distributeFlags "$i" 64
done

for i in sync_*.png; do
	distribute "$i" 48
done

for i in ic_slidelockthumb*.png; do
	distribute "$i" 36
done

for i in anim_tatoeba_bubbles_*; do
	distribute "$i" 12
done

	

