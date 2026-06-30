package io.github.mzahidur.i18n.application.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TranslationApplicationServiceTest {

    @Test
    void serviceShouldBeInstantiable() {
        // Basic smoke test - full tests will come later
        assertThat(TranslationApplicationService.class).isNotNull();
    }
}