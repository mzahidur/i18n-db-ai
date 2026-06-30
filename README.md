# i18n-db-ai

> **Multilingual Database + AI Translation Library for Spring Boot**

A plug-and-play Spring Boot starter that provides fast, database-backed internationalization with intelligent AI fallback.

**Group:** `io.github.mzahidur`  
**Artifact:** `i18n-db-ai-spring-boot-starter`

---

## ✨ Features

- 🚀 Database-first translations (`i18n_messages` table)
- 🔄 Smart fallback chain: **Database → Properties → AI**
- 🤖 AI-generated translations automatically persisted
- 🏢 Optional multi-tenant support
- 🌱 Seamless Spring `MessageSource` integration (`@Primary`)
- ⚡ Configurable caching (Caffeine or Redis)
- 🗄️ Vendor-specific Flyway migrations
- 🧅 Clean Architecture (Onion / Hexagonal)

---

# Quick Start

## 1. Add the Dependency

### Maven

```xml
<dependency>
    <groupId>io.github.mzahidur</groupId>
    <artifactId>i18n-db-ai-spring-boot-starter</artifactId>
    <version>1.0.1</version>
</dependency>
```

---

## 2. Configure the Starter

```yaml
i18n:
  db:
    enabled: true

    cache:
      type: caffeine      # caffeine | redis | none
      ttl: 3600s

    ai:
      enabled: true
      provider: spring-ai # spring-ai | langchain4j | none
      store-result: true

    flyway:
      enabled: true
```

---

## 3. Use Spring's MessageSource

```java
@Service
public class GreetingService {

    private final MessageSource messageSource;

    public GreetingService(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String getWelcomeMessage() {
        return messageSource.getMessage(
            "user.welcome",
            null,
            LocaleContextHolder.getLocale()
        );
    }
}
```

That's it. The starter automatically resolves messages using the following strategy:

```
Database
    ↓
messages.properties
    ↓
AI Translation
    ↓
Persist Translation
```

---

# Translation Resolution Flow

```text
                 Request Message
                        │
                        ▼
            Database Translation?
               │             │
             Yes             No
               │             ▼
               │      Properties File?
               │         │        │
               │       Yes        No
               │         │         ▼
               │         │   AI Translation
               │         │         │
               │         ▼         ▼
               └──────► Return & Persist
```

---

# Configuration

| Property | Description | Default |
|----------|-------------|---------|
| `i18n.db.enabled` | Enable database-backed translations | `true` |
| `i18n.db.cache.type` | Cache implementation (`caffeine`, `redis`, `none`) | `caffeine` |
| `i18n.db.cache.ttl` | Cache expiration | `3600s` |
| `i18n.db.ai.enabled` | Enable AI translation fallback | `true` |
| `i18n.db.ai.provider` | AI provider implementation | `spring-ai` |
| `i18n.db.ai.store-result` | Persist AI-generated translations | `true` |
| `i18n.db.flyway.enabled` | Run Flyway migrations | `true` |

---

# Extension Points

The starter is designed to be extensible.

You can provide your own implementations for:

- `CacheKeyResolver`
- `AiTranslationPort`
- `TenantResolver`
- Cache implementation
- AI provider
- Translation persistence strategy

---

# Architecture

The project follows **Clean Architecture (Onion / Hexagonal)** principles.

```text
                Application
                     │
      ┌──────────────┼──────────────┐
      │              │              │
   Message      AI Translation   Caching
     Port            Port          Port
      │              │              │
      └──────────────┼──────────────┘
                     │
               Infrastructure
                     │
      ┌──────────────┼──────────────┐
      │              │              │
   Database      Spring AI      Redis/Caffeine
```

---

# Supported Features

- ✅ Spring Boot Auto Configuration
- ✅ Spring `MessageSource`
- ✅ Flyway Integration
- ✅ AI Translation
- ✅ Multi-Tenant Ready
- ✅ Database Persistence
- ✅ Cache Abstraction
- ✅ Vendor-specific SQL Migrations

---

# Roadmap

- [x] Database translation source
- [x] AI fallback translation
- [x] Automatic translation persistence
- [x] Spring Boot starter
- [x] Flyway migrations
- [x] Multi-tenant support
- [ ] Metrics & Micrometer integration
- [ ] Admin REST API
- [ ] Translation management UI
- [ ] Batch translation import/export

---

# Documentation

Additional documentation, configuration examples, and advanced usage will be available in the project wiki.

---

# License

Licensed under the **Apache License 2.0**.

See the `LICENSE` file for details.