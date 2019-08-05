# Minesweeper Server


### run in dev mode

``` 
sbt run
```

The server will become available at port 9000.

All source code changes are recompiled on the fly thanks to PlayFramework's amazing hot reloading feature. 
 
There's no ~~spoon~~ fork so debugger is attachable. Use `-jvm-debug 9999` sbt parameter or IDE's debug configuration.  

#### build and run
```
docker build . -t minesweeper
docker run -it --rm -p 9000:9000 minesweeper
```

####  run websocket client to Play!
    # if you have `wscat` in your PATH
    wscat -c localhost:9000

    # if you have fresh enouh npm installed
    npx wscat -c localhost:9000

    # if you love containers more than polluting your system with once-run executables
    docker run -it --rm --net=host joshgubler/wscat -c localhost:9000
