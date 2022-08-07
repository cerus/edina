#!/bin/bash

rm -rf asm_backup
cp -r edinaj/src/main/java/dev/cerus/edina/edinaj/compiler/step/classes asm_backup

declare -A replacements
replacements=(
  ["\"dev/cerus/edina/edinaj/asm/Stack\""]="settings.getStackName()"
  ["\"dev/cerus/edina/edinaj/asm/Launcher\""]="settings.getAppLauncherName()"
  ["\"dev/cerus/edina/edinaj/asm/App\""]="settings.getAppName()"
  ["\"dev/cerus/edina/edinaj/asm/Natives\""]="settings.getNativesName()"
  ["dev/cerus/edina/edinaj/asm/Stack"]="\" + settings.getStackName() + \""
  ["dev/cerus/edina/edinaj/asm/Launcher"]="\" + settings.getAppLauncherName() + \""
  ["dev/cerus/edina/edinaj/asm/App"]="\" + settings.getAppName() + \""
  ["dev/cerus/edina/edinaj/asm/Natives"]="\" + settings.getNativesName() + \""
  ["(V17,"]="(V1_8,"
)

replacer () {
  local inf="$1.txt"
  local outf="$1_mod.txt"
  local outf2="$1_mod2.txt"
  local jout="edinaj/src/main/java/dev/cerus/edina/edinaj/compiler/step/classes/Class$2Step.java"
  local jout2="edinaj/src/main/java/dev/cerus/edina/edinaj/compiler/step/classes/Class$22Step.java"
  local cls_name_unquoted="dev/cerus/edina/edinaj/asm/$2"
  local cls_name_quoted="\"$cls_name_quoted\""

  IN="$(grep -rn "classWriter.visit(" $inf)"
  mapfile -td \: fields < <(printf "%s\0" "$IN")
  let startline=${fields[0]}
  IN="$(grep -rn "return classWriter.toByteArray();" $inf)"
  mapfile -td \: fields < <(printf "%s\0" "$IN")
  let endline=${fields[0]}-2

  cp $inf $outf
  awk "NR >= $startline && NR <= $endline" $outf > $outf2
  mv $outf2 $outf

  IN="$(grep -rn "classWriter.visit(" $jout)"
  mapfile -td \: fields < <(printf "%s\0" "$IN")
  let startline=${fields[0]}-1
  IN="$(cat $jout | wc -l)"
  let endline=$IN-3

  cp $jout $jout2
  #cat $jout | sed -n "${startline},${startline}p" > $jout2
  head -$startline $jout > $jout2
  cat $outf >> $jout2
  tail -n 3 $jout >> $jout2

  for i in "${!replacements[@]}"
  do
  	replace "$i" "${replacements[$i]}" -- $jout2
  done

  mv $jout2 $jout
  rm $outf
}

replacer "asm_stack" "Stack"
replacer "asm_launcher" "Launcher"
replacer "asm_natives" "Natives"