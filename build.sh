#!/bin/bash
# Собирает основную версию TabunAC

# В ant.properties должны быть указаны
# - Параметры keystore-а (jks) в key.store, key.store.password, key.alias, key.alias.password
# - Путь к Android SDK в sdk.dir

# Почему ant? Gradle - чёртов слоупок.

rm -rf bin;
mkdir -p apk;
echo "==== Building main"
ant release
cp 'bin/ac-release.apk' apk/ac.apk;
echo "==== Built main"