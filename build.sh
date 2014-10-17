#!/bin/bash
# Собирает все разноцветные версии TabunAC

# В ant.properties должны быть указаны параметры keystore-а (jks)
# в параметрах keystore, keystore.password, keystore.alias, keystore.alias.password

# В local.properties должен быть указан путь к Android SDK в sdk.dir

# Почему ant? Gradle - чёртов слоупок.

mkdir -p apk;

for F in $(ls themes | grep -Po ".*(?=\Q.xml\E)")
do
	cp -f themes/${F}.xml 'res/values/colors.xml'
	ant
	cp 'bin/ac-release.apk' apk/ac-${F}.apk
done
