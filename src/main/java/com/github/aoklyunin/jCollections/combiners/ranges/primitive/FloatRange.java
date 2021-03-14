package com.github.aoklyunin.jCollections.combiners.ranges.primitive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Objects;

/**
 * Вещественный диапазон
 */
public class FloatRange extends PrimitiveRange {
    /**
     * Конструктор диапазона примитивного типа
     *
     * @param min            минимальное значение
     * @param max            максимальное значение
     * @param stepCnt        кол-во шагов диапазона
     * @param name           название диапазона
     * @param enabled        флаг, разрешено ли изменение значения интервала
     * @param canRepeatValue флаг, могут ли повторяться значения интервала
     */
    @JsonCreator
    public FloatRange(
            @NotNull @JsonProperty("min") Float min, @NotNull @JsonProperty("max") Float max,
            @Nullable @JsonProperty("stepCnt") Integer stepCnt,
            @Nullable @JsonProperty("name") String name, @Nullable @JsonProperty("enabled") Boolean enabled,
            @Nullable @JsonProperty("canRepeatValue") Boolean canRepeatValue
    ) {
        super(Objects.requireNonNull(min), Objects.requireNonNull(max), stepCnt, name, enabled, canRepeatValue);
        this.size = max - min;
        this.step = (float) this.size / this.stepCnt;
        if (min >= max)
            throw new AssertionError(this + " min>=max");
        setCurrentValue(min);
    }

    /**
     * Конструктор символьного диапазона
     *
     * @param range интервал
     */
    public FloatRange(@NotNull FloatRange range) {
        super(Objects.requireNonNull(range));
    }

    /**
     * Строковое представление диапазона
     *
     * @return строковое представление диапазона
     */
    @Override
    public String toString() {
        return "FloatRange{" + getString() + "}";
    }

}
