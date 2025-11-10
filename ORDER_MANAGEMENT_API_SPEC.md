# API Specification - Quản lý Đơn hàng (Order Management)

## 📋 Tổng quan

Tài liệu này mô tả các API endpoints cần thiết để triển khai backend cho hệ thống quản lý đơn hàng của CafeManagerPro.

**Firebase Realtime Database Path:** `order/`

---

## 🗂️ Data Model

### Order Model
```json
{
  "id": 1234567890,
  "userEmail": "user@example.com",
  "dateTime": "2025-10-09 19:00:00",
  "drinks": [
    {
      "id": 1,
      "name": "Cà phê sữa",
      "image": "url",
      "price": 25000,
      "count": 2,
      "variant": "ice",
      "size": "regular",
      "sugar": "normal",
      "ice": "normal",
      "note": "Ít đá"
    }
  ],
  "price": 50000,
  "voucher": 5000,
  "total": 45000,
  "paymentMethod": "GoPay",
  "status": 1,
  "rate": 4.5,
  "review": "Rất ngon!",
  "address": {
    "id": 1,
    "name": "Nhà riêng",
    "address": "123 Đường ABC",
    "phone": "0123456789"
  },
  "latitude": 10.762622,
  "longitude": 106.660172
}
```

### Order Status
```kotlin
STATUS_NEW = 1       // Đơn hàng mới
STATUS_DOING = 2     // Đang thực hiện
STATUS_ARRIVED = 3   // Đã giao
STATUS_COMPLETE = 4  // Hoàn thành
```

---

## 🔌 API Endpoints

### 1. **Tạo đơn hàng mới (Create Order)**

**Endpoint:** `POST /api/orders`

**Request Body:**
```json
{
  "id": 1234567890,
  "userEmail": "user@example.com",
  "dateTime": "2025-10-09 19:00:00",
  "drinks": [...],
  "price": 50000,
  "voucher": 5000,
  "total": 45000,
  "paymentMethod": "GoPay",
  "status": 1,
  "address": {...},
  "latitude": 10.762622,
  "longitude": 106.660172
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order created successfully",
  "orderId": 1234567890
}
```

**Firebase Implementation:**
```kotlin
// File: PaymentActivity.kt (line 35-50)
MyApplication[this].getOrderDatabaseReference()
    ?.child(mOrderBooking!!.id.toString())
    ?.setValue(mOrderBooking) { error, ref ->
        // Success callback
    }
```

---

### 2. **Lấy danh sách đơn hàng (Get Orders)**

#### 2.1 Lấy tất cả đơn hàng (Admin)

**Endpoint:** `GET /api/orders`

**Query Parameters:**
- `status` (optional): Filter by status (1,2,3,4)
- `limit` (optional): Number of records
- `offset` (optional): Pagination offset

**Response:**
```json
{
  "success": true,
  "data": [
    {
      "id": 1234567890,
      "userEmail": "user@example.com",
      "dateTime": "2025-10-09 19:00:00",
      "drinks": [...],
      "price": 50000,
      "total": 45000,
      "status": 1
    }
  ],
  "total": 100
}
```

**Firebase Implementation:**
```kotlin
// File: OrderFragment.kt (line 78-109)
MyApplication[activity!!].getOrderDatabaseReference()
    ?.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            for (dataSnapshot in snapshot.children) {
                val order = dataSnapshot.getValue(Order::class.java)
                // Process order
            }
        }
    })
```

#### 2.2 Lấy đơn hàng theo user

**Endpoint:** `GET /api/orders/user/{email}`

**Response:** Same as above

**Firebase Implementation:**
```kotlin
// File: OrderFragment.kt (line 143-146)
MyApplication[activity!!].getOrderDatabaseReference()
    ?.orderByChild("userEmail")
    ?.equalTo(user!!.email)
    ?.addValueEventListener(...)
```

---

### 3. **Lấy chi tiết đơn hàng (Get Order Detail)**

**Endpoint:** `GET /api/orders/{orderId}`

**Response:**
```json
{
  "success": true,
  "data": {
    "id": 1234567890,
    "userEmail": "user@example.com",
    "dateTime": "2025-10-09 19:00:00",
    "drinks": [...],
    "price": 50000,
    "voucher": 5000,
    "total": 45000,
    "paymentMethod": "GoPay",
    "status": 1,
    "rate": 4.5,
    "review": "Rất ngon!",
    "address": {...},
    "latitude": 10.762622,
    "longitude": 106.660172
  }
}
```

**Firebase Implementation:**
```kotlin
// File: TrackingOrderActivity.kt (line 110-127)
MyApplication[this].getOrderDetailDatabaseReference(orderId)
    ?.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            mOrder = snapshot.getValue(Order::class.java)
        }
    })
```

---

### 4. **Cập nhật trạng thái đơn hàng (Update Order Status)**

**Endpoint:** `PATCH /api/orders/{orderId}/status`

**Request Body:**
```json
{
  "status": 2
}
```

**Response:**
```json
{
  "success": true,
  "message": "Order status updated successfully"
}
```

**Firebase Implementation:**
```kotlin
// File: TrackingOrderActivity.kt (line 166-184)
val map: MutableMap<String, Any> = HashMap()
map["status"] = status
MyApplication[this].getOrderDatabaseReference()
    ?.child(mOrder!!.id.toString())
    ?.updateChildren(map) { error, ref ->
        // Success callback
    }
```

---

### 5. **Cập nhật đánh giá đơn hàng (Update Order Rating)**

**Endpoint:** `PATCH /api/orders/{orderId}/rating`

**Request Body:**
```json
{
  "rate": 4.5,
  "review": "Rất ngon!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Rating updated successfully"
}
```

**Firebase Implementation:**
```kotlin
// File: RatingReviewActivity.kt (line 84-93)
val map: MutableMap<String, Any?> = HashMap()
map["rate"] = rating.rate
map["review"] = rating.review
MyApplication[this].getOrderDatabaseReference()
    ?.child(ratingReview!!.id)
    ?.updateChildren(map) { error, ref ->
        // Success callback
    }
```

---

### 6. **Xóa đơn hàng (Delete Order)**

**Endpoint:** `DELETE /api/orders/{orderId}`

**Response:**
```json
{
  "success": true,
  "message": "Order deleted successfully"
}
```

**Note:** Hiện tại app không có chức năng xóa đơn hàng, nhưng nên implement cho admin.

---

## 🔐 Authentication & Authorization

### Headers Required:
```
Authorization: Bearer {firebase_token}
Content-Type: application/json
```

### Permission Rules:

| Endpoint | User | Admin |
|----------|------|-------|
| POST /api/orders | ✅ Own orders | ✅ All |
| GET /api/orders | ✅ Own orders | ✅ All |
| GET /api/orders/{id} | ✅ Own orders | ✅ All |
| PATCH /api/orders/{id}/status | ❌ | ✅ |
| PATCH /api/orders/{id}/rating | ✅ Own orders | ✅ All |
| DELETE /api/orders/{id} | ❌ | ✅ |

**Admin Check:**
```kotlin
// Email phải chứa "@admin.com"
if (user.email.contains("@admin.com")) {
    user.isAdmin = true
}
```

---

## 📊 Query Filters

### Filter by Status:
```
GET /api/orders?status=1  // Đơn hàng mới
GET /api/orders?status=2  // Đang thực hiện
GET /api/orders?status=3  // Đã giao
GET /api/orders?status=4  // Hoàn thành
```

### Filter by Date Range:
```
GET /api/orders?startDate=2025-10-01&endDate=2025-10-31
```

### Filter by User:
```
GET /api/orders?userEmail=user@example.com
```

### Pagination:
```
GET /api/orders?limit=20&offset=0
```

---

## 🔄 Real-time Updates (WebSocket/SSE)

Để đồng bộ real-time như Firebase, backend nên hỗ trợ:

### WebSocket Endpoint:
```
ws://api.example.com/ws/orders
```

### Events:
- `order.created` - Đơn hàng mới
- `order.updated` - Cập nhật đơn hàng
- `order.status_changed` - Thay đổi trạng thái
- `order.deleted` - Xóa đơn hàng

---

## 🗄️ Database Schema (SQL)

Nếu dùng SQL thay vì Firebase:

```sql
CREATE TABLE orders (
    id BIGINT PRIMARY KEY,
    user_email VARCHAR(255) NOT NULL,
    date_time TIMESTAMP NOT NULL,
    price INT NOT NULL,
    voucher INT DEFAULT 0,
    total INT NOT NULL,
    payment_method VARCHAR(50),
    status TINYINT DEFAULT 1,
    rate DECIMAL(2,1) DEFAULT 0,
    review TEXT,
    latitude DECIMAL(10,8),
    longitude DECIMAL(11,8),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_email (user_email),
    INDEX idx_status (status),
    INDEX idx_date_time (date_time)
);

CREATE TABLE order_drinks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    drink_id BIGINT NOT NULL,
    drink_name VARCHAR(255) NOT NULL,
    drink_image TEXT,
    price INT NOT NULL,
    count INT NOT NULL,
    variant VARCHAR(20),
    size VARCHAR(20),
    sugar VARCHAR(20),
    ice VARCHAR(20),
    note TEXT,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

CREATE TABLE order_addresses (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    name VARCHAR(255),
    address TEXT NOT NULL,
    phone VARCHAR(20),
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);
```

---

## 📝 Code Reference

### Key Files:

1. **Model:**
   - `app/src/main/java/com/pro/shopfee/model/Order.kt`
   - `app/src/main/java/com/pro/shopfee/model/DrinkOrder.kt`

2. **Create Order:**
   - `app/src/main/java/com/pro/shopfee/activity/PaymentActivity.kt` (line 35-50)

3. **Read Orders:**
   - `app/src/main/java/com/pro/shopfee/fragment/OrderFragment.kt` (line 78-146)

4. **Update Order:**
   - `app/src/main/java/com/pro/shopfee/activity/TrackingOrderActivity.kt` (line 166-184)
   - `app/src/main/java/com/pro/shopfee/activity/RatingReviewActivity.kt` (line 84-93)

5. **Firebase Reference:**
   - `app/src/main/java/com/pro/shopfee/MyApplication.kt` (line 48-58)

---

## 🚀 Implementation Recommendations

### Backend Stack Options:

1. **Node.js + Express + Firebase Admin SDK**
   - Dễ migrate từ Firebase client
   - Giữ nguyên cấu trúc dữ liệu

2. **Node.js + Express + PostgreSQL**
   - Tốt cho query phức tạp
   - Cần chuyển đổi data structure

3. **Spring Boot + MySQL**
   - Enterprise-grade
   - Tích hợp tốt với Kotlin/Android

4. **Laravel + MySQL**
   - Rapid development
   - Built-in authentication

### Migration Steps:

1. ✅ Export data từ Firebase
2. ✅ Setup database schema
3. ✅ Implement REST APIs
4. ✅ Add authentication middleware
5. ✅ Setup WebSocket/SSE cho real-time
6. ✅ Update Android app để call REST API thay vì Firebase
7. ✅ Testing & deployment

---

## 📞 Contact

Nếu cần thêm thông tin về implementation, vui lòng tham khảo source code tại các file đã liệt kê ở trên.
