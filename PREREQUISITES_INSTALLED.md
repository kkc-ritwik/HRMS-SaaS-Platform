# HRMS Platform - Prerequisites Status

## ✅ Installed Successfully

| Tool | Version | Location | Status |
|------|---------|----------|--------|
| Java JDK | 17.0.12 | `C:\Program Files\Java\jdk-17` | ✅ Ready |
| Maven | 3.9.6 | `C:\Users\ritwi\apache-maven-3.9.6` | ✅ Ready |
| Node.js | 24.16.0 | System PATH | ✅ Ready |
| npm | 11.13.0 | System PATH | ✅ Ready |
| Docker | 29.5.3 | System | ⚠️ Needs restart |

## 📋 Next Steps

### 1. Fix Docker Desktop
Docker Desktop is installed but needs to be restarted. Try:
```powershell
# Option 1: Restart Docker Desktop from system tray
# Right-click Docker icon → Quit → Start again

# Option 2: If WSL issues, run as Administrator:
wsl --update
wsl --set-default-version 2
```

### 2. Start Infrastructure Services
Once Docker is running, execute:
```powershell
cd C:\Users\ritwi\Desktop\HRMS-SaaS-Platform
docker-compose up -d
```

This will start:
- PostgreSQL 16 (port 5433)
- Redis 7 (port 6379)
- Kafka (port 9092)
- Zookeeper (port 2181)
- Elasticsearch (port 9200)
- MinIO (ports 9000, 9001)

### 3. Verify Infrastructure
```powershell
docker ps
docker exec hrms-postgres pg_isready -U hrms_user -d hrms_db
```

### 4. Build Backend Services
```powershell
mvn clean install -DskipTests
```

### 5. Install Frontend Dependencies
```powershell
cd hrms-frontend
npm install
```

### 6. Start Development

**Backend (each service):**
```powershell
# Start infrastructure services first
cd service-discovery
mvn spring-boot:run
```

**Frontend:**
```powershell
cd hrms-frontend
npm run dev
```

## 🔧 Environment Variables (if needed)

Maven is in your PATH for this session. To make it permanent across all terminal sessions:
- Maven: `C:\Users\ritwi\apache-maven-3.9.6\bin`
- Java: `C:\Program Files\Java\jdk-17\bin` (already set by installer)
- Node: Already in PATH (set by installer)

## 📚 Reference Documents

- [SETUP_GUIDE.md](docs/SETUP_GUIDE.md) - Comprehensive setup instructions
- [RUNBOOK.md](RUNBOOK.md) - Production operations guide
- [README.md](README.md) - Platform overview

## 🐛 Troubleshooting

### Java not found in new terminals
```powershell
# Java should be automatically in PATH
# If not, add: C:\Program Files\Java\jdk-17\bin
```

### Maven not found in new terminals
```powershell
$env:Path += ";C:\Users\ritwi\apache-maven-3.9.6\bin"
```

### Docker daemon not starting
1. Check Windows features: "Virtual Machine Platform" and "Windows Subsystem for Linux"
2. Update WSL: `wsl --update`
3. Restart Windows

---

**Installation completed:** 2026-06-14
