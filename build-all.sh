#!/bin/bash
# Собирает все разноцветные версии TabunAC

# В ant.properties должны быть указаны
# - Параметры keystore-а (jks) в key.store, key.store.password, key.alias, key.alias.password
# - Путь к Android SDK в sdk.dir

# Почему ant? Gradle - чёртов слоупок.

# cd img;
# echo "Rendering icons";
# bash "build_icons.sh";
# cd ..;
mkdir -p apk

cp -frT 'res' '.res_cached';

for F in $(ls themes)
do
	echo "==== Copying "${F}
	cp -frvT themes/${F} res;

	echo "==== Building "${F}
	ant release > /dev/null;

	mv -f bin/ac-release.apk apk/ac-${F}.apk;
	echo "==== Built "${F}

	echo "==== Restoring initial state"
	rm -rf bin/res
	cp -rfT .res_cached res;

done;

rm -rf '.res_cached';
