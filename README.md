# Location-Tracker
Android application which tracks device's location (one-time or periodically) using background service and partial wake lock, and saves it into a Realm database

## Device requirements
- Android 4.4+
- Location Feature

## Technical information
- Minimum SDK Version: 19
- Target SDK Version: 25
- Realm Mobile Database Version: 2.3.0
- IDE used for development: Android Studio

---

Supported location providers: Network, GPS, Passive

---

You are able to request one-time location update or periodically location updates.

---

You need to request one-time location update or periodically location updates after the "LocationService" is successfully bound in "onServiceConnected(ComponentName name, IBinder service)" overridden method of the "ServiceConnection" interface if the required application permissions are granted (see "MainActivity.class).

```java
private ServiceConnection mLocationServiceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        LogHelper.debugLog("\"" + name.getClassName() + "\" onServiceConnected");

        LocationService.LocalBinder localBinder = (LocationService.LocalBinder) service;

        LocationService locationService = localBinder.getService();

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Request one-time location update or periodically location updates
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        LogHelper.debugLog("\"" + name.getClassName() + "\" onServiceDisconnected");
    }
};
```

---

Here is an example for requesting one-time location update with specified provider:

- Provider: Network

```java
locationService.requestLocationUpdate(LocationManager.NETWORK_PROVIDER);
```

---

Here is an example for requesting periodically location updates with specified provider and time and distance between updates:

- Provider: GPS
- Minimum time interval between location updates, in milliseconds: 60000 (60 seconds, 1 minute)
- Minimum distance between location updates, in meters: 2

```java
locationService.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 20);
```

---


Here is an example for requesting one-time location update with criteria best provider:

- Criteria: High accuracy
- Provider: true; "true" for currently enabled location provider, "false" for not. When using "true" as a value, make sure that the device location is enabled.

```java
Criteria criteria = new Criteria();
criteria.setAccuracy(Criteria.ACCURACY_HIGH);

locationService.requestLocationUpdate(criteria, true);
```

---

Here is an example for requesting periodically location updates with criteria best provider:

- Criteria: High accuracy
- Provider: true; "true" for currently enabled location provider, "false" for not. When using "true" as a value, make sure that the device location is enabled.
- Minimum time interval between location updates, in milliseconds: 60000 (60 seconds, 1 minute)
- Minimum distance between location updates, in meters: 2

```java
Criteria criteria = new Criteria();
criteria.setAccuracy(Criteria.ACCURACY_HIGH);

locationService.requestLocationUpdates(criteria, true, 6000, 2);
```

---

To stop requesting periodically location updates simply stop the service or call the "LocationService" "removeLocationUpdates()" method.

### Manifest required permissions
```xml
<uses-permission android:name="android.permission.WAKE_LOCK" />

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### License
Android-Location-Tracker is released under the The GNU General Public License v3.0. See "LICENSE" for more information.
