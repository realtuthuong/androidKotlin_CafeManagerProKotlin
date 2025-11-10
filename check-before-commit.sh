#!/bin/bash

# Script kiểm tra trước khi commit lên GitHub
# Chạy: bash check-before-commit.sh

echo "🔍 Kiểm tra files nhạy cảm trước khi commit..."
echo ""

ERRORS=0

# Kiểm tra google-services.json
if git ls-files --error-unmatch app/google-services.json > /dev/null 2>&1; then
    echo "❌ ERROR: app/google-services.json đang được track bởi git!"
    echo "   Chạy: git rm --cached app/google-services.json"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ app/google-services.json không được track"
fi

# Kiểm tra ApiKeys.kt
if git ls-files --error-unmatch app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt > /dev/null 2>&1; then
    echo "❌ ERROR: ApiKeys.kt đang được track bởi git!"
    echo "   Chạy: git rm --cached app/src/main/java/com/pro/shopfee/utils/ApiKeys.kt"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ ApiKeys.kt không được track"
fi

# Kiểm tra API keys thật trong code
echo ""
echo "🔍 Kiểm tra API keys thật trong code..."

# Google Maps API Key pattern
if grep -r "AIzaSy[A-Za-z0-9_-]\{35\}" app/src/ --exclude-dir=build --exclude="*.example" > /dev/null 2>&1; then
    echo "⚠️  WARNING: Tìm thấy Google Maps API Key trong code"
    echo "   Kiểm tra: grep -r 'AIzaSy' app/src/ --exclude-dir=build"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ Không tìm thấy Google Maps API Key thật"
fi

# VNPay Hash Secret
if grep -r "KFXCQ9O7TVXNMMAXULFHROLQJDTT1ZNQ" app/src/ --exclude-dir=build --exclude="*.example" > /dev/null 2>&1; then
    echo "⚠️  WARNING: Tìm thấy VNPay Hash Secret trong code"
    echo "   Nên thay thế bằng placeholder trước khi commit"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ Không tìm thấy VNPay Hash Secret thật"
fi

# Blockchain RPC API Key
if grep -r "R4y1vJ3E5sffi90cr_hrMBlQNV3EaUip" app/src/ --exclude-dir=build --exclude="*.example" > /dev/null 2>&1; then
    echo "⚠️  WARNING: Tìm thấy Blockchain RPC API Key trong code"
    echo "   Nên thay thế bằng placeholder trước khi commit"
    ERRORS=$((ERRORS + 1))
else
    echo "✅ Không tìm thấy Blockchain RPC API Key thật"
fi

echo ""
if [ $ERRORS -eq 0 ]; then
    echo "✅ Tất cả kiểm tra đều PASS! Có thể commit an toàn."
    exit 0
else
    echo "❌ Tìm thấy $ERRORS vấn đề. Vui lòng sửa trước khi commit!"
    echo ""
    echo "📖 Xem thêm hướng dẫn trong:"
    echo "   - SETUP.md"
    echo "   - SECURITY.md"
    exit 1
fi

