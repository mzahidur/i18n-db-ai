package io.github.mzahidur.i18n.infra.adapter;

import io.github.mzahidur.i18n.domain.model.I18nMessage;
import io.github.mzahidur.i18n.domain.model.TranslationSource;
import io.github.mzahidur.i18n.infra.adapter.jpa.I18nMessageJpaRepository;
import io.github.mzahidur.i18n.infra.adapter.jpa.JpaTranslationRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class JpaTranslationRepositoryAdapterTest {

    @Autowired
    private I18nMessageJpaRepository jpaRepository;

    private JpaTranslationRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaTranslationRepositoryAdapter(jpaRepository);
        jpaRepository.deleteAll();
    }

    // ... (rest of the test methods remain the same)
}