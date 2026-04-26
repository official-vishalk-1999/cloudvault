# cloudvault

A file storage app where users can upload, view and delete images — stored on AWS S3, metadata tracked in DynamoDB.

Runs on EC2 — needs AWS S3 bucket and DynamoDB tables set up.

---

## Stack

- Spring Boot, Java 17
- AWS S3 (file storage)
- AWS DynamoDB (user and file metadata)
- Spring Security Crypto (BCrypt password hashing)
- Plain HTML + Bootstrap (frontend)

---

## How it works

User signs up → credentials stored in DynamoDB Users table → on login, BCrypt checks password → session stored in browser sessionStorage → user uploads images → file goes to S3, metadata (userId, imageId, uploadTime) saved in DynamoDB Images table → presigned URLs generated on fetch so images are accessible without making bucket public.

```
Upload flow:
POST /api/files/upload
        ↓
   S3 (stores file)
        +
   DynamoDB (saves metadata)

Fetch flow:
GET /api/files/{userId}
        ↓
   DynamoDB (get metadata)
        ↓
   S3Presigner (generate temp URL per file)
```

Admin can view all users, see image count per user, enable/disable accounts, and delete users — deleting a user also cleans up all their S3 files and DynamoDB records.

---

## Pages

- `/` — landing, choose user or admin
- `/login.html` — user login
- `/signup.html` — user signup
- `/dashboard.html` — upload, view, delete files
- `/admin-login.html` — admin login
- `/admin-dashboard.html` — manage users

---

## API

```
POST /api/auth/signup         → register user
POST /api/auth/login          → login

POST /api/files/upload        → upload file (multipart)
GET  /api/files/{userId}      → get all files with presigned URLs
DELETE /api/files/delete      → delete file from S3 + DynamoDB

GET  /api/admin/users         → all users with image count
POST /api/admin/toggle        → enable or disable user
DELETE /api/admin/delete      → delete user + all their files
```

---

## Run locally

Need AWS account with S3 bucket and DynamoDB tables (Users, Images) created.

```bash
git clone https://github.com/official-vishalk-1999/cloudvault
cd cloudvault
```

Set env vars:
```
AWS_ACCESS_KEY
AWS_SECRET_KEY
AWS_REGION
AWS_S3_BUCKET
```

```bash
./mvnw spring-boot:run
```

Open http://localhost:8080

Default admin login: `admin / admin`

---

## Notes

- File upload restricted to JPG, JPEG, PNG — max 1MB enforced on frontend
- Deleting a user from admin panel removes all their S3 files and DynamoDB records — not reversible
- DynamoDB Images table: partition key `userId`, sort key `imageId`
- DynamoDB Users table: partition key `email`
