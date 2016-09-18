rem adb forward tcp:23946 tcp:23946
adb shell am start -D -n com.test.shellapp/com.test.shellapp.MainActivity
set/p option=������˿ں�:
jdb -connect com.sun.jdi.SocketAttach:hostname=127.0.0.1,port=%option%