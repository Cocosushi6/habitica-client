Habitica client is a little java client for habitica that uses Apache's HttpClient lib.
Its features : 
- Validate todos, habits and dailies
- Create todo, habit or daily
- Minimize in system tray (works well in Linux, I haven't tested for Windows and Mac OS X but I plan to to it soon).
- Uses the API v3
- That's all for the moment ! it is just a little project and I don't plan to add lots of features.
- Feel free to add features yourself !

Note : The JXTrayIcon class is not from me, it has been written by Alexander Potochkin to allow using themeable Swing components in system tray (https://svn.java.net/svn/swinghelper~svn/trunk/src/java/org/jdesktop/swinghelper/tray/).

To use the client, create a file 'credentials.txt' in the same directory than the client and put your API token, then your user ID (they can both be found on habitica.com, in the 'API' section of the settings).
