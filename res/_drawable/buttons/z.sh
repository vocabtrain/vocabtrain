#!/bin/zsh
for i in *.xcf; do
	base=$i:r
	xmlfile=`echo $base.xml | sed 's@^ic_@btn_@'`
	
	cat > $xmlfile <<EOF
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/${base}_pressed"
        android:state_focused="true"
        android:state_enabled="true"
        android:state_pressed="true" />
    <item android:drawable="@drawable/${base}_disabled"
        android:state_enabled="false"
       />
    <item android:drawable="@drawable/${base}_pressed"
        android:state_focused="false"
        android:state_enabled="true"
        android:state_pressed="true" />
    <item android:drawable="@drawable/${base}_selected" 
            android:state_enabled="true"
            android:state_focused="true" />
    <item android:drawable="@drawable/${base}_default"
        android:state_enabled="true"
        android:state_focused="false"
        android:state_pressed="false" />
</selector>
EOF
	done	

