# 📸 MiradorUM – Portal of Instagrammable Viewpoints and Swings

![Java](https://img.shields.io/badge/Backend-Java_SpringBoot-blue?logo=spring)
![Vue.js](https://img.shields.io/badge/Frontend-Vue.js-4FC08D?logo=vue.js)
![MySQL](https://img.shields.io/badge/Database-MySQL-blue?logo=mysql)
![License](https://img.shields.io/badge/License-MIT-green)

This project aims to develop an **interactive and dynamic portal** dedicated to exploring and sharing scenic viewpoints and swings with high photographic potential. The platform allows users to discover georeferenced locations, view detailed information, and share their experiences with other visitors.

---

## 🎯 Goal

To create a web application where users can:
- Explore viewpoints and swings in Portugal based on their current location;
- Access data such as location, accessibility, and photos of each spot;
- Share experiences and ratings of the places they visit;
- Receive intelligent suggestions for popular or nearby locations, based on their current location and estimated travel time.

---

## 💻 Tech Stack

**Backend:**
- Java 17
- Spring Boot
- Hibernate (JPA)
- MySQL

**Frontend:**
- Vue.js (Vite)

**DevOps:**
- Docker & Docker Compose
- MinIO (S3-compatible object storage)

---

## 🚀 Deployment Instructions

1. Clone the repository:
```bash
git clone https://github.com/mrmikept/miradorum.git
cd miradorum
````

2. Set up environment variables:
   Create a `.env` file inside the `miradorum-front` directory:

```env
VITE_API_URL=http://localhost:8080/       # Backend application URL
VITE_MINIO_URL=http://localhost:9000/     # S3 MinIO service URL
```

3. From the root of the repository, run:

```bash
docker compose up --build -d
```

---

## 🌐 Application URL

Access the application at:
[http://localhost:5173/](http://localhost:5173/)

---

## Directory Structure

```
.
├── benchmark # Performance or load testing scripts
├── miradourum # Backend application (Java Spring Boot)
│   └── (...)
├── miradourum-front # Frontend application (Vue.js)
│   └── (...)
├── payment_service # Fake Payment-related service
├── postman # Postman collections for API testing
└── report # Final report
```

## 👨‍💻 Contributors

This project was developed by students from the University of Minho as part of the curricular units **Application Architectures** and **Interactive and Reliable Systems**, under the **Application Engineering** track.

| Name | Univ. Id |
|------|-----|
| [Gonçalo Marinho](https://github.com/gmarinhog165) | PG55945 |
| [Henrique Vaz](https://github.com/Vaz7)       | PG55947 |
| [Luís Caetano](https://github.com/056zedelta)       | PG57887 |
| [Maya Gomes](https://github.com/mayagomes039)           | PG57891 |
| [Mike Pinto](https://github.com/mrmikept)            | PG55987 |

---

## 📝 License

This project is licensed under the **MIT License**.
See the [LICENSE](./LICENSE) file for more information.

