# Agrega al proyecto Android el código de las alarmas nativas y los permisos (lo corre GitHub al compilar)
import os, shutil, re
src = 'android-extra'
dst = 'android/app/src/main/java/com/alexis/ruedayquincena'
os.makedirs(dst, exist_ok=True)
for f in os.listdir(src):
    if f.endswith('.java'):
        shutil.copy(os.path.join(src, f), os.path.join(dst, f))
os.makedirs('android/app/src/main/res/raw', exist_ok=True)
shutil.copy('alarma.wav', 'android/app/src/main/res/raw/alarma.wav')

m = 'android/app/src/main/AndroidManifest.xml'
s = open(m, encoding='utf-8').read()
perms = ['POST_NOTIFICATIONS', 'SCHEDULE_EXACT_ALARM', 'USE_EXACT_ALARM', 'RECEIVE_BOOT_COMPLETED', 'WAKE_LOCK',
         'VIBRATE', 'FOREGROUND_SERVICE', 'FOREGROUND_SERVICE_MEDIA_PLAYBACK', 'USE_FULL_SCREEN_INTENT']
extra = ''.join(f'\n    <uses-permission android:name="android.permission.{p}" />' for p in perms
                if f'android.permission.{p}"' not in s)
s = s.replace('<uses-permission android:name="android.permission.INTERNET" />',
              '<uses-permission android:name="android.permission.INTERNET" />' + extra, 1)
comp = '''
        <activity android:name=".AlarmaActivity" android:exported="false" android:showWhenLocked="true"
            android:turnScreenOn="true" android:excludeFromRecents="true" android:launchMode="singleInstance"
            android:theme="@android:style/Theme.DeviceDefault.NoActionBar.Fullscreen" />
        <receiver android:name=".AlarmaReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
            </intent-filter>
        </receiver>
        <receiver android:name=".AlarmaCorte" android:exported="false" />
        <service android:name=".AlarmaService" android:exported="false" android:foregroundServiceType="mediaPlayback" />
    </application>'''
s = s.replace('</application>', comp, 1)
open(m, 'w', encoding='utf-8').write(s)
print(s)

# Firma fija: siempre la misma clave, así cada versión nueva se instala encima de la anterior
import base64
open('android/firma.keystore', 'wb').write(base64.b64decode(open('firma.txt').read()))
g = 'android/app/build.gradle'
gs = open(g, encoding='utf-8').read()
if 'firma.keystore' not in gs:
    gs = gs.replace('android {', '''android {
    signingConfigs {
        debug {
            storeFile file("${rootDir}/firma.keystore")
            storePassword "android"
            keyAlias "androiddebugkey"
            keyPassword "android"
        }
    }''', 1)
    open(g, 'w', encoding='utf-8').write(gs)
print('firma fija configurada')
