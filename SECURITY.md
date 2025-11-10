# Security Policy

## 🔐 Bảo mật API Keys

Dự án này sử dụng nhiều API keys và secrets. **TUYỆT ĐỐI KHÔNG** commit các keys thật lên GitHub.

## ⚠️ Files chứa thông tin nhạy cảm

### 1. `app/google-services.json`
- **Chứa**: Firebase API keys, project ID
- **Xử lý**: Đã thêm vào `.gitignore`
- **Template**: `app/google-services.json.example`

### 2. `app/src/main/java/com/pro/shopfee/utils/Constant.kt`
- **Chứa**: 
  - Google Maps API Key
  - VNPay TMN Code và Hash Secret
  - Blockchain RPC URL với API key
- **Xử lý**: 
  - Thêm comment cảnh báo
  - Tạo `ApiKeys.kt.example` làm template
  - **CẦN THAY THẾ** các keys thật trước khi commit

### 3. `app/src/main/java/com/pro/shopfee/MyApplication.kt`
- **Chứa**: Firebase Database URL
- **Xử lý**: Có thể giữ nguyên (public URL) hoặc move sang config

## ✅ Checklist trước khi commit

- [ ] Đã thay thế Google Maps API Key bằng placeholder
- [ ] Đã thay thế VNPay credentials bằng placeholder
- [ ] Đã thay thế Blockchain RPC URL/API key bằng placeholder
- [ ] Đã kiểm tra `google-services.json` không được commit
- [ ] Đã tạo file `.example` cho các file nhạy cảm
- [ ] Đã cập nhật README với hướng dẫn setup

## 🔍 Kiểm tra trước khi push

```bash
# Kiểm tra không có API keys thật
grep -r "AIzaSy[A-Za-z0-9_-]" app/src/ --exclude-dir=build
grep -r "KFXCQ9O7TVXNMMAXULFHROLQJDTT1ZNQ" app/src/ --exclude-dir=build
grep -r "R4y1vJ3E5sffi90cr_hrMBlQNV3EaUip" app/src/ --exclude-dir=build

# Kiểm tra google-services.json không được track
git ls-files | grep google-services.json

# Nếu tìm thấy, xóa khỏi git:
# git rm --cached app/google-services.json
```

## 🛡️ Best Practices

1. **Sử dụng BuildConfig** cho production:
   ```kotlin
   // build.gradle
   buildTypes {
       release {
           buildConfigField "String", "GOOGLE_MAPS_API_KEY", "\"${project.findProperty("GOOGLE_MAPS_API_KEY")}\""
       }
   }
   ```

2. **Sử dụng environment variables** trong CI/CD

3. **Sử dụng Android Keystore** cho signing keys

4. **Không hardcode** secrets trong code

## 📝 Nếu đã commit nhầm keys

1. **Ngay lập tức**:
   - Revoke keys đã commit trên các service providers
   - Tạo keys mới
   - Xóa keys cũ khỏi git history (nếu cần)

2. **Xóa khỏi git history**:
   ```bash
   git filter-branch --force --index-filter \
     "git rm --cached --ignore-unmatch app/google-services.json" \
     --prune-empty --tag-name-filter cat -- --all
   ```

3. **Force push** (cẩn thận!):
   ```bash
   git push origin --force --all
   ```

## 🔄 Sau khi clone

1. Copy các file `.example` và điền thông tin
2. Không commit các file thật
3. Thêm vào `.gitignore` nếu chưa có

