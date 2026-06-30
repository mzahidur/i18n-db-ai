package io.github.mzahidur.i18n.application.service.provider;

import io.github.mzahidur.i18n.domain.model.TranslationSource;
import io.github.mzahidur.i18n.domain.port.AiTranslationPort;
import io.github.mzahidur.i18n.domain.port.TranslationRepositoryPort;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

/**
 * Third (last-resort) provider in the fallback chain.
 *
 * <p>Calls {@link AiTranslationPort} to generate a translation on-the-fly.
 * When {@code storeResult} is {@code true} (the default) the AI-generated
 * string is persisted to the DB via {@link TranslationRepositoryPort} so
 * that subsequent requests are served from the fast path.</p>
 *
 * <p>If the AI port returns empty (e.g. it is the {@code NoOpAiTranslationService})
 * this provider also returns empty, and the chain exhaustion handling in
 * {@link TranslationChain} takes over.</p>
 */
public class AiTranslationProvider implements TranslationProvider {

    private static final int ORDER = 30;

    private final AiTranslationPort aiPort;
    private final TranslationRepositoryPort repository;
    private final boolean storeResult;

    /**
     * @param aiPort      AI provider port (may be a {@code NoOpAiTranslationService})
     * @param repository  repository port used to persist AI results
     * @param storeResult when {@code true}, successful AI translations are saved to DB
     */
    public AiTranslationProvider(AiTranslationPort aiPort,
                                 TranslationRepositoryPort repository,
                                 boolean storeResult) {
        this.aiPort = Objects.requireNonNull(aiPort, "aiPort must not be null");
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.storeResult = storeResult;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invokes the AI provider and, if a translation is returned and
     * {@link #storeResult} is enabled, persists it to the database tagged with
     * {@link TranslationSource#AI}.</p>
     */
    @Override
    public Optional<String> resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        Optional<String> aiResult = aiPort.translate(code, locale);

        aiResult.ifPresent(translation -> {
            if (storeResult) {
                repository.save(code, locale, translation, TranslationSource.AI);
            }
        });

        return aiResult;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public String getName() {
        return "AI";
    }
}