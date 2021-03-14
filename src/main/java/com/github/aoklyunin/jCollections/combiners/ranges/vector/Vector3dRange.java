package com.github.aoklyunin.jCollections.combiners.ranges.vector;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.github.aoklyunin.jCollections.combiners.ranges.Range;
import com.sun.istack.NotNull;
import com.sun.istack.Nullable;
import jMath.aoklyunin.github.com.coordinateSystem.CoordinateSystem3d;
import jMath.aoklyunin.github.com.vector.Vector3d;

import java.util.Objects;

/**
 * Диапазон трёхмерных вещественных векторов, перебирается по свёртке, шаг всегда равен 1
 */
@JsonPropertyOrder({"divideCnt", "min", "max"})
public class Vector3dRange extends Range {
    /**
     * Кол-во делений СК
     */
    private final int divideCnt;
    /**
     * СК диапазона
     */
    @NotNull
    private final CoordinateSystem3d ownCS;
    /**
     * Минимальное значение диапазона
     */
    @NotNull
    public Object min;
    /**
     * Максимальное значение диапазона
     */
    @NotNull
    public Object max;

    /**
     * Конструктор диапазона примитивного типа
     *
     * @param min            минимальное значение
     * @param max            максимальное значение
     * @param name           название интервала
     * @param divideCnt      кол-во делений на каждое измерение
     * @param enabled        флаг, разрешено ли изменение значения интервала
     * @param canRepeatValue флаг, могут ли повторяться значения интервала
     */
    @JsonCreator
    public Vector3dRange(
            @NotNull @JsonProperty("min") Vector3d min, @NotNull @JsonProperty("max") Vector3d max,
            @Nullable @JsonProperty("divideCnt") Integer divideCnt,
            @Nullable @JsonProperty("name") String name, @Nullable @JsonProperty("enabled") Boolean enabled,
            @Nullable @JsonProperty("canRepeatValue") Boolean canRepeatValue
    ) {
        super(name, null, enabled, canRepeatValue);
        this.divideCnt = Objects.requireNonNullElse(divideCnt, 10);
        this.min = new Vector3d(min);
        this.max = new Vector3d(max);
        this.size = Vector3d.subtract(max, min);
        this.ownCS = new CoordinateSystem3d(min, max);
        this.setCurrentValue(min);
        this.stepCnt = ownCS.conv(max, this.divideCnt);
        if (Vector3d.biggerOrEqual(min, max))
            throw new AssertionError(this + " min>=max");
    }

    /**
     * Конструктор диапазона трёхмерных вещественных векторов
     *
     * @param min       минимум СК
     * @param max       максимум СК
     * @param divideCnt Кол-во делений СК
     */
    public Vector3dRange(@NotNull Vector3d min, @NotNull Vector3d max, int divideCnt) {
        this(Objects.requireNonNull(min), Objects.requireNonNull(max), divideCnt, null, true, true);
    }

    /**
     * Конструктор диапазона трёхмерных вещественных векторов
     *
     * @param range     диапазон
     * @param min       минимум СК
     * @param max       максимум СК
     * @param divideCnt Кол-во делений СК
     */
    private Vector3dRange(@NotNull Range range, @NotNull Vector3d min, @NotNull Vector3d max, int divideCnt) {
        super(Objects.requireNonNull(range));
        this.divideCnt = divideCnt;
        this.min = new Vector3d(min);
        this.max = new Vector3d(max);
        this.size = Vector3d.subtract(max, min);
        this.ownCS = new CoordinateSystem3d(min, max);
        this.setCurrentValue(min);
        this.stepCnt = ownCS.conv(max, divideCnt);
    }

    /**
     * Конструктор диапазона двумерных целочисленных векторов
     *
     * @param range интервал
     */
    public Vector3dRange(@NotNull Vector3dRange range) {
        this(range, (Vector3d) range.min, (Vector3d) range.max, range.divideCnt);
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
        return ownCS.deconv(stepNum, divideCnt);
    }

    /**
     * Получить номер шага по значению
     *
     * @param object значение
     * @return номер шага
     */
    @Override
    public int getStepNum(@NotNull Object object) {
        return ownCS.conv((Vector3d) Objects.requireNonNull(object), divideCnt);
    }

    /**
     * Получить  максимум
     *
     * @return максимум
     */
    @NotNull
    @Override
    public Object getMax() {
        return max;
    }

    /**
     * Получить минимум
     *
     * @return минимум
     */
    @NotNull
    @Override
    public Object getMin() {
        return min;
    }

    /**
     * Получить делитель
     *
     * @return делитель
     */
    public int getDivideCnt() {
        return divideCnt;
    }

    /**
     * Строковое представление диапазона
     *
     * @return строковое представление диапазона
     */
    @Override
    public String toString() {
        return "Vector3dRange{" + getString() + "}";
    }

    /**
     * Строковое представление диапазона
     *
     * @return строковое представление диапазона
     */
    @JsonIgnore
    public String getString() {
        return super.getString() + "[" + min + "," + max + "], " + divideCnt;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Vector3dRange that = (Vector3dRange) o;

        if (divideCnt != that.divideCnt) return false;
        if (!Objects.equals(ownCS, that.ownCS)) return false;
        if (!Objects.equals(min, that.min)) return false;
        return Objects.equals(max, that.max);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + divideCnt;
        result = 31 * result + (ownCS != null ? ownCS.hashCode() : 0);
        result = 31 * result + (min != null ? min.hashCode() : 0);
        result = 31 * result + (max != null ? max.hashCode() : 0);
        return result;
    }
}
