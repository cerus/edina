rm -rf doc_temp
mkdir doc_temp
cd doc_temp
git clone git@github.com:cerus/edina.wiki.git
cd edina.wiki
rm -rf docs
mkdir docs

cd ../../
java -jar eddoc/target/eddoc.jar -I stdlib -O ./doc_temp/edina.wiki/docs
cd doc_temp/edina.wiki
sed -i -E "s*\(stdlib([A-Za-z0-9_/]+)?/*\(*" ./docs/index.md
replace ".md)" ")" -- ./docs/index.md
replace "# Index" "# Stdlib Documentation Index" -- ./docs/index.md

git add *
git commit -m "Update docs"
git push

cd ../..
rm -rf doc_temp