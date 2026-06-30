package io.github.mzahidur.i18n.starter;

import io.github.mzahidur.i18n.starter.config.I18nDbAutoConfig;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class I18nDbAutoConfigTest {

    @Test
    void configClassExists() {
        assertThat(I18nDbAutoConfig.class).isNotNull();
    }
}