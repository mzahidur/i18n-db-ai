package io.github.mzahidur.i18n.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class I18nMessageTest {

    @Test
    @DisplayName("Should create basic message from DB")
    void shouldCreateBasicMessage() {
        I18nMessage msg = I18nMessage.create("user.welcome", "en", "Welcome!");

        assertThat(msg.getCode()).isEqualTo("user.welcome");
        assertThat(msg.getLocale()).isEqualTo("en");
        assertThat(msg.getMessage()).isEqualTo("Welcome!");
        assertThat(msg.getSource()).isEqualTo(TranslationSource.DB);
        assertThat(msg.isAiGenerated()).isFalse();
        assertThat(msg.getTenantId()).isNull();
    }

    @Test
    @DisplayName("Should create tenant-specific message")
    void shouldCreateForTenant() {
        I18nMessage msg = I18nMessage.createForTenant("user.welcome", "ms-MY", "Selamat datang!", "tenant-123");

        assertThat(msg.getTenantId()).isEqualTo("tenant-123");
        assertThat(msg.getSource()).isEqualTo(TranslationSource.DB);
    }

    @Test
    @DisplayName("Should create AI-generated message")
    void shouldCreateFromAi() {
        I18nMessage msg = I18nMessage.createFromAi("greeting", "fr", "Bonjour!", null);

        assertThat(msg.isAiGenerated()).isTrue();
        assertThat(msg.getSource()).isEqualTo(TranslationSource.AI);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("Should reject blank code or locale")
    void shouldRejectBlankCodeOrLocale(String invalid) {
        assertThatThrownBy(() -> I18nMessage.create(invalid, "en", "Hello"))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> I18nMessage.create("hello", invalid, "Hello"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Should reject null message")
    void shouldRejectNullMessage() {
        assertThatThrownBy(() -> I18nMessage.create("key", "en", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Equals and hashCode should be based on business key")
    void testEqualsAndHashCode() {
        I18nMessage m1 = I18nMessage.create("key", "en", "Hello");
        I18nMessage m2 = I18nMessage.create("key", "en", "Different");

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
    }

    @Test
    @DisplayName("Update message should change content and source")
    void shouldUpdateMessage() {
        I18nMessage msg = I18nMessage.create("key", "en", "Old");

        msg.updateMessage("New text", TranslationSource.DB);

        assertThat(msg.getMessage()).isEqualTo("New text");
        assertThat(msg.getSource()).isEqualTo(TranslationSource.DB);
    }
}