Author: Zhipeng Hu

The SimpleFTP folder contains both client and server folders which are set respectively as client space and server space. The client side includes uploadTest.txt which can be used as upload test file. Likewise, the server side includes downloadTest.txt which can be used as download test file.

SimpleFTP is implemented based on UDP and the server is multithreaded. A hashset is used to store all port number assigned to existing connection. A hashmap is used to index the connection of existing socket. The program is developed based on GNU command line parser (Detail see license). Make sure to include jargs-1.0 in the build path when running program.

Command Usage:  "help -h put -p get -g aliasname -a"

PUT and GET should not be used in the same command. -p or -g should follow the test filename
If -a is not used, the destination file name would be set as default "Result.txt".
-h is for showing help information

The initial request will use port 65 so make sure port 65 is not taken.


Upload
1. Compile and run server.java
2. Compile and run ClientParser.java with arguments "-p uploadTest.txt -a uploadResult.txt -h"
3. Go to current project work space directory and find "uploadResult.txt" in the server folder.

Repeat the above process and see how it goes when the result has already exist on server.

Download
1. Compile and run server.java
2. Compile and run ClientParser.java with arguments "-g downloadTest.txt -a downloadResult.txt -h"
3. Go to current project work space directory and find "downloadResult.txt" in the client folder.
