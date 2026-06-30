# Contributing to i18n-db-ai

First off, thank you for considering contributing to **i18n-db-ai**! 🎉

Whether you're fixing a bug, improving documentation, adding a new AI provider, or proposing a new feature, your contributions are greatly appreciated.

---

# Getting Started

## 1. Fork and Clone

Fork the repository, then clone your fork locally.

```bash
git clone https://github.com/mzahidur/i18n-db-ai.git
cd i18n-db-ai
```

---

## 2. Build the Project

Using Maven Daemon (recommended):

```bash
mvnd clean install
```

Or with Maven:

```bash
mvn clean install
```

---

## 3. Run Tests

```bash
mvn test
```

Before submitting a Pull Request, ensure:

- All tests pass
- The project builds successfully
- No compiler warnings are introduced

---

# Project Structure

The project follows **Clean Architecture (Onion / Hexagonal)** principles.

```text
i18n-db-ai
├── i18n-db-ai-domain
│   ├── Domain models
│   ├── Value objects
│   └── Ports
│
├── i18n-db-ai-application
│   ├── Use cases
│   ├── Services
│   └── Translation orchestration
│
├── i18n-db-ai-infrastructure
│   ├── JPA adapters
│   ├── Flyway migrations
│   ├── Cache adapters
│   ├── AI providers
│   └── Spring implementations
│
└── i18n-db-ai-spring-boot-starter
    ├── Auto-configuration
    ├── Properties
    └── Spring Boot integration
```

---

# Coding Guidelines

Please follow these principles when contributing.

## Architecture

- Keep the project aligned with **Clean Architecture**
- The **Domain** module must not depend on Spring or any external framework
- Business logic belongs in the Application layer
- Infrastructure should implement ports rather than contain business logic

---

## Code Style

- Follow standard Java conventions
- Prefer constructor injection
- Keep classes focused and cohesive
- Favor immutability where appropriate
- Avoid unnecessary complexity

---

## Testing

New features should include appropriate tests.

Recommended test types include:

- Unit tests
- Integration tests
- Repository tests
- Auto-configuration tests

---

# Areas for Contribution

Contributions are especially welcome in the following areas:

- 🤖 Additional AI providers (`AiTranslationPort`)
- ⚡ New cache implementations
- 🗄️ Additional database vendor support
- 🌍 Translation improvements
- 🚀 Performance optimizations
- 📖 Documentation
- 🧪 Test coverage
- 🐞 Bug fixes

---

# Development Workflow

Please follow the branching strategy below when contributing.

| Branch Prefix | Purpose |
|---------------|---------|
| `feature/` | New features |
| `bugfix/` | Bug fixes |
| `hotfix/` | Critical production fixes |
| `refactor/` | Code refactoring without behavior changes |
| `docs/` | Documentation improvements |
| `test/` | Adding or improving tests |
| `chore/` | Maintenance tasks, dependency updates, build changes |

### Examples

```bash
feature/add-openai-provider

bugfix/fix-cache-key-collision

docs/update-readme

refactor/simplify-message-resolution

test/add-translation-service-tests
```

Whenever possible, keep each branch focused on a single change.

---

# Pull Request Process

1. Fork the repository.
2. Create a feature branch.

```bash
git checkout -b feature/amazing-feature
```

3. Commit your changes.

```bash
git commit -m "Add amazing feature"
```

4. Push your branch.

```bash
git push origin feature/amazing-feature
```

5. Open a Pull Request.

Please include:

- A clear description of the change
- The motivation behind it
- Any relevant issue references
- Screenshots or examples if applicable

---

# Before You Submit

Before opening a Pull Request, please verify the following:

- [ ] The project builds successfully (`mvn clean install`)
- [ ] All tests pass
- [ ] New functionality includes appropriate tests
- [ ] Existing tests continue to pass
- [ ] Code follows the project's architecture and coding conventions
- [ ] Documentation has been updated if necessary
- [ ] Public APIs are documented
- [ ] Commit messages are clear and descriptive
- [ ] No unnecessary files or generated artifacts are included

A well-scoped Pull Request is much easier to review than a large one containing unrelated changes.

---

# Issue Templates

Before opening a new issue, please search existing issues to avoid duplicates.

If available, use the appropriate GitHub Issue Template:

- 🐞 Bug Report
- ✨ Feature Request
- 📖 Documentation Improvement
- ❓ Question / Support

Using the provided templates helps maintainers understand and address your request more efficiently.

---

# Reporting Issues

If you've found a bug or have a feature request, please open an issue.

When reporting a bug, include:

- Spring Boot version
- Java version
- Database vendor
- Starter version
- Steps to reproduce
- Expected behavior
- Actual behavior
- Relevant logs or stack traces

---

# Commit Message Guidelines

Prefer clear and descriptive commit messages.

Examples:

```text
feat: add DeepSeek AI provider

fix: prevent duplicate translation persistence

docs: improve README examples

test: add cache integration tests

refactor: simplify translation fallback chain
```

---

# Code of Conduct

Please be respectful and constructive in all interactions.

We strive to foster an open, welcoming, and inclusive community for everyone.

---

# Questions?

If you have questions, suggestions, or ideas, feel free to:

- Open a GitHub Issue
- Start a GitHub Discussion
- Submit a Pull Request

We appreciate every contribution—large or small. Thank you for helping make **i18n-db-ai** better!