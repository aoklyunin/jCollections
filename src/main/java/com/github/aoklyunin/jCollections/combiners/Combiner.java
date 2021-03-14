package com.github.aoklyunin.jCollections.combiners;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.aoklyunin.jCollections.combiners.ranges.EmptyRange;
import com.github.aoklyunin.jCollections.combiners.ranges.Range;
import com.github.aoklyunin.jCollections.combiners.ranges.RangeBuilder;
import com.github.aoklyunin.jCollections.combiners.ranges.complex.CombinerRange;
import com.github.aoklyunin.jCollections.combiners.ranges.complex.ListRange;
import com.github.aoklyunin.jCollections.combiners.ranges.primitive.*;
import com.github.aoklyunin.jCollections.combiners.ranges.vector.Vector2iRange;
import com.github.aoklyunin.jCollections.combiners.ranges.vector.Vector3dRange;
import org.jetbrains.annotations.NotNull;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Класс комбайнера(перебирает все комбинации имеют по одному любому значению из каждого диапазона)
 * Каждой комбинации соответствует её номер.
 */
public class Combiner {

    /**
     * Количество комбинаций
     */
    @NotNull
    @JsonIgnore
    BigInteger combinationCnt;
    /**
     * Текущее положение в переборе комбинаций
     */
    @NotNull
    @JsonIgnore
    BigInteger combinationLoopPos;
    /**
     * список диапазонов значений
     */
    @NotNull
    protected List<Range> ranges;
    /**
     * словарь соответствийназванийдиапазонов и их номеров
     */
    @NotNull
    private Map<String, Integer> rangeDict;

    /**
     * Конструктор хранителя интервалов
     *
     * @param ranges список интервалов
     */
    @JsonCreator
    public Combiner(@NotNull @JsonProperty("ranges") List<Range> ranges) {
        setRanges(Objects.requireNonNull(ranges));
    }

    /**
     * Конструктор хранителя интервалов
     *
     * @param rangeCnt количество интервалов(задаётся точное число для того, чтобы кадлый интервал был строго
     *                 под указанным номерои и чтобы это контроллировать)
     */
    public Combiner(int rangeCnt) {
        this.rangeDict = new HashMap<>();
        ranges = new ArrayList<>();
        for (int i = 0; i < rangeCnt; i++) {
            ranges.add(new RangeBuilder().enabled(false).build());
            rangeDict.put(ranges.get(i).getName(), i);
        }
        initCombinationLoop();
    }

    /**
     * Конструктор хранителя интервалов
     *
     * @param ranges список интервалов
     */
    public Combiner(Range... ranges) {
        setRanges(Arrays.asList(ranges));
    }


    /**
     * Конструктор хранителя интервалов
     *
     * @param rangeCnt       количество интервалов(задаётся точное число для того, чтобы кадлый интервал был строго
     *                       под указанным номерои и чтобы это контроллировать)
     * @param sourceCombiner комбайнер, на основе которого будет построен новый
     */
    public Combiner(@NotNull Combiner sourceCombiner, int rangeCnt) {
        ranges = new ArrayList<>();
        this.rangeDict = new HashMap<>();
        for (int i = 0; i < Math.min(rangeCnt, sourceCombiner.ranges.size()); i++) {
            ranges.add(copyRange(sourceCombiner.ranges.get(i)));
            rangeDict.put(sourceCombiner.ranges.get(i).getName(), i);
        }
        for (int i = sourceCombiner.ranges.size(); i < rangeCnt; i++) {
            ranges.add(new RangeBuilder().enabled(false).build());
            rangeDict.put(ranges.get(i).getName(), i);
        }
        initCombinationLoop();
    }

    /**
     * Задать диапазоны
     *
     * @param lst список диапазонов
     */
    @JsonIgnore
    public void setRanges(@NotNull List<Range> lst) {
        this.ranges = new ArrayList<>();
        this.ranges.addAll(lst);
        this.rangeDict = new HashMap<>();
        for (int i = 0; i < lst.size(); i++) {
            rangeDict.put(ranges.get(i).getName(), i);
        }
        initCombinationLoop();
    }


    /**
     * Конструктор хранителя интервалов
     *
     * @param sourceCombiner комбайнер, на основе которого будет построен новый
     */
    public Combiner(@NotNull Combiner sourceCombiner) {
        this(sourceCombiner, sourceCombiner.ranges.size());
    }


    /**
     * инициализировать переборщик
     */
    public void initCombinationLoop() {
        combinationLoopPos = BigInteger.ZERO;
        calculateCombinationCnt();
    }

    /**
     * Рассчитать количество комбинаций
     */
    protected void calculateCombinationCnt() {
        combinationCnt = conv(getMax()).add(BigInteger.ONE);
    }


    /**
     * Получить следующую комбинацию (последний элемент - это номер комбинации)
     *
     * @return следующая комбинация, если она есть, null, если комбинации закончились
     */
    @NotNull
    @JsonIgnore
    public synchronized List<Object> getNextAsList() {
        // System.out.println(Thread.currentThread().getName()+" "+"getNext from combiner "+combinationLoopPos);
        // нужно переходить к следующему
        if (this.combinationLoopPos.compareTo(combinationCnt) >= 0)
            return new ArrayList<>();
        List<Object> objects = deconv(combinationLoopPos);
        objects.add(combinationLoopPos);
        combinationLoopPos = combinationLoopPos.add(BigInteger.ONE);
        // System.out.println(Thread.currentThread().getName()+" "+"getNext from end");
        return new ArrayList<>(objects);
    }

    /**
     * Получить следующую комбинацию (последний элемент - это номер комбинации)
     *
     * @return следующая комбинация, если она есть, null, если комбинации закончились
     */
    @NotNull
    @JsonIgnore
    public synchronized Map<String, Object> getNextAsDict() {
        // System.out.println(Thread.currentThread().getName()+" "+"getNext from combiner "+combinationLoopPos);
        // нужно переходить к следующему

        Map<String, Object> map = new HashMap<>();
        if (this.combinationLoopPos.compareTo(combinationCnt) >= 0)
            return map;
        List<Object> objects = deconv(combinationLoopPos);
        for (int i = 0; i < ranges.size(); i++) {
            map.put(ranges.get(i).getName(), objects.get(i));
        }
        map.put("combinationLoopPos", combinationLoopPos);
        combinationLoopPos = combinationLoopPos.add(BigInteger.ONE);
        // System.out.println(Thread.currentThread().getName()+" "+"getNext from end");
        return map;
    }

    /**
     * Преобразование номер комбинации в комбинацию
     *
     * @param combinationNumber номер комбинации
     * @return следующая комбинация
     */
    @NotNull
    public List<Object> deconv(@NotNull BigInteger combinationNumber) {
        List<Object> res = new LinkedList<>();
        for (Range range : ranges) {
            if (!range.isEmpty()) {
                if (!range.isEnabled())
                    res.add(range.getCurrentValue());
                else {
                    combinationNumber = range.pullFrom(combinationNumber, res);
                }
            } else
                res.add(null);
        }
        return res;
    }

    /**
     * Преобразование номер комбинации в комбинацию
     *
     * @param combinationNumber номер комбинации
     * @return следующая комбинация
     */
    @NotNull
    public LinkedList<Object> deconv(int combinationNumber) {
        LinkedList<Object> res = new LinkedList<>();
        for (Range range : ranges) {
            if (!range.isEmpty())
                if (!range.isEnabled())
                    res.add(range.getCurrentValue());
                else {
                    combinationNumber = range.pullFrom(combinationNumber, res);
                }
        }
        return res;
    }

    /**
     * Узнать номер комбинации по её значению
     *
     * @param combination комбинация
     * @return номер комбинации
     */
    @NotNull
    public BigInteger conv(@NotNull List<Object> combination) {
        BigInteger value = BigInteger.valueOf(0);
        Iterator it = new LinkedList<>(combination).descendingIterator();
        for (int i = 0; i < ranges.size(); i++) {
            Range range = ranges.get(ranges.size() - i - 1);
            if (!range.isEmpty()) {
                Object object = it.next();
                if (range.isEnabled()) {
                    value = range.pushTo(value, object);
                }
            }
        }
        return value;
    }

    /**
     * Узнать номер комбинации по её значению
     *
     * @param combination комбинация
     * @return номер комбинации
     */
    public int simpleConv(@NotNull List<?> combination) {
        int value = 0;
        Iterator it = new LinkedList<>(Objects.requireNonNull(combination)).descendingIterator();
        for (int i = 0; i < ranges.size(); i++) {
            Range range = ranges.get(ranges.size() - i - 1);
            if (!range.isEmpty()) {
                Object object = it.next();
                if (range.isEnabled()) {
                    value = range.pushTo(value, object);
                }
            }
        }
        return value;
    }

    /**
     * Получить комбинацию по умолчанию
     *
     * @return комбинация по умолчанию
     */
    @NotNull
    @JsonIgnore
    public LinkedList<Object> getDefaultCombinationList() {
        LinkedList<Object> values = new LinkedList<>();
        for (Range range : ranges) {
            if (!range.isEmpty())
                values.add(range.getCurrentValue());
        }
        return values;
    }

    /**
     * Получить комбинацию по умолчанию
     *
     * @return комбинация по умолчанию
     */
    @NotNull
    @JsonIgnore
    public Map<String, Object> getDefaultCombinationAsMap() {
        Map<String, Object> values = new HashMap<>();
        for (Range range : ranges) {
            if (!range.isEmpty())
                values.put(range.getName(), range.getCurrentValue());
        }
        return values;
    }

    /**
     * Задать значение по умолчанию
     *
     * @param num          номер интервала
     * @param defaultValue значение оп умолчанию
     */
    public void setDefaultValue(int num, @NotNull Object defaultValue) {
        ranges.get(num).setCurrentValue(Objects.requireNonNull(defaultValue));
    }

    /**
     * Задать значение по умолчанию
     *
     * @param name         название диапазона
     * @param defaultValue значение оп умолчанию
     */
    public void setDefaultValue(@NotNull String name, @NotNull Object defaultValue) {
        ranges.get(rangeDict.get(Objects.requireNonNull(name))).setCurrentValue(Objects.requireNonNull(defaultValue));
    }

    /**
     * Получить значение по умолчанию
     *
     * @param num номер интервала
     * @return значение по умолчанию
     */
    @NotNull
    @JsonIgnore
    public Object getDefaultValue(int num) {
        return ranges.get(num).getCurrentValue();
    }

    /**
     * Изменить значение по умолчанию на один шаг для интервала
     *
     * @param num     номер интервала
     * @param flgNext флаг, нужно ли смещаться вправо
     * @return флаг, получилось ли сместить значение по умолчанию
     */
    public boolean changeDefaultValue(int num, boolean flgNext) {
        return ranges.get(num).changeCurrentValue(flgNext);
    }

    /**
     * Изменить значение по умолчанию на один шаг для интервала
     *
     * @param name    название интервала
     * @param flgNext флаг, нужно ли смещаться вправо
     * @return флаг, получилось ли сместить значение по умолчанию
     */
    public boolean changeDefaultValue(@NotNull String name, boolean flgNext) {
        return ranges.get(rangeDict.get(Objects.requireNonNull(name))).changeCurrentValue(flgNext);
    }

    /**
     * Задать случайное значение по умолчанию для интервала
     *
     * @param num номер интервала
     */
    public void setRandomRangeValue(int num) {
        ranges.get(num).setRandomCurrentValue();
    }

    /**
     * Задать случайное значение по умолчанию для интервала
     *
     * @param name название интервала
     */
    public void setRandomRangeValue(@NotNull String name) {
        ranges.get(rangeDict.get(Objects.requireNonNull(name))).setRandomCurrentValue();
    }

    /**
     * Задать случайное значение из заданного интервала
     *
     * @param num номер интервала
     * @return случайное значение из заданного интервала
     */
    @NotNull
    @JsonIgnore
    public Object getRandomValue(int num) {
        return ranges.get(num).getRandomValue();
    }

    /**
     * Задать случайное значение из заданного интервала
     *
     * @param name название интервала
     * @return случайное значение из заданного интервала
     */
    @NotNull
    @JsonIgnore
    public Object getRandomValue(@NotNull String name) {
        return ranges.get(rangeDict.get(Objects.requireNonNull(name))).getRandomValue();
    }

    /**
     * Получить случайный номер разрешённого интервала
     *
     * @return случайный номер разрешённого интервала
     */
    @JsonIgnore
    public int getRandomEnabledParamNum() {
        LinkedList<Integer> enabledRangeNumbers = getEnabledRangeNumbers();
        if (enabledRangeNumbers.size() == 0) {
            throw new AssertionError("there are no enabled params");
        }
        return enabledRangeNumbers.get(ThreadLocalRandom.current().nextInt(enabledRangeNumbers.size()));
    }

    /**
     * Получить случайный номер разрешённого интервала
     *
     * @return случайный номер разрешённого интервала
     */
    @NotNull
    @JsonIgnore
    public String getRandomEnabledParamName() {
        int num = getRandomEnabledParamNum();
        return ranges.get(num).getName();
    }

    /**
     * Получить список номеров разрешённых интервалов
     *
     * @return список номеров разрешённых интервалов
     */
    @NotNull
    @JsonIgnore
    public LinkedList<Integer> getEnabledRangeNumbers() {
        // создаём список разрешённых для мутаций параметров
        LinkedList<Integer> enabledRangeNumbers = new LinkedList<>();
        for (int i = 0; i < ranges.size(); i++) {
            if (ranges.get(i).isEnabled())
                enabledRangeNumbers.add(i);
        }
        return enabledRangeNumbers;
    }

    /**
     * Получить минимальную комбинацию
     *
     * @return минимальная комбинация
     */
    @NotNull
    @JsonIgnore
    public LinkedList<Object> getMin() {
        LinkedList<Object> values = new LinkedList<>();
        for (Range range : ranges)
            if (!range.isEmpty())
                if (range.isEnabled())
                    values.add(range.getMin());
                else
                    values.add(range.getCurrentValue());
        return values;
    }

    /**
     * Получить максимальную комбинацию
     *
     * @return максимальная комбинация
     */
    @NotNull
    @JsonIgnore
    public LinkedList<Object> getMax() {
        LinkedList<Object> values = new LinkedList<>();
        for (Range range : ranges) {
            if (!range.isEmpty())
                if (range.isEnabled())
                    values.add(range.getMax());
                else
                    values.add(range.getCurrentValue());
        }
        return values;
    }

    /**
     * Задать диапазон
     *
     * @param num   номердиапазона
     * @param range диапазон
     */
    public void setRange(int num, @NotNull Range range) {
        ranges.set(num, Objects.requireNonNull(range));
        initCombinationLoop();
    }

    /**
     * Получить кол-во комбинаций
     *
     * @return кол-во комбинаций
     */
    @NotNull
    public BigInteger getCombinationCnt() {
        return combinationCnt;
    }

    /**
     * Получить Текущее положение в переборе комбинаций
     *
     * @return Текущее положение в переборе комбинаций
     */
    @NotNull
    public BigInteger getCombinationLoopPos() {
        return combinationLoopPos;
    }

    /**
     * Получить список диапазонов значений
     *
     * @return список диапазонов значений
     */
    @NotNull
    public List<Range> getRanges() {
        return ranges;
    }

    /**
     * Получить строковое представление диапазонов
     *
     * @return строковое представление диапазонов
     */
    @NotNull
    @JsonIgnore
    public String getRangesString() {
        StringBuilder rangesStr = new StringBuilder("{\n ");
        for (Range range : ranges) {
            rangesStr.append(range).append("\n ");
        }
        return rangesStr.toString();
    }

    /**
     * Строковое представление объекта вида:
     *
     * @return "Combiner{getString()}"
     */
    @Override
    public String toString() {
        return "Combiner{" + getString() + '}';
    }

    /**
     * Строковое представление объекта вида:
     * "combinationCnt, combinationLoopPos, ranges.size()"
     *
     * @return строковое представление объекта
     */
    protected String getString() {
        return combinationCnt + ", " + combinationLoopPos + ", " + ranges.size();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Combiner combiner = (Combiner) o;

        if (!Objects.equals(combinationCnt, combiner.combinationCnt))
            return false;
        if (!Objects.equals(combinationLoopPos, combiner.combinationLoopPos))
            return false;
        if (!Objects.equals(ranges, combiner.ranges)) return false;
        return Objects.equals(rangeDict, combiner.rangeDict);
    }

    @Override
    public int hashCode() {
        int result = combinationCnt.hashCode();
        result = 31 * result + combinationLoopPos.hashCode();
        result = 31 * result + ranges.hashCode();
        result = 31 * result + rangeDict.hashCode();
        return result;
    }

    /**
     * Копировать диапазон
     *
     * @param sourceRange диапазон, который надо скопировать
     * @return новый диапазон-копия
     */
    public static Range copyRange(Range sourceRange) {
        if (sourceRange.getClass().equals(CombinerRange.class))
            return new CombinerRange((CombinerRange) sourceRange);
        if (sourceRange.getClass().equals(ListRange.class))
            return new ListRange((ListRange) sourceRange);
        if (sourceRange.getClass().equals(CharRange.class))
            return new CharRange((CharRange) sourceRange);
        if (sourceRange.getClass().equals(DoubleRange.class))
            return new DoubleRange((DoubleRange) sourceRange);
        if (sourceRange.getClass().equals(FloatRange.class))
            return new FloatRange((FloatRange) sourceRange);
        if (sourceRange.getClass().equals(IntRange.class))
            return new IntRange((IntRange) sourceRange);
        if (sourceRange.getClass().equals(LongRange.class))
            return new LongRange((LongRange) sourceRange);
        if (sourceRange.getClass().equals(Vector2iRange.class))
            return new Vector2iRange((Vector2iRange) sourceRange);
        if (sourceRange.getClass().equals(Vector3dRange.class))
            return new Vector3dRange((Vector3dRange) sourceRange);
        if (sourceRange.getClass().equals(EmptyRange.class))
            return new EmptyRange(sourceRange);
        throw new IllegalArgumentException("copyRange(): unexpected class " + sourceRange.getClass());
    }

}
