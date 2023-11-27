# SWE-622 Programming Assignment 1
## File Sharing System (FSS) using Java RMI

### Set up the File Server
Navigate to the folder where `pa2.jar` is located and run these commands:
```console
mkdir -p server
cd server
java -cp ..\pa2.jar Server start <port>
```
The `server` folder will be the base folder for the File Server.

The argument `<port>` indicates server port.

### Set up a Client
Navigate to the folder where `pa2.jar` is located and run these commands:
```console
set PA1_SERVER=<address>:<port>
mkdir client
cd client
```
The `client` folder will be used as the base folder for the client.

The argument `<address>` indicates the address of server. It would be `localhost` if both server and client are run on local machine.

The argument `<port>` is the server port. It should be the same that was provided when setting up the server.

Any number of client folders can be created, the base folder will be the one where below commands are executed.

### Run Client
The file path for all the commands will use context root as the base directories of client and server respectively.

#### Upload File
```console
java -cp ..\pa2.jar Client upload </path/filename/on/client> </path/filename/on/server>
```

#### Download File
```console
java -cp ..\pa2.jar Client download </path/filename/on/server> </path/filename/on/client>
```

#### Delete File
```console
java -cp ..\pa2.jar Client rm </path/filename/on/server>
```

#### Create Directory
```console
java -cp ..\pa2.jar Client mkdir </path/on/server>
```

#### Delete Directory
```console
java -cp ..\pa2.jar Client rmdir </path/on/server>
```

#### Get Contents in a Directory
```console
java -cp ..\pa2.jar Client dir </path/on/server>
```

#### Shutdown File Server
```console
java -cp ..\pa2.jar Client shutdown
```
