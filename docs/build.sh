#!/bin/bash
cd `dirname $0`

rm -rf public

hugo
find ./ -name '*.html' -exec sed -i -e 's|[[:space:]]*<option id="\([a-zA-Z]\+\)" value="|<option id="\1" value="/elasticjob/current|g' {} \;
cd public/en
sed -i -e 's/cn/en/g' index.html

cd ..
