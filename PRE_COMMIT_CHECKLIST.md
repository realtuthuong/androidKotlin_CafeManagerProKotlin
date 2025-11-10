# ✅ Checklist trước khi commit lên GitHub

## 🔐 Bảo mật (QUAN TRỌNG NHẤT)

- [ ] **Đã thay thế tất cả API keys thật bằng placeholder** trong `Constant.kt`
  - [ ] Google Maps API Key
  - [ ] VNPay TMN Code và Hash Secret  
  - [ ] Blockchain RPC URL/API Key

- [ ] **Đã kiểm tra `google-services.json` không được commit**
  ```bash
  git status | grep google-services.json
  # Nếu thấy, chạy: git rm --cached app/google-services.json
  ```

- [ ] **Đã kiểm tra `ApiKeys.kt` không được commit** (nếu có)
  ```bash
  git status | grep ApiKeys.kt
  # Nếu thấy, chạy: git rm --cached app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt
  ```

- [ ] **Đã tạo file template**:
  - [ ] `app/google-services.json.example`
  - [ ] `app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt.example`

## 📁 Files và cấu trúc

- [ ] **Đã cập nhật `.gitignore`** với:
  - [ ] `app/google-services.json`
  - [ ] `app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt`
  - [ ] `local.properties`
  - [ ] Build folders
  - [ ] IDE files

- [ ] **Đã tạo documentation**:
  - [ ] `README.md` - Tổng quan dự án
  - [ ] `SETUP.md` - Hướng dẫn setup
  - [ ] `SECURITY.md` - Chính sách bảo mật

## 🧪 Code quality

- [ ] **Đã test app chạy được** sau khi thay thế API keys
- [ ] **Không có lỗi compile**
- [ ] **Đã xóa code debug/test không cần thiết**

## 📝 Git

- [ ] **Đã kiểm tra files sẽ commit**:
  ```bash
  git status
  git diff --cached
  ```

- [ ] **Đã chạy script kiểm tra** (nếu có):
  ```bash
  bash check-before-commit.sh
  ```

## 🚀 Sau khi commit

- [ ] **Đã test clone và setup** từ GitHub:
  ```bash
  git clone <your-repo-url>
  cd CafeManagerProKotlin
  # Follow SETUP.md instructions
  ```

## ⚠️ Nếu đã commit nhầm keys

1. **NGAY LẬP TỨC**: Revoke keys trên service providers
2. Tạo keys mới
3. Xóa keys cũ khỏi git history (xem SECURITY.md)

---

**Lưu ý**: Checklist này nên được kiểm tra TRƯỚC MỖI commit lên GitHub!

