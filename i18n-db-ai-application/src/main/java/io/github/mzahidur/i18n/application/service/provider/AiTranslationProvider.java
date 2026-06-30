package io.github.mzahidur.i18n.application.service.provider;

import io.github.mzahidur.i18n.domain.model.I18nMessage;
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
 *
 * <h3>Source locale</h3>
 * <p>{@link AiTranslationPort#translate(String, Locale, Locale, java.util.Map)}
 * requires both a source and a target locale — it translates text written
 * <em>in</em> one locale <em>into</em> another. A message-code lookup,
 * however, only naturally carries the requested target locale; the message
 * {@code code} itself is an identifier, not source-language text.</p>
 *
 * <p>This provider resolves that gap by treating the message {@code code} as
 * a semantic prompt passed in a configurable {@code sourceLocale} (defaulting
 * to {@link Locale#ENGLISH}, matching the convention that message codes are
 * written as English-readable dot-paths, e.g. {@code "user.welcome.title"}).
 * The AI provider implementation is expected to use the code as context for
 * generating an appropriate translation in the target locale, rather than
 * performing a literal text translation of the code string.</p>
 */
public class AiTranslationProvider implements TranslationProvider {

    private static final int ORDER = 30;

    private final AiTranslationPort aiPort;
    private final TranslationRepositoryPort repository;
    private final boolean storeResult;
    private final Locale sourceLocale;

    /**
     * @param aiPort       AI provider port (may be a {@code NoOpAiTranslationService})
     * @param repository   repository port used to persist AI results
     * @param storeResult  when {@code true}, successful AI translations are saved to DB
     * @param sourceLocale the locale the message {@code code} is considered to
     *                     originate from when prompting the AI provider
     */
    public AiTranslationProvider(AiTranslationPort aiPort,
                                 TranslationRepositoryPort repository,
                                 boolean storeResult,
                                 Locale sourceLocale) {
        this.aiPort = Objects.requireNonNull(aiPort, "aiPort must not be null");
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.storeResult = storeResult;
        this.sourceLocale = Objects.requireNonNull(sourceLocale, "sourceLocale must not be null");
    }

    /**
     * Convenience constructor defaulting {@code sourceLocale} to
     * {@link Locale#ENGLISH}, consistent with the convention that message
     * codes are authored as English-readable identifiers.
     *
     * @param aiPort      AI provider port (may be a {@code NoOpAiTranslationService})
     * @param repository  repository port used to persist AI results
     * @param storeResult when {@code true}, successful AI translations are saved to DB
     */
    public AiTranslationProvider(AiTranslationPort aiPort,
                                 TranslationRepositoryPort repository,
                                 boolean storeResult) {
        this(aiPort, repository, storeResult, Locale.ENGLISH);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Invokes the AI provider and, if a translation is returned and
     * {@link #storeResult} is enabled, persists it to the database tagged with
     * {@link io.github.mzahidur.i18n.domain.model.TranslationSource#AI} via
     * {@link I18nMessage#createFromAi(String, String, String, String)}.</p>
     */
    @Override
    public Optional<String> resolve(String code, Locale locale) {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(locale, "locale must not be null");

        Optional<String> aiResult = aiPort.translate(code, sourceLocale, locale);

        aiResult.ifPresent(translation -> {
            if (storeResult) {
                I18nMessage message = I18nMessage.createFromAi(
                        code, locale.toString(), translation, null);
                repository.save(message);
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
