ถ้าคุณใช้ `proxy_pass` ใน **Nginx** กับ **Spring Boot + Swagger** และ Swagger UI ยังแสดง **"Swagger Petstore"** แทนที่จะเป็น API ของคุณเอง ปัญหานี้อาจเกิดจาก Swagger UI ไม่สามารถโหลดไฟล์ **OpenAPI JSON** (`swagger-config.json` หรือ `openapi.json`) ได้อย่างถูกต้อง

---

## 🔍 **สาเหตุที่เป็นไปได้และวิธีแก้ไข**
### **1. Nginx ไม่ได้ Proxy ไปที่ API Docs อย่างถูกต้อง**
หากคุณใช้ `proxy_pass` ไปยัง `/api/v1/` Swagger อาจยังคงมองหา API docs ที่ `/v3/api-docs` โดยที่ Nginx ไม่ได้ส่งต่อไปถูกต้อง

#### ✅ **วิธีแก้: แก้ไข Nginx Configuration**
ตรวจสอบว่าไฟล์ **Nginx config** มีการกำหนดค่า `location` ให้รองรับ Swagger อย่างถูกต้อง เช่น:

```nginx
server {
    listen 80;

    location /api/v1/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/v1/swagger-ui/ {
        proxy_pass http://localhost:8080/swagger-ui/;
    }

    location /api/v1/v3/api-docs/ {
        proxy_pass http://localhost:8080/v3/api-docs/;
    }
}
```
> **🔹 หมายเหตุ**: ตรวจสอบว่า `proxy_pass` กำหนดไปที่ **port ของ Spring Boot** ที่ถูกต้อง (เช่น `8080`)

---

### **2. กำหนดค่า Swagger ใน Spring Boot ให้รองรับ Path `/api/v1/`**
โดยปกติ **Springdoc OpenAPI** จะใช้ `swagger-ui.html` และ `v3/api-docs` เป็นค่าเริ่มต้น แต่เมื่อคุณใช้ Nginx ให้แก้ไข **application.properties** หรือ **application.yml**:

#### ✅ **แก้ไข `application.properties`**
```properties
springdoc.swagger-ui.path=/api/v1/swagger-ui.html
springdoc.api-docs.path=/api/v1/v3/api-docs
```

#### ✅ **หรือแก้ไข `application.yml`**
```yaml
springdoc:
  swagger-ui:
    path: /api/v1/swagger-ui.html
  api-docs:
    path: /api/v1/v3/api-docs
```

---

### **3. ล้าง Cache ของ Swagger UI**
บางครั้ง **Swagger UI** จะใช้ข้อมูลเก่าที่ Cache ไว้ ทำให้ยังแสดง **"Swagger Petstore"** อยู่

#### ✅ **วิธีแก้**:
- ลอง **เปิดใน Incognito Mode** หรือ
- กด `Ctrl + Shift + R` (Windows/Linux) หรือ `Cmd + Shift + R` (Mac) เพื่อบังคับให้โหลดใหม่
- ลบ Cache ของเบราว์เซอร์แล้วลองเข้าใหม่

---

### **4. กำหนด `servers` ใน OpenAPI Config**
หาก Swagger UI ยังคงไม่ทำงาน อาจต้องระบุ **Base Path** ให้ตรงกับ `/api/v1/` โดยใช้ **@OpenAPIDefinition**

#### ✅ **เพิ่ม Configuration ใน Spring Boot**
```java
@OpenAPIDefinition(
    servers = {
        @Server(url = "/api/v1", description = "API Base Path")
    }
)
@Configuration
public class SwaggerConfig {
}
```
---

### **5. รีสตาร์ท Spring Boot และ Nginx**
หลังจากเปลี่ยนแปลงการตั้งค่า **อย่าลืม Restart ทั้ง Spring Boot และ Nginx**:

```sh
sudo systemctl restart nginx
```
และ **Restart Spring Boot** เพื่อให้ค่าใหม่มีผล

---

### **6. ตรวจสอบ Log และ Network Request**
ถ้ายังไม่ทำงาน:
- ดู Log ของ Nginx:
```sh
sudo tail -f /var/log/nginx/error.log
```
- เปิด **Developer Tools (F12 → Network tab)** แล้วดูว่า Swagger UI กำลังพยายามโหลดไฟล์ `v3/api-docs` จากที่ไหน

---

## ✅ **สรุป**
หลังจากแก้ไขทั้งหมดแล้ว **ให้ลองเปิด Swagger UI ใหม่ที่:**
```
http://yourdomain.com/api/v1/swagger-ui.html
```
และลองเข้าดู API Docs โดยตรง:
```
http://yourdomain.com/api/v1/v3/api-docs
```
ถ้าทำถูกต้อง Swagger จะไม่แสดง **"Swagger Petstore"** อีกต่อไป 🚀

หากยังมีปัญหา บอกได้เลยครับ เดี๋ยวช่วยแก้ให้! 😊


When using `proxy_pass` in Nginx with a custom path like `/api/v1/`, and Swagger UI still shows the default **"Swagger Petstore"**, it usually means that the Swagger UI is not correctly resolving its configuration file (`swagger-config.json` or `openapi.json`).

### **Possible Issues and Fixes**
#### **1. Swagger Configuration URL Not Resolving Properly**
Since you're proxying through `/api/v1/`, your Swagger UI might still be looking for the API docs at `/v3/api-docs`, but Nginx is not forwarding this correctly.

##### **Fix: Adjust Nginx Configuration**
Ensure your Nginx config correctly forwards requests to the `swagger-ui.html` and API docs:

```nginx
server {
    listen 80;

    location /api/v1/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/v1/swagger-ui/ {
        proxy_pass http://localhost:8080/swagger-ui/;
    }

    location /api/v1/v3/api-docs/ {
        proxy_pass http://localhost:8080/v3/api-docs/;
    }
}
```

#### **2. Adjust Swagger Base Path in Spring Boot**
In your `application.properties` or `application.yml`, set:
```properties
springdoc.swagger-ui.path=/api/v1/swagger-ui.html
springdoc.api-docs.path=/api/v1/v3/api-docs
```
or in `application.yml`:
```yaml
springdoc:
  swagger-ui:
    path: /api/v1/swagger-ui.html
  api-docs:
    path: /api/v1/v3/api-docs
```

#### **3. Ensure You Are Not Caching Swagger Configuration**
Try opening **Swagger UI** in an **incognito window** or clearing the browser cache (`Ctrl + Shift + R` or `Cmd + Shift + R` on Mac). If it works, your browser was caching the old Swagger UI settings.

#### **4. Update the `servers` in OpenAPI Config**
If you are using **Springdoc OpenAPI**, you may need to explicitly define the server base URL in `@OpenAPIDefinition`:
```java
@OpenAPIDefinition(
    servers = {
        @Server(url = "/api/v1", description = "API Base Path")
    }
)
@Configuration
public class SwaggerConfig {
}
```
This ensures Swagger generates links correctly under `/api/v1/`.

#### **5. Restart Spring Boot and Nginx**
Once you've applied the fixes:
```sh
sudo systemctl restart nginx
```
And restart your Spring Boot application.

#### **6. Check Logs**
If it still doesn’t work, check your logs:
```sh
sudo tail -f /var/log/nginx/error.log
```
And look at the network requests in **Developer Tools (F12 → Network tab)** to see where Swagger is trying to fetch `v3/api-docs`.

---

### **Final Check**
After applying these changes, go to:
```
http://yourdomain.com/api/v1/swagger-ui.html
```
If you still see **Swagger Petstore**, check if `v3/api-docs` returns your correct OpenAPI spec:
```
http://yourdomain.com/api/v1/v3/api-docs
```

Let me know if you need further debugging help! 🚀

