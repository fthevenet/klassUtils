Usage:
```
java -jar build/libs/klassUtils-0.1.jar
```

Generate 65535 java files, compile, load and dump CDS archive:
```
export NAME=test245_60; rm -rf /tmp/$NAME ; java -jar klassUtils-0.1.jar gen -o /tmp/$NAME -n 65535 -f 245 -m 60 ; javac -J-Xmx40g /tmp/$NAME/*.java ; pushd /tmp/$NAME; jar cf /tmp/$NAME.jar *.class ; popd; java -XX:ArchiveClassesAtExit=$NAME.jsa -cp /tmp/$NAME.jar ClassLoadTest
```

Generate static CDS archive with CCP off:
```
java  -XX:-UseCompressedClassPointers  -Xshare:off -XX:DumpLoadedClassList=static.classlist -jar build/libs/klassUtils-0.1.ja
java  -XX:-UseCompressedClassPointers  -Xshare:dump -XX:SharedArchiveFile=static_noCCS.jsa -XX:DumpLoadedClassList=static.classlist -jar build/libs/klassUtils-0.1.jar
```

Generate and load 600K classes, wait for user input to terminate:
```
java  -XX:-UseCompressedClassPointers -XX:+RecordDynamicDumpInfo -XX:SharedArchiveFile=static_noCCS.jsa -jar build/libs/klassUtils-0.1.jar load -n 600000 -w
```