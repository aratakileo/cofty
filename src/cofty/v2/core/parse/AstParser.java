package cofty.v2.core.parse;

import cofty.core.parser.ParseContext;
import cofty.v2.core.ast.AstValue;
import cofty.v2.core.ast.AstValueList;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Optional;

public interface AstParser<R extends AstValue> {
    @NotNull Optional<R> parse(@NotNull ParseContext context);

    static <R extends AstValue, V extends AstParser<R>> @NotNull AstParser<AstValueList<R>> repeated(V parser) {
        return context -> {
            final var parsed = new AstValueList<R>();
            Optional<R> value;

            do {
                value = optional(parser).parse(context);
                value.ifPresent(parsed::add);
            } while (value.isPresent());

            return parsed.isEmpty() ? Optional.empty() : Optional.of(parsed);
        };
    }

    static <R extends AstValue, V extends AstParser<R>> @NotNull AstParser<AstValueList<R>> all(
            @NotNull V parser1,
            @NotNull V parser2,
            @NotNull V... parsers
    ) {
        return context -> {
            final var transaction = context.startTransaction();
            final var parsed1 = optional(parser1).parse(transaction);
            final var parsed2 = optional(parser2).parse(transaction);

            if (parsed1.isEmpty() || parsed2.isEmpty())
                return Optional.empty();

            final var parsed = Arrays.stream(parsers).map(_parser -> optional(_parser).parse(transaction)).toList();

            if (parsed.stream().anyMatch(Optional::isEmpty))
                return Optional.empty();

            transaction.finishTransaction();

            return Optional.of(AstValueList.of(parsed.stream().map(Optional::get).toList()).add(parsed1.get(), parsed2.get()));
        };
    }

    static <R extends AstValue, V extends AstParser<R>> @NotNull AstParser<AstValueList<R>> all(
            @NotNull V parser1,
            @NotNull V parser2
    ) {
        return context -> {
            final var transaction = context.startTransaction();
            final var parsed1 = optional(parser1).parse(transaction);
            final var parsed2 = optional(parser2).parse(transaction);

            if (parsed1.isPresent() && parsed2.isPresent()) {
                transaction.finishTransaction();
                return Optional.of(AstValueList.of(parsed1.get(), parsed2.get()));
            }

            return Optional.empty();
        };
    }

    @SafeVarargs
    @SuppressWarnings("unchecked")
    static @NotNull AstParser<? extends AstValue> any(
            @NotNull AstParser<? extends AstValue> parser1,
            @NotNull AstParser<? extends AstValue> parser2,
            @NotNull AstParser<? extends AstValue>... parsers
    ) {
        return context -> {
            final var parsed1 = optional(parser1).parse(context);

            if (parsed1.isPresent())
                return (Optional<AstValue>) parsed1;

            final var parsed2 = optional(parser2).parse(context);

            if (parsed2.isPresent())
                return (Optional<AstValue>) parsed2;

            return (Optional<AstValue>) Arrays.stream(parsers)
                    .map(_parser -> optional(_parser).parse(context))
                    .filter(Optional::isPresent)
                    .findAny()
                    .orElse(Optional.empty());
        };
    }

    static <R extends AstValue, V extends AstParser<R>> @NotNull AstParser<R> any(
            @NotNull V parser1,
            @NotNull V parser2
    ) {
        return context -> {
            final var parsed1 = optional(parser1).parse(context);

            if (parsed1.isPresent())
                return parsed1;

            return optional(parser2).parse(context);
        };
    }

    static <R extends AstValue, V extends AstParser<R>> @NotNull AstParser<R> optional(@NotNull V parser) {
        return context -> {
            final var transaction = context.startTransaction();
            final var parsed = parser.parse(transaction);

            if (parsed.isPresent()) {
                transaction.finishTransaction();
                return parsed;
            }

            return Optional.empty();
        };
    }
}
