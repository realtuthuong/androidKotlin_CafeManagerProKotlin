# Hướng dẫn khởi tạo Git và push lên GitHub

## 📋 Các bước thực hiện

### 1. Khởi tạo Git repository (nếu chưa có)

```bash
cd C:\Users\LENOVO\Desktop\CafeManagerProKotlin
git init
```

### 2. Thay thế API Keys trong Constant.kt

**QUAN TRỌNG**: Trước khi commit, bạn PHẢI thay thế các API keys thật:

Mở `app/src/main/java/com/pro/shopfee/utils/Constant.kt` và thay:

- Dòng 70: `GOOGLE_MAPS_API_KEY` → `"YOUR_PLACEHOLDER_KEY"`
- Dòng 60-61: `VNPAY_TMN_CODE` và `VNPAY_HASH_SECRET` → `"YOUR_PLACEHOLDER"`
- Dòng 45: `BLOCKCHAIN_RPC_BASE_URL` → thay API key bằng placeholder

### 3. Kiểm tra google-services.json

```bash
# Nếu file đã được track, xóa khỏi git
git rm --cached app/google-services.json
```

### 4. Thêm các file vào git

```bash
# Thêm tất cả files (trừ những file trong .gitignore)
git add .

# Kiểm tra files sẽ được commit
git status
```

### 5. Commit lần đầu

```bash
git commit -m "Initial commit: CafeManagerPro with MVVM architecture

- Refactored to MVVM pattern
- Added Repository layer
- Added ViewModels for all Activities and Fragments
- Added Hilt dependency injection
- Added security configurations and documentation"
```

### 6. Tạo repository trên GitHub

1. Đăng nhập GitHub
2. Click "New repository"
3. Đặt tên: `CafeManagerProKotlin`
4. **KHÔNG** tạo README, .gitignore, license (đã có sẵn)
5. Click "Create repository"

### 7. Kết nối và push

```bash
# Thêm remote (thay YOUR_USERNAME bằng username GitHub của bạn)
git remote add origin https://github.com/YOUR_USERNAME/CafeManagerProKotlin.git

# Đổi tên branch thành main (nếu cần)
git branch -M main

# Push lên GitHub
git push -u origin main
```

### 8. Kiểm tra trên GitHub

1. Vào repository trên GitHub
2. Kiểm tra:
   - ✅ Không có `google-services.json` thật
   - ✅ Không có API keys thật trong code
   - ✅ Có file `.example` templates
   - ✅ Có README.md, SETUP.md, SECURITY.md

## ⚠️ Nếu đã commit nhầm API keys

Xem hướng dẫn trong `SECURITY.md` để:
1. Revoke keys
2. Xóa khỏi git history
3. Commit lại

## 📝 Commit message mẫu

```bash
git commit -m "Refactor to MVVM architecture

- Implemented MVVM pattern with ViewModels and Repositories
- Added Hilt for dependency injection
- Separated data layer from UI layer
- Added LiveData/StateFlow for reactive programming
- Added security configurations and documentation
- Added setup guides and checklists"
```

## 🔄 Các lệnh Git hữu ích

```bash
# Xem files đã thay đổi
git status

# Xem diff trước khi commit
git diff

# Xem files sẽ được commit
git diff --cached

# Xem lịch sử commit
git log --oneline

# Undo last commit (giữ thay đổi)
git reset --soft HEAD~1

# Xóa file khỏi git nhưng giữ file thật
git rm --cached <file>
```

