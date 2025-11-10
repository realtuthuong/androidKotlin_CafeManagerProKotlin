# ⚠️ HÀNH ĐỘNG CẦN THIẾT TRƯỚC KHI PUSH LÊN GITHUB

## 🚨 QUAN TRỌNG: Bạn PHẢI làm các bước sau

### 1. Thay thế API Keys trong Constant.kt

Mở file `app/src/main/java/com/pro/shopfee/utils/Constant.kt` và thay thế:

```kotlin
// Thay dòng 70:
const val GOOGLE_MAPS_API_KEY = "YOUR_PLACEHOLDER_KEY" // hoặc ""

// Thay dòng 60-61:
const val VNPAY_TMN_CODE = "YOUR_PLACEHOLDER"
const val VNPAY_HASH_SECRET = "YOUR_PLACEHOLDER"

// Thay dòng 45:
const val BLOCKCHAIN_RPC_BASE_URL = "https://eth-sepolia.g.alchemy.com/v2/YOUR_PLACEHOLDER/"
```

### 2. Kiểm tra google-services.json không được track

```bash
# Kiểm tra
git ls-files | grep google-services.json

# Nếu thấy, xóa khỏi git (KHÔNG xóa file thật)
git rm --cached app/google-services.json
```

### 3. Chạy script kiểm tra

```bash
bash check-before-commit.sh
```

### 4. Commit các file mới

```bash
git add .gitignore
git add README.md
git add SETUP.md
git add SECURITY.md
git add PRE_COMMIT_CHECKLIST.md
git add .gitattributes
git add app/google-services.json.example
git add app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt.example
git add check-before-commit.sh
```

### 5. Commit code đã sửa

```bash
git add .
git commit -m "Refactor to MVVM architecture and add security configurations"
```

### 6. Push lên GitHub

```bash
git push origin main
# hoặc
git push origin master
```

## ✅ Sau khi push

1. Test clone lại từ GitHub
2. Đảm bảo có thể setup được theo hướng dẫn trong SETUP.md
3. Kiểm tra không có API keys thật trong code trên GitHub

---

**LƯU Ý**: Nếu bạn đã commit nhầm API keys thật, xem hướng dẫn trong `SECURITY.md`

