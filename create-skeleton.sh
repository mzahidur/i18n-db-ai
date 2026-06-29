#!/usr/bin/env bash
# =============================================================================
# i18n-db-ai — Project Skeleton Creator
# io.github.mzahidur:i18n-db-ai
#
# Creates:
#   • Multi-module Maven LIBRARY (not a runnable Spring Boot app)
#   • Parent + 4 child module pom.xml files
#   • Placeholder source/test .java files
#   • Flyway migration SQL placeholders
#   • META-INF starter registration files
#   • .gitignore, README.md, CONTRIBUTING.md
#
# Usage:
#   chmod +x create-skeleton.sh
#   ./create-skeleton.sh [target-dir]
#
# If [target-dir] is omitted, the project is created in ./i18n-db-ai
# =============================================================================

set -euo pipefail

ROOT="${1:-./i18n-db-ai}"

echo ""
echo "╔══════════════════════════════════════════════════════════════╗"
echo "║      i18n-db-ai  —  Maven Library Skeleton Creator           ║"
echo "║      io.github.mzahidur:i18n-db-ai                           ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""
echo "  Target : $ROOT"
echo "  Type   : Spring Boot Auto-Configure Library (thin JAR)"
echo ""

# =============================================================================
# HELPERS
# =============================================================================

mkf() {
    local path="$ROOT/$1"
    mkdir -p "$(dirname "$path")"
    touch "$path"
    echo "  [+] $1"
}

writef() {
    local path="$ROOT/$1"
    mkdir -p "$(dirname "$path")"
    cat > "$path"
    echo "  [pom] $1"
}

mkhdr() {
    echo ""
    echo "  ── $1"
    echo "  $(printf '─%.0s' {1..60})"
}

# =============================================================================
# PATH SHORTCUTS
# =============================================================================

DOMAIN_SRC="i18n-db-ai-domain/src/main/java/io/github/mzahidur/i18n/domain"
DOMAIN_TST="i18n-db-ai-domain/src/test/java/io/github/mzahidur/i18n/domain"

APP_SRC="i18n-db-ai-application/src/main/java/io/github/mzahidur/i18n/application"
APP_TST="i18n-db-ai-application/src/test/java/io/github/mzahidur/i18n/application"

INFRA_SRC="i18n-db-ai-infrastructure/src/main/java/io/github/mzahidur/i18n/infra"
INFRA_TST="i18n-db-ai-infrastructure/src/test/java/io/github/mzahidur/i18n/infra"
INFRA_RES="i18n-db-ai-infrastructure/src/main/resources"

STARTER_SRC="i18n-db-ai-spring-boot-starter/src/main/java/io/github/mzahidur/i18n/starter"
STARTER_RES="i18n-db-ai-spring-boot-starter/src/main/resources"
STARTER_TST="i18n-db-ai-spring-boot-starter/src/test/java/io/github/mzahidur/i18n/starter"

# =============================================================================
# ROOT — PARENT POM
# =============================================================================
mkhdr "ROOT — parent pom.xml"

writef "pom.xml" << 'EOF'
<!-- full parent pom.xml content goes here -->
EOF

# Root non-POM files
mkf ".gitignore"
mkf "README.md"
mkf "CONTRIBUTING.md"

# =============================================================================
# MODULE 1 — DOMAIN POM
# =============================================================================
mkhdr "MODULE 1 — i18n-db-ai-domain"

writef "i18n-db-ai-domain/pom.xml" << 'EOF'
<!-- domain pom.xml content -->
EOF

# Domain source/test placeholders
mkf "$DOMAIN_SRC/model/I18nMessage.java"
mkf "$DOMAIN_TST/model/I18nMessageTest.java"

# =============================================================================
# MODULE 2 — APPLICATION POM
# =============================================================================
mkhdr "MODULE 2 — i18n-db-ai-application"

writef "i18n-db-ai-application/pom.xml" << 'EOF'
<!-- application pom.xml content -->
EOF

mkf "$APP_SRC/service/TranslationApplicationService.java"
mkf "$APP_TST/service/TranslationApplicationServiceTest.java"

# =============================================================================
# MODULE 3 — INFRASTRUCTURE POM
# =============================================================================
mkhdr "MODULE 3 — i18n-db-ai-infrastructure"

writef "i18n-db-ai-infrastructure/pom.xml" << 'EOF'
<!-- infrastructure pom.xml content -->
EOF

mkf "$INFRA_SRC/adapter/JpaTranslationRepositoryAdapter.java"
mkf "$INFRA_TST/adapter/JpaTranslationRepositoryAdapterTest.java"
mkf "$INFRA_RES/db/migration/V1__create_i18n_messages.sql"

# =============================================================================
# MODULE 4 — SPRING BOOT STARTER POM
# =============================================================================
mkhdr "MODULE 4 — i18n-db-ai-spring-boot-starter"

writef "i18n-db-ai-spring-boot-starter/pom.xml" << 'EOF'
<!-- starter pom.xml content -->
EOF

mkf "$STARTER_SRC/I18nDbAutoConfig.java"
mkf "$STARTER_RES/META-INF/spring.factories"
mkf "$STARTER_TST/I18nDbAutoConfigTest.java"

echo ""
echo "✅ Skeleton created successfully at $ROOT"
