#!/bin/bash
# Собирает все разноцветные версии TabunAC

# В ant.properties должны быть указаны
# - Параметры keystore-а (jks) в key.store, key.store.password, key.alias, key.alias.password
# - Путь к Android SDK в sdk.dir

# Почему ant? Gradle - чёртов слоупок.

cd img;
echo "Rendering icons";
bash "build_icons.sh";
cd ..;

rm -rf bin;
mkdir -p apk;
cp -f 'res/values/colors.xml' '.colors_cached.xml';
bash build.sh;

for F in $(ls themes | grep -Po ".*(?=\Q.xml\E)")
do
	cp -f themes/${F}.xml 'res/values/colors.xml';
	echo "==== Building "${F}
	ant release;
	cp 'bin/ac-release.apk' apk/ac-${F}.apk;
	echo "==== Built "${F}
done;

mv -f '.colors_cached.xml' 'res/values/colors.xml';
