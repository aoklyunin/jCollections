package com.github.aoklyunin.jCollections.combiners.ranges;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Базовый класс диапазона
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "@class")
public abstract class Range {
    /**
     * Количество созданных безымянных диапазонов
     */
    @JsonIgnore
    private static int createdNoNameRangeCnt = 0;
    /**
     * Флаг, разрешён ли диапазон
     */
    private final boolean enabled;
    /**
     * Могут ли повторяться значения интервала
     */
    private final boolean canRepeatValue;
    /**
     * Значение диапазона по умолчанию
     */
    @NotNull
    @JsonIgnore
    private Object currentValue;
    /**
     * Количество шагов диапазона
     */
    protected int stepCnt;
    /**
     * Текущий номер шага диапазона
     */
    @JsonIgnore
    private int currentStepNum;
    /**
     * Кол-во шагов диапазона
     */
    @NotNull
    @JsonIgnore
    protected Object size;
    /**
     * Имя диапазона
     */
    @NotNull
    private final String name;


    /**
     * Конструктор базового диапазона
     *
     * @param name           название диапазона
     * @param stepCnt        кол-во шагов диапазона
     * @param enabled        флаг, разрешён ли диапазон
     * @param canRepeatValue флаг, могут ли повторяться значения интервала
     */
    @JsonCreator
    public Range(
            @Nullable @JsonProperty("name") String name, @Nullable @JsonProperty("stepCnt") Integer stepCnt,
            @Nullable @JsonProperty("enabled") Boolean enabled,
            @Nullable @JsonProperty("canRepeatValue") Boolean canRepeatValue
    ) {
        //System.out.println(name);
        this.enabled = Objects.requireNonNullElse(enabled, true);
        this.canRepeatValue = Objects.requireNonNullElse(canRepeatValue, true);
        this.name = Objects.requireNonNullElseGet(name, () -> getClass() + " " + (createdNoNameRangeCnt++));
        this.stepCnt = Objects.requireNonNullElse(stepCnt, 1);
    }

    /**
     * Конструктор базового диапазона
     *
     * @param range базовый диапазон
     */
    public Range(@NotNull Range range) {
        this.enabled = range.enabled;
        this.canRepeatValue = range.canRepeatValue;
        this.name = range.name;
        this.stepCnt = range.stepCnt;
        this.size = range.size;
        this.currentValue = range.currentValue;
        this.currentStepNum = range.currentStepNum;
    }

    /**
     * Проверка, является ли диапазон пустым
     *
     * @return флаг, является ли диапазон пустым
     */
    @JsonIgnore
    public boolean isEmpty() {
        return this.getClass().equals(EmptyRange.class);
    }

    /**
     * Получить значение по  номеру шага
     *
     * @param stepNum номер шага
     * @return значение интервала
     */
    @NotNull
    public abstract Object getValue(int stepNum);

    /**
     * Получить номер шага по значению
     *
     * @param object значение
     * @return номер шага
     */
    public abstract int getStepNum(@NotNull Object object);

    /**
     * Получить флаг, разрешён ли этот интервал
     *
     * @return флаг, разрешён ли этот интервал
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Получить флаг, может ли значение повторяться
     *
     * @return флаг, может ли значение повторяться
     */
    public boolean isCanRepeatValue() {
        return canRepeatValue;
    }

    /**
     * Получить текущее значение интервала
     *
     * @return текущее значение интервала
     */
    @NotNull
    public Object getCurrentValue() {
        return currentValue;
    }

    /**
     * Получить название интервала
     *
     * @return название интервала
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Получить  максимум
     *
     * @return максимум
     */
    @NotNull
    public abstract Object getMax();

    /**
     * Получить минимум
     *
     * @return минимум
     */
    @NotNull
    public abstract Object getMin();

    /**
     * Получить кол-во шагов диапазона
     *
     * @return кол-во шагов диапазона
     */
    public int getStepCnt() {
        return stepCnt;
    }

    /**
     * Получить значение из свёртки
     *
     * @param value     свёртка из которого получается значение нового элемента развёртки
     *                  как остаток от деления числа на stepCnt+1
     * @param deconvArr массив развёрнутых значений
     * @return свёртка с извлечённым значением диапазона
     */
    @NotNull
    public BigInteger pullFrom(@NotNull BigInteger value, @NotNull List<Object> deconvArr) {
        deconvArr.add(getValue(value.mod(BigInteger.valueOf((stepCnt + 1))).intValue()));
        return value.divide(BigInteger.valueOf((stepCnt + 1)));
    }

    /**
     * Получить значение из свёртки
     *
     * @param value     свёртка из которого получается значение нового элемента развёртки
     *                  как остаток от деления числа на stepCnt+1
     * @param deconvArr массив развёрнутых значений
     * @return свёртка с извлечённым значением диапазона
     */
    public int pullFrom(int value, @NotNull List<Object> deconvArr) {
        deconvArr.add(getValue(value % (stepCnt + 1)));
        return value / (stepCnt + 1);
    }

    /**
     * Добавить значение в свёртку
     *
     * @param value  свёртка в которую нужно добавить значение, его нужно умножить на stepCnt+1 и прибавить номер
     *               текущего шага
     * @param object значение, которое нужно добавить в свёртку
     * @return новая свёртка
     */
    public int pushTo(int value, @NotNull Object object) {
        return value * (stepCnt + 1) + getStepNum(Objects.requireNonNull(object));
    }

    /**
     * Добавить значение в свёртку
     *
     * @param value  свёртка в которую нужно добавить значение, его нужно умножить на stepCnt+1 и прибавить номер
     *               текущего шага
     * @param object значение, которое нужно добавить в свёртку
     * @return новая свёртка
     */
    public BigInteger pushTo(@NotNull BigInteger value, @NotNull Object object) {
        return value.multiply(BigInteger.valueOf((stepCnt + 1)))
                .add(BigInteger.valueOf(getStepNum(Objects.requireNonNull(object))));
    }

    /**
     * Поменять значение по умолчанию
     *
     * @param flgNext флаг:true - взять следующее значение, false - предыдущий
     * @return флаг, получилось поменять занчение по умолчанию
     */
    public boolean changeCurrentValue(boolean flgNext) {
        if (flgNext)
            return setCurrentStep(currentStepNum + 1);
        else
            return setCurrentStep(currentStepNum - 1);
    }

    /**
     * Задать значение по умолчанию
     *
     * @param object новое значение
     * @return флаг, получилось поменять занчение по умолчанию
     */
    public boolean setCurrentValue(@NotNull Object object) {
        //System.out.println("setCurrentValue "+object);
        int newStepNum = getStepNum(Objects.requireNonNull(object));
        if (newStepNum < 0) {
            currentStepNum = newStepNum;
            currentValue = getMin();
            return false;
        } else if (newStepNum >= stepCnt) {
            currentStepNum = stepCnt;
            currentValue = getMax();
            return false;
        } else {
            currentStepNum = newStepNum;
            currentValue = object;
            return true;
        }
    }

    /**
     * Задать текущий шаг
     *
     * @param stepNum номер шага
     * @return флаг, получилось ли задать номер шага
     */
    public boolean setCurrentStep(int stepNum) {
        return setCurrentValue(getValue(stepNum));
    }

    /**
     * Возвращает случаное значение диапазона
     *
     * @return случаное значение диапазона
     */
    @NotNull
    @JsonIgnore
    public Object getRandomValue() {
        return getValue(ThreadLocalRandom.current().nextInt(stepCnt));
    }

    /**
     * Задать случайное значение по умолчанию
     */
    public void setRandomCurrentValue() {
        setCurrentValue(getRandomValue());
    }

    /**
     * Проверка, является ли диапазон пустым
     *
     * @return флаг, является ли диапазон пустым
     */
    @Override
    public String toString() {
        return "Range{" + getString() + "}";
    }

    /**
     * Строковое представление диапазона
     *
     * @return строковое представление диапазона
     */
    @JsonIgnore
    public String getString() {
        return (enabled ? "+" : "-") + name + ":" + currentValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Range range = (Range) o;

        if (enabled != range.enabled) return false;
        if (canRepeatValue != range.canRepeatValue) return false;
        if (stepCnt != range.stepCnt) return false;
        if (currentStepNum != range.currentStepNum) return false;
        if (!Objects.equals(currentValue, range.currentValue)) return false;
        if (!Objects.equals(size, range.size)) return false;
        return Objects.equals(name, range.name);
    }

    @Override
    public int hashCode() {
        int result = (enabled ? 1 : 0);
        result = 31 * result + (canRepeatValue ? 1 : 0);
        result = 31 * result + (currentValue != null ? currentValue.hashCode() : 0);
        result = 31 * result + stepCnt;
        result = 31 * result + currentStepNum;
        result = 31 * result + (size != null ? size.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }


    /**
     * Фабрика символьного диапазона
     *
     * @param min     минимальное значение
     * @param max     максимальное значение
     * @param stepCnt кол-во шагов диапазона, если передаётся ноль, то кол-во задаётся равным
     *                разнице между максимумом и минимумом
     * @return новый диапазон
     */
    public static Range of(Object min, Object max, int stepCnt) {
        return new RangeBuilder().setMinMax(min, max).setStepCnt(stepCnt).build();
    }

    /**
     * Фабрика символьного диапазона
     *
     * @param min     минимальное значение
     * @param max     максимальное значение
     * @param stepCnt кол-во шагов диапазона, если передаётся ноль, то кол-во задаётся равным
     *                разнице между максимумом и минимумом
     * @param enabled разрешён ли диапазон
     * @return новый диапазон
     */
    public static Range of(Object min, Object max, int stepCnt, boolean enabled) {
        return new RangeBuilder().setMinMax(min, max).setStepCnt(stepCnt).enabled(enabled).build();
    }

    /**
     * Фабрика символьного диапазона
     *
     * @param min     минимальное значение
     * @param max     максимальное значение
     * @param enabled флаг, разрешено ли изменение значения интервала
     * @return новый диапазон
     */
    public static Range of(Object min, Object max, boolean enabled) {
        return new RangeBuilder().setMinMax(min, max).enabled(enabled).build();
    }

    /**
     * Фабрика символьного диапазона
     *
     * @param min минимальное значение
     * @param max максимальное значение
     * @return новый диапазон
     */
    public static Range of(Object min, Object max) {
        return new RangeBuilder().setMinMax(min, max).build();
    }
}
