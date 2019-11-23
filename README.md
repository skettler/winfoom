# winfoom
### Basic Proxy Facade for NTLM and Kerberos

Winfoom is a HTTP(s) proxy server facade that allows applications to authenticate through a NTML/Kerberos authenticated proxy server, typically used in corporate environments, 
without having to deal with the actual handshake.

A lot of software applications have problems when dealing with an authenticated proxy server's protocol. Winfoom sits between the corporate proxy and applications and offloads the authentication and the proxy's protocol, acting as a facade. This way, the software application will only have to deal with a basic proxy with no authentication.

An example of such facade for NTLM proxies is [CNTLM](http://cntlm.sourceforge.net/)

### Instalation

Winfoom is a Java application and comes with a release that includes a Java environment [AdoptOpenJDK](https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.5%2B10/OpenJDK11U-jdk_x64_windows_hotspot_11.0.5_10.msi),
so you don't have to install anything.

The installation is pretty basic, just unzip the content of the released archive then, double click on `launch.bat` file.

### Configuration


To test it, open a browser, let's say Firefox and configure proxy like this:

![firefox](https://docs.google.com/uc?export=download&id=1T18McN2oy4NPrIMtwS9CHlsYXz4KJi7T)

Now you should be able to access any URL without Firefox asking for credentials.


### TODO

   - ~~Get the domain automaticaly.~~
   - Debug mode.

### Feedback

Any feedback or suggestions are welcome. 
It is hosted with an Apache 2.0 license so issues, forks and PRs are most appreciated.

