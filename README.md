# Cloud File Storage 🗂️☁️
A multi-user cloud file storage solution. Allows users to securely upload, store, manage, and download their files and folders through a convenient web interface.
## 🚀 Core Features

### 👥 User Management
- **Registration** of new accounts
- **Authorization** in the system
- **Logout** from the system

### 📁 File and Folder Management
- **Upload** of files and entire folders
- **Create** new empty folders
- **Delete** files and folders
- **Rename** and **move** elements
- **Download** files and folders (as zip archives)

## 🚀 Quick Start Guide

1. Clone the repository
2. Copy the `.env.example` file to `.env`
3. Edit the `.env` file with your values
4. Start the application by running the command: `docker-compose up`
   - After successful startup, services will be available at the following addresses:
   - Main application: http://localhost:8080
   - MinIO Console: http://localhost:9001
   - PostgreSQL: localhost:5432
   - Redis: localhost:6379
5. After the first launch, configure the bucket in MinIO:
   - Open MinIO Console: http://localhost:9001
   - Log in with credentials from the `.env` file (MINIO_USER and MINIO_PASSWORD)
   - Create a bucket with the name specified in MINIO_BUCKET_NAME

**Upload limit:** 500 MB per request
## 🔗 Live Deployment

- **🌐 Live Demo**: [http://176.123.165.134:8080/](http://176.123.165.134:8080/)
- **📚 API Documentation**: [Swagger UI](http://176.123.165.134:8080/swagger-ui/index.html#/)

  
