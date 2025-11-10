# Hướng Dẫn Debug FCM Notification

## Các Bước Kiểm Tra

### 1. Kiểm tra FCM Token đã được gửi lên server chưa

**Trong Logcat, tìm các tag sau:**
- `VerifyActivity`: Tìm log "Device FCM Token received"
- `FcmTokenHelper`: Tìm log "SUCCESS: FCM token sent to server successfully"
- `MainActivity`: Tìm log "Device FCM Token" và "FCM token successfully sent to server"

**Nếu thấy lỗi:**
- `Access token is empty`: User chưa đăng nhập
- `Failed to send FCM token to server`: Kiểm tra network và server response

### 2. Kiểm tra Token trên Server

**API:** `GET /users/me`
**Response:** Kiểm tra field `fcmToken` trong response

**So sánh token:**
- Token trong Logcat (Device FCM Token)
- Token từ API `/users/me` (User Info FCM Token)
- Token mà backend đang dùng để gửi notification

### 3. Kiểm tra Notification được nhận

**Trong Logcat, tìm tag `FCMService`:**
- `NOTIFICATION RECEIVED in onMessageReceived!`: App đã nhận notification
- `Notification displayed successfully!`: Notification đã được hiển thị

**Lưu ý quan trọng:**
- Nếu app ở **foreground** và backend gửi notification với **notification payload**, hệ thống sẽ tự động hiển thị và **KHÔNG gọi** `onMessageReceived` - đây là hành vi bình thường của FCM
- `onMessageReceived` chỉ được gọi khi:
  1. App ở foreground VÀ message là data-only (không có notification payload)
  2. App ở foreground VÀ có notification payload (tùy cấu hình Firebase)
  3. App ở background VÀ message là data-only

### 4. Cách Backend nên gửi Notification

**Option 1: Data-only message (Recommended)**
```json
{
  "to": "FCM_TOKEN",
  "data": {
    "title": "Notification Title",
    "body": "Notification Body",
    "message": "Additional message"
  }
}
```
- Luôn gọi `onMessageReceived` khi app ở foreground
- App tự xử lý và hiển thị notification

**Option 2: Notification payload**
```json
{
  "to": "FCM_TOKEN",
  "notification": {
    "title": "Notification Title",
    "body": "Notification Body"
  },
  "data": {
    "key": "value"
  }
}
```
- Khi app ở background: Hệ thống tự hiển thị, KHÔNG gọi `onMessageReceived`
- Khi app ở foreground: Tùy cấu hình, có thể gọi hoặc không gọi `onMessageReceived`

### 5. Test Notification từ Firebase Console

1. Vào Firebase Console > Cloud Messaging
2. Tạo test message
3. Chọn "Send test message"
4. Nhập FCM token từ Logcat
5. Gửi và kiểm tra Logcat

### 6. Kiểm tra Permissions

**Android 13+ (API 33+):**
- Cần permission `POST_NOTIFICATIONS`
- App đã tự động request permission trong `MainActivity.askNotificationPermission()`

**Kiểm tra:**
- Settings > Apps > FTask > Notifications
- Đảm bảo notifications được bật

### 7. Common Issues

**Issue: Token không được gửi lên server**
- Kiểm tra accessToken có tồn tại không
- Kiểm tra network connection
- Kiểm tra server response trong Logcat

**Issue: Notification không hiển thị khi app ở background**
- Kiểm tra notification channel đã được tạo chưa
- Kiểm tra notification permission
- Kiểm tra notification settings trong device settings

**Issue: onMessageReceived không được gọi**
- Nếu app ở background và backend gửi notification payload: Đây là hành vi bình thường
- Backend nên gửi data-only message để luôn gọi `onMessageReceived`

### 8. Log Tags để Filter trong Logcat

```
VerifyActivity
FcmTokenHelper
FCMService
MainActivity
```

### 9. Kiểm tra Firebase Configuration

- File `google-services.json` đã có trong `app/` directory
- Package name trong `google-services.json` khớp với `applicationId` trong `build.gradle.kts`
- Firebase project đã enable Cloud Messaging API

### 10. Test với cURL

```bash
curl -X POST https://fcm.googleapis.com/fcm/send \
  -H "Authorization: key=YOUR_SERVER_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "to": "FCM_TOKEN_FROM_LOGCAT",
    "data": {
      "title": "Test Title",
      "body": "Test Body",
      "message": "Test Message"
    }
  }'
```

Replace:
- `YOUR_SERVER_KEY`: Firebase Server Key từ Firebase Console
- `FCM_TOKEN_FROM_LOGCAT`: Token từ Logcat

