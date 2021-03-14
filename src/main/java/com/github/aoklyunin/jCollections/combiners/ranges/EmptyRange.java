package com.github.aoklyunin.jCollections.combiners.ranges;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.util.Objects;


/**
 * Пустой интервал
 */
public class EmptyRange extends Range {

    /**
     * Конструктор базового диапазона
     *
     * @param name           название диапазона
     * @param enabled        флаг, разрешён ли диапазон
     * @param canRepeatValue флаг, могут ли повторяться значения интервала
     */
    @JsonCreator
    public EmptyRange(
            @Nullable @JsonProperty("name") String name, @Nullable @JsonProperty("enabled") Boolean enabled,
            @Nullable @JsonProperty("canRepeatValue") Boolean canRepeatValue
    ) {
        super(name, null, enabled, canRepeatValue);
    }

    /**
     * Конструктор пустого интервала
     *
     * @param range интервал, на основе которого создаётся новый
     */
    public EmptyRange(@NotNull Range range) {
        super(Objects.requireNonNull(range));
    }


    /**
     * Получить значение по  номеру шага
     *
     * @param stepNum номер шага
     * @return значение интервала
     */
    @NotNull
    @Override
    public Object getValue(int stepNum) {
        return null;
    }

    /**
     * Получить номер шага по значению
     *
     * @param object значение
     * @return номер шага
     */
    @Override
    public int getStepNum(@NotNull Object object) {
        return 0;
    }

    /**
     * Получить  максимум
     *
     * @return максимум
     */
    @NotNull
    @Override
    @JsonIgnore
    public Object getMax() {
        return null;
    }

    /**
     * Получить минимум
     *
     * @return минимум
     */
    @NotNull
    @Override
    @JsonIgnore
    public Object getMin() {
        return null;
    }

    /**
     * Проверка, является ли диапазон пустым
     *
     * @return флаг, является ли диапазон пустым
     */
    @Override
    public String toString() {
        return "EmptyRange{" + getString() + "}";
    }

}
