# Hướng Dẫn Tích Hợp FCM Notification - Step by Step

Hướng dẫn này dựa trên implementation thực tế của project `Test_Noti`. Follow từng bước để tích hợp FCM notification thành công.

---

## Bước 1: Setup Firebase Console

### 1.1. Tạo Firebase Project
1. Vào [Firebase Console](https://console.firebase.google.com/)
2. Tạo project mới hoặc chọn project có sẵn
3. Thêm Android app với package name: `com.anhtu.test_noti`

### 1.2. Tải file `google-services.json`
1. Trong Firebase Console → Project Settings → Your apps
2. Download file `google-services.json`
3. Đặt file vào thư mục: `app/google-services.json`

**File structure:**
```
Test_Noti/
  └── app/
      └── google-services.json  ← Đặt file ở đây
```

---

## Bước 2: Cấu Hình Dependencies

### 2.1. File `build.gradle.kts` (Project level)

**Location:** `build.gradle.kts` (root)

```kotlin
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
}
```

**Giải thích:** Thêm Google Services plugin vào project level.

---

### 2.2. File `gradle/libs.versions.toml`

**Location:** `gradle/libs.versions.toml`

```toml
[versions]
agp = "8.13.0"
junit = "4.13.2"
junitVersion = "1.3.0"
espressoCore = "3.7.0"
appcompat = "1.7.1"
material = "1.13.0"
activity = "1.11.0"
constraintlayout = "2.2.1"
firebase-bom = "33.7.0"
playServicesTasks = "18.4.0"

[libraries]
junit = { group = "junit", name = "junit", version.ref = "junit" }
ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
activity = { group = "androidx.activity", name = "activity", version.ref = "activity" }
constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebase-bom" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }
play-services-tasks = { group = "com.google.android.gms", name = "play-services-tasks", version.ref = "playServicesTasks" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
```

**Giải thích:** 
- Thêm `firebase-bom = "33.7.0"` vào `[versions]`
- Thêm `playServicesTasks = "18.4.0"` vào `[versions]`
- Thêm Firebase libraries vào `[libraries]`

---

### 2.3. File `app/build.gradle.kts`

**Location:** `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")  // ← Thêm dòng này
}

android {
    namespace = "com.anhtu.test_noti"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.anhtu.test_noti"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    
    // Firebase ← Thêm phần này
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.play.services.tasks)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
```

**Giải thích:**
- Thêm plugin `id("com.google.gms.google-services")` ở đầu file
- Thêm Firebase dependencies vào `dependencies` block

**Sau khi thêm:** Sync Gradle project (File → Sync Project with Gradle Files)

---

## Bước 3: Cấu Hình AndroidManifest.xml

### 3.1. Thêm Permissions

**Location:** `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Test_Noti">
        
        <!-- MainActivity -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Firebase Cloud Messaging Service -->
        <service
            android:name=".MyFirebaseMessagingService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
    </application>

</manifest>
```

**Giải thích:**
- `INTERNET`: Cần để kết nối với Firebase
- `POST_NOTIFICATIONS`: Cần cho Android 13+ (API 33+) để hiển thị notification
- `MyFirebaseMessagingService`: Service xử lý notification từ FCM

---

## Bước 4: Tạo MainActivity

### 4.1. Code MainActivity.java

**Location:** `app/src/main/java/com/anhtu/test_noti/MainActivity.java`

```java
package com.anhtu.test_noti;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup permission launcher
        setupPermissionLauncher();

        // Request notification permission cho Android 13+ (API 33+)
        requestNotificationPermission();

        // Lấy FCM Token và log ra console
        getFCMToken();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Notification permission granted");
                    } else {
                        Log.w(TAG, "Notification permission denied");
                    }
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting notification permission");
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                Log.d(TAG, "Notification permission already granted");
            }
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Lấy token thành công
                        String fmcToken = task.getResult();
                        
                        // Log token ra console (Logcat)
                        Log.d(TAG, "FCM Token: " + fmcToken);
                        Log.i(TAG, "============================================");
                        Log.i(TAG, "FCM TOKEN (copy token này để test từ BE):");
                        Log.i(TAG, fmcToken);
                        Log.i(TAG, "============================================");
                    }
                });
    }
}
```

**Giải thích:**
- `requestNotificationPermission()`: Request permission cho Android 13+
- `getFCMToken()`: Lấy FCM token và log ra Logcat
- Token sẽ hiển thị trong Logcat với tag `MainActivity`

---

## Bước 5: Tạo MyFirebaseMessagingService

### 5.1. Code MyFirebaseMessagingService.java

**Location:** `app/src/main/java/com/anhtu/test_noti/MyFirebaseMessagingService.java`

```java
package com.anhtu.test_noti;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "default_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        
        Log.i(TAG, "============================================");
        Log.i(TAG, "NOTIFICATION RECEIVED!");
        Log.i(TAG, "From: " + remoteMessage.getFrom());
        Log.i(TAG, "Message ID: " + remoteMessage.getMessageId());
        Log.i(TAG, "Message Type: " + remoteMessage.getMessageType());
        Log.i(TAG, "============================================");

        // Kiểm tra nếu message có data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload size: " + remoteMessage.getData().size());
            for (java.util.Map.Entry<String, String> entry : remoteMessage.getData().entrySet()) {
                Log.d(TAG, "Data - " + entry.getKey() + ": " + entry.getValue());
            }
        } else {
            Log.d(TAG, "No data payload");
        }

        // Kiểm tra nếu message có notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Has notification payload");
            Log.d(TAG, "Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            
            // Hiển thị notification
            sendNotification(title, body, remoteMessage.getData());
        } else {
            Log.d(TAG, "No notification payload, using data payload");
            // Nếu không có notification payload, lấy từ data
            String title = remoteMessage.getData().get("title");
            String body = remoteMessage.getData().get("body");
            String message = remoteMessage.getData().get("message");
            
            if (title == null || title.isEmpty()) {
                title = "New Notification";
            }
            if (body == null || body.isEmpty()) {
                body = message != null ? message : "You have a new message";
            }
            
            Log.d(TAG, "Extracted Title: " + title + ", Body: " + body);
            sendNotification(title, body, remoteMessage.getData());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
        Log.i(TAG, "============================================");
        Log.i(TAG, "NEW FCM TOKEN (nếu token thay đổi):");
        Log.i(TAG, token);
        Log.i(TAG, "============================================");
        
        // Gửi token lên server nếu cần
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer: " + token);
    }

    private void sendNotification(String title, String messageBody, java.util.Map<String, String> data) {
        try {
            Log.i(TAG, "============================================");
            Log.i(TAG, "Creating notification...");
            Log.i(TAG, "Title: " + title);
            Log.i(TAG, "Body: " + messageBody);
            Log.i(TAG, "============================================");
            
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            // Thêm data vào intent nếu cần
            if (data != null && !data.isEmpty()) {
                for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
                    intent.putExtra(entry.getKey(), entry.getValue());
                }
            }
            
            PendingIntent pendingIntent = PendingIntent.getActivity(
                    this, 0, intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

            // Tạo notification channel cho Android Oreo trở lên
            createNotificationChannel();

            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(android.R.drawable.ic_dialog_info)
                            .setContentTitle(title != null ? title : "New Notification")
                            .setContentText(messageBody != null ? messageBody : "You have a new message")
                            .setAutoCancel(true)
                            .setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI)
                            .setContentIntent(pendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                int notificationId = (int) System.currentTimeMillis();
                notificationManager.notify(notificationId, notificationBuilder.build());
                Log.i(TAG, "============================================");
                Log.i(TAG, "Notification displayed successfully!");
                Log.i(TAG, "Notification ID: " + notificationId);
                Log.i(TAG, "============================================");
            } else {
                Log.e(TAG, "NotificationManager is null!");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying notification", e);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                // Kiểm tra xem channel đã tồn tại chưa
                if (notificationManager.getNotificationChannel(CHANNEL_ID) == null) {
                    NotificationChannel channel = new NotificationChannel(
                            CHANNEL_ID,
                            "Default Channel",
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("Default notification channel");
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    channel.setShowBadge(true);
                    // Sound sẽ tự động được bật khi importance là HIGH
                    // Sử dụng sound mặc định của hệ thống (không cần setSound riêng)

                    notificationManager.createNotificationChannel(channel);
                    Log.d(TAG, "Notification channel created: " + CHANNEL_ID);
                } else {
                    Log.d(TAG, "Notification channel already exists: " + CHANNEL_ID);
                }
            }
        }
    }
}
```

**Giải thích:**
- `onMessageReceived()`: Xử lý notification khi nhận được từ FCM
- `onNewToken()`: Xử lý khi FCM token thay đổi
- `sendNotification()`: Hiển thị notification trên device
- `createNotificationChannel()`: Tạo notification channel (bắt buộc cho Android 8.0+)

---

## Bước 6: Test & Verify

### 6.1. Lấy FCM Token

1. **Build & Run app** trên device/emulator
2. **Mở Logcat** trong Android Studio
3. **Filter bằng tag:** `MainActivity`
4. **Tìm dòng:** `FCM TOKEN (copy token này để test từ BE):`
5. **Copy token** để sử dụng cho backend

**Ví dụ log:**
```
I/MainActivity: ============================================
I/MainActivity: FCM TOKEN (copy token này để test từ BE):
I/MainActivity: dAbCdEfGhIjKlMnOpQrStUvWxYz123456789
I/MainActivity: ============================================
```

### 6.2. Gửi Test Notification từ Backend

**Format JSON để gửi từ backend:**

```json
{
  "token": "FCM_TOKEN_TỪ_APP",
  "notification": {
    "title": "Test Notification",
    "body": "Hello from backend!"
  },
  "data": {
    "key1": "value1",
    "key2": "value2"
  }
}
```

**Hoặc chỉ dùng data payload:**

```json
{
  "token": "FCM_TOKEN_TỪ_APP",
  "data": {
    "title": "Tiêu đề",
    "body": "Nội dung",
    "message": "Nội dung thay thế"
  }
}
```

### 6.3. Kiểm Tra Logs

**Khi nhận được notification:**

1. **Mở Logcat** với filter: `FCMService`
2. **Kiểm tra logs:**
   - `NOTIFICATION RECEIVED!`
   - `Creating notification...`
   - `Notification displayed successfully!`

### 6.4. Kiểm Tra Notification

- **App ở FOREGROUND**: Notification sẽ hiển thị ngay
- **App ở BACKGROUND**: Notification sẽ hiển thị trên notification bar
- **Click vào notification**: App sẽ mở và chuyển đến MainActivity

---

## Checklist

Sử dụng checklist này để đảm bảo đã hoàn thành tất cả các bước:

- [ ] File `google-services.json` đã đặt trong `app/`
- [ ] Google Services plugin đã thêm vào `build.gradle.kts` (project level)
- [ ] Firebase dependencies đã thêm vào `gradle/libs.versions.toml`
- [ ] Firebase dependencies đã thêm vào `app/build.gradle.kts`
- [ ] Đã sync Gradle project
- [ ] Permissions đã thêm vào `AndroidManifest.xml`
- [ ] Service đã đăng ký trong `AndroidManifest.xml`
- [ ] MainActivity đã có code request permission và lấy token
- [ ] MyFirebaseMessagingService đã được tạo
- [ ] App đã build và chạy thành công
- [ ] FCM token đã hiển thị trong Logcat
- [ ] Đã test gửi notification từ backend
- [ ] Notification đã hiển thị trên device

---

## Troubleshooting

### Token không hiển thị?
- Kiểm tra `google-services.json` có đúng package name không
- Kiểm tra internet connection
- Kiểm tra logs trong Logcat với tag `MainActivity`

### Notification không hiển thị?
- Kiểm tra permission đã được grant chưa (Android 13+)
- Kiểm tra app có đang chạy không (foreground/background)
- Kiểm tra logs trong Logcat với tag `FCMService`
- Kiểm tra backend có gửi đúng format không

### App crash khi chạy?
- Kiểm tra đã sync Gradle chưa
- Kiểm tra dependencies có đúng không
- Kiểm tra `google-services.json` có đúng vị trí không
- Xem logcat để biết lỗi cụ thể

---

## Lưu Ý Quan Trọng

1. **Token có thể thay đổi**: Khi cài đặt lại app, clear data, hoặc cài trên device khác
2. **Android 13+**: Cần request `POST_NOTIFICATIONS` permission (đã có trong code)
3. **Notification Channel**: Bắt buộc cho Android 8.0+ (Oreo) - đã tự động tạo trong code
4. **Foreground vs Background**:
   - **Foreground**: `onMessageReceived()` được gọi → app tự hiển thị notification
   - **Background với notification payload**: Android tự hiển thị, `onMessageReceived()` KHÔNG được gọi
   - **Background với data payload only**: `onMessageReceived()` được gọi → app tự hiển thị

---

## Kết Luận

Sau khi hoàn thành tất cả các bước trên, bạn đã tích hợp thành công FCM notification vào Android app. Notification sẽ hiển thị khi backend gửi thông báo đến device.

**Next Steps:**
- Gửi token lên backend để lưu trữ
- Customize notification icon, sound, vibration
- Xử lý notification click action
- Thêm deep linking nếu cần

---

**Chúc bạn thành công! 🎉**

