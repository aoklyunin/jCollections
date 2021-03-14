package com.github.aoklyunin.jCollections.combiners.ranges.primitive;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Objects;


/**
 * Integer диапазон
 */
public class LongRange extends PrimitiveRange {

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
    public LongRange(
            @NotNull @JsonProperty("min") Long min, @NotNull @JsonProperty("max") Long max,
            @Nullable @JsonProperty("stepCnt") Integer stepCnt,
            @Nullable @JsonProperty("name") String name, @Nullable @JsonProperty("enabled") Boolean enabled,
            @Nullable @JsonProperty("canRepeatValue") Boolean canRepeatValue
    ) {
        super(
                Objects.requireNonNull(min), Objects.requireNonNull(max),
                Objects.requireNonNullElse(stepCnt, (int) (max - min)), name, enabled, canRepeatValue
        );
        this.size = max - min;
        this.step = (long) this.size / this.stepCnt;
        if (min >= max)
            throw new AssertionError(this + " min>=max");
        if (this.stepCnt > (long) this.size)
            throw new AssertionError(this + " stepCnt>=size: step is zero");
        setCurrentValue(min);
    }

    /**
     * Конструктор символьного диапазона
     *
     * @param range интервал
     */
    public LongRange(@NotNull LongRange range) {
        super(Objects.requireNonNull(range));
    }

    /**
     * Строковое представление диапазона
     *
     * @return строковое представление диапазона
     */
    @Override
    public String toString() {
        return "LongRange{" + getString() + "}";
    }
}
