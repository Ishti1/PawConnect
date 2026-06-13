# CatConnect API Reference

Base URL: `http://localhost:8080/api`

## Authentication

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/register` | No | Create account |
| POST | `/auth/login` | No | Login, returns JWT |
| GET | `/auth/me` | Yes | Current user profile |

### Register / Login body

```json
{
  "email": "user@example.com",
  "password": "secret123",
  "displayName": "Cat Lover"
}
```

## Public resources

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/health` | Service health check |
| GET | `/vets` | List veterinarians |
| GET | `/vets/emergency` | Emergency vets only |
| GET | `/shops` | Nearby cat shops |
| GET | `/shelters` | Shelter directory |
| GET | `/knowledge` | Care articles |
| GET | `/food-recommendations` | Food suggestions |
| GET | `/memes` | Cat memes feed |
| GET | `/adoptions` | Adoption listings |
| GET | `/lost-found` | Active lost & found posts |
| DELETE | `/lost-found/{id}` | Remove post (auth; soft-delete) |
| GET | `/donations` | Donation campaigns |
| GET | `/files/**` | Serve stored images |

## Authenticated resources

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET/POST | `/moments` | User cat moments |
| POST | `/lost-found` | Create lost/found post |
| POST | `/adoptions` | List cat for adoption |
| POST | `/donations/{id}/contribute` | Record contribution |
| POST | `/upload` | Upload image (multipart) |

## Upload

```
POST /api/upload?category=moments
Content-Type: multipart/form-data
Authorization: Bearer <token>

file: <binary>
```

Response:

```json
{
  "url": "/api/files/moments/abc-123.jpg"
}
```

## Error format

```json
{
  "message": "Human-readable error",
  "timestamp": "2026-05-25T12:00:00"
}
```
