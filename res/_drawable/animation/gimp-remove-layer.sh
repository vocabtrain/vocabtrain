#!/bin/zsh
gimp -n -i -b - <<EOF
(let* ( (file's (cadr (file-glob "*.xcf" 1))) (filename "") (image 0) (layer 0) )
  (while (pair? file's) 
    (set! image (car (gimp-file-load RUN-NONINTERACTIVE (car file's) (car file's))))
    (script-fu-gimp-layer-group-hideallbut image "$1")
    (set! layer (car (gimp-image-merge-visible-layers image CLIP-TO-IMAGE)))
    (set! filename (string-append (substring (car file's) 0 (- (string-length (car file's)) 4)) ".png"))
    (gimp-file-save RUN-NONINTERACTIVE image layer filename filename)
    (gimp-image-delete image)
    (set! file's (cdr file's))
    )
  (gimp-quit 0)
  )
EOF
    #(script-fu-gimp-layer-group-show image "$1" FALSE "")
