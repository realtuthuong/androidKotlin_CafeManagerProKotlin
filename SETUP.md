# Hướng dẫn Setup dự án

## ⚠️ QUAN TRỌNG: Cấu hình API Keys trước khi chạy

Dự án này sử dụng nhiều API keys và secrets. Bạn **PHẢI** cấu hình chúng trước khi chạy app.

## 📋 Checklist trước khi commit lên GitHub

- [ ] Đã thêm `app/google-services.json` vào `.gitignore`
- [ ] Đã tạo file `ApiKeys.kt` từ template và KHÔNG commit
- [ ] Đã xóa/ẩn các API keys thật trong `Constant.kt`
- [ ] Đã kiểm tra không có secrets trong code
- [ ] Đã tạo file `.example` cho các file nhạy cảm

## 🔧 Các bước setup

### 1. Firebase Setup

1. Tạo project trên Firebase Console
2. Download `google-services.json`
3. Copy vào `app/` folder
4. **KHÔNG commit file này**

### 2. API Keys Setup

#### Option 1: Sử dụng ApiKeys.kt (Khuyến nghị)

1. Copy template:
   ```bash
   cp app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt.example \
      app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt
   ```

2. Điền các keys thật vào `ApiKeys.kt`

3. Cập nhật `Constant.kt`:
   ```kotlin
   const val GOOGLE_MAPS_API_KEY = ApiKeys.GOOGLE_MAPS_API_KEY
   const val VNPAY_TMN_CODE = ApiKeys.VNPAY_TMN_CODE
   const val VNPAY_HASH_SECRET = ApiKeys.VNPAY_HASH_SECRET
   const val BLOCKCHAIN_RPC_BASE_URL = ApiKeys.BLOCKCHAIN_RPC_BASE_URL
   ```

#### Option 2: Giữ trong Constant.kt (Không khuyến nghị cho production)

Nếu giữ keys trong `Constant.kt`, **PHẢI**:
- Thay thế bằng placeholder trước khi commit
- Thêm comment cảnh báo
- Không commit keys thật

### 3. Các API Keys cần thiết

1. **Google Maps API Key**
   - Lấy từ [Google Cloud Console](https://console.cloud.google.com/)
   - Enable: Distance Matrix API

2. **VNPay Credentials**
   - TMN Code: Từ VNPay merchant account
   - Hash Secret: Từ VNPay merchant account

3. **Blockchain RPC URL** (nếu dùng)
   - Alchemy API Key hoặc Infura
   - Format: `https://eth-sepolia.g.alchemy.com/v2/YOUR_KEY/`

4. **Firebase**
   - Tự động từ `google-services.json`

## 🚫 Files KHÔNG được commit

Các file sau đã được thêm vào `.gitignore`:

```
app/google-services.json
app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt
local.properties
*.keystore
*.jks
```

## ✅ Kiểm tra trước khi push

```bash
# Kiểm tra files sẽ được commit
git status

# Kiểm tra không có API keys trong code
grep -r "AIzaSy" app/src/ --exclude-dir=build
grep -r "KFXCQ9O7TVXNMMAXULFHROLQJDTT1ZNQ" app/src/ --exclude-dir=build

# Nếu tìm thấy keys thật, thay thế bằng placeholder
```

## 🔄 Sau khi clone từ GitHub

1. Copy `google-services.json.example` → `google-services.json` và điền thông tin
2. Copy `ApiKeys.kt.example` → `ApiKeys.kt` và điền keys
3. Build và chạy

