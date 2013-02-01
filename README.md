Pesterchum
==========
This is an unofficial rewrite of Pesterchum with no connections to either Pesterchum or MSPaintAdventures,
the goal of which is to address some flaws.
* Firstly portability, the download is massive for such a simple application, 
and although it does have cross-platform compatability, 
it's still not as good as Java's. 
* Secondly, nothing on it is encrypted, 
it just uses an IRC backend, so this version will have encryption and also user logins. 
* Thirdly the chum roll, it is only saved locally and not on the server, 
meaning the friends list doesn't go well between computers, so mine will have a server side friend list. 
* Fourth, the ability to use other servers. The current Pesterchum does have this feature, but it is not simple to find.

Future
------
When the above goals have been met and it contains all the features possible from
MSPaintAdventures Pesterchum, then we would like to develop an Android version.

Automatic Building
------------------
Automatic builds happen whenever a change is pushed to the repo. The server and client automatically include all jars in the lib folder and all class files.
However other files such as xml and graphics will need to be added manually in the ant build files, as has been done for the client.

Downloads can be found at http://jenkins.hyperbadger.it.cx (reliability not guaranteed)


