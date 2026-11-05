# Safe Delete

A small full-stack application for safely deleting files on the server. The backend exposes a single REST endpoint that only deletes regular files located inside a configured allowed root directory, refusing symlinks, directories, and paths outside the root. The frontend is a minimal React UI for submitting paths and viewing per-path results.

## Stack

Spring Boot 3.2 / Java 17 / React 18 / Vite 5

## Layout

```
.
├── backend/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/com/example/demo/
│           │   ├── DemoApplication.java
│           │   └── controller/ApiController.java
│           └── resources/application.properties
└── frontend/
    ├── index.html
    ├── package.json
    ├── vite.config.js
    └── src/
        ├── App.jsx
        ├── App.css
        └── main.jsx
```

## How to run

Backend (listens on `http://localhost:8080`):

```
cd backend
mvn spring-boot:run
```

Frontend (dev server on `http://localhost:5173`, proxies `/api` to the backend):

```
cd frontend
npm install
npm run dev
```

## API

### `POST /api/delete`

Deletes one or more files. Each path is validated independently; the response contains a result entry per requested path.

**Request body**

```json
{
  "paths": ["/absolute/path/to/file.txt"]
}
```

**Response body**

```json
{
  "results": [
    { "path": "/absolute/path/to/file.txt", "deleted": true, "reason": "deleted" }
  ]
}
```

**Per-path outcomes**

| `deleted` | `reason` |
|-----------|----------|
| `true`    | `deleted` |
| `false`   | `empty path` |
| `false`   | `outside allowed root` |
| `false`   | `does not exist` |
| `false`   | `symlink refused` |
| `false`   | `not a regular file` |
| `false`   | `invalid path` |
| `false`   | `io error: <message>` |

## Notes

- The allowed root is configured via `app.allowed-root` in `backend/src/main/resources/application.properties` (default `./allowed`, resolved relative to the backend working directory). Paths are normalized and must resolve inside this root.
- Only regular files are deleted. Directories, symlinks, and special files are rejected.
- The endpoint is unauthenticated and has no CSRF protection — do not expose it publicly without adding authentication and authorization.
- There is no file listing or upload endpoint; the UI requires the user to type absolute paths manually.
- The frontend dev server relies on Vite's proxy for `/api`; for production builds, serve the static assets behind a reverse proxy that forwards `/api` to the backend.
- No persistence layer is used; results are returned synchronously per request.