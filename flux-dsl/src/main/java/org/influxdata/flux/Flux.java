/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.influxdata.flux;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.influxdata.flux.functions.AbstractParametrizedFlux;
import org.influxdata.flux.functions.CountFlux;
import org.influxdata.flux.functions.CovarianceFlux;
import org.influxdata.flux.functions.DerivativeFlux;
import org.influxdata.flux.functions.DifferenceFlux;
import org.influxdata.flux.functions.DistinctFlux;
import org.influxdata.flux.functions.DropFlux;
import org.influxdata.flux.functions.ExpressionFlux;
import org.influxdata.flux.functions.FilterFlux;
import org.influxdata.flux.functions.FirstFlux;
import org.influxdata.flux.functions.FromFlux;
import org.influxdata.flux.functions.GroupFlux;
import org.influxdata.flux.functions.IntegralFlux;
import org.influxdata.flux.functions.JoinFlux;
import org.influxdata.flux.functions.KeepFlux;
import org.influxdata.flux.functions.LastFlux;
import org.influxdata.flux.functions.LimitFlux;
import org.influxdata.flux.functions.MapFlux;
import org.influxdata.flux.functions.MaxFlux;
import org.influxdata.flux.functions.MeanFlux;
import org.influxdata.flux.functions.MinFlux;
import org.influxdata.flux.functions.RangeFlux;
import org.influxdata.flux.functions.RenameFlux;
import org.influxdata.flux.functions.SampleFlux;
import org.influxdata.flux.functions.SetFlux;
import org.influxdata.flux.functions.ShiftFlux;
import org.influxdata.flux.functions.SkewFlux;
import org.influxdata.flux.functions.SortFlux;
import org.influxdata.flux.functions.SpreadFlux;
import org.influxdata.flux.functions.StddevFlux;
import org.influxdata.flux.functions.SumFlux;
import org.influxdata.flux.functions.ToBoolFlux;
import org.influxdata.flux.functions.ToDurationFlux;
import org.influxdata.flux.functions.ToFloatFlux;
import org.influxdata.flux.functions.ToIntFlux;
import org.influxdata.flux.functions.ToStringFlux;
import org.influxdata.flux.functions.ToTimeFlux;
import org.influxdata.flux.functions.ToUIntFlux;
import org.influxdata.flux.functions.WindowFlux;
import org.influxdata.flux.functions.YieldFlux;
import org.influxdata.flux.functions.properties.OperatorProperties;
import org.influxdata.flux.functions.restriction.Restrictions;
import org.influxdata.platform.Arguments;

/**
 * <a href="https://github.com/influxdata/platform/tree/master/query#basic-syntax">Flux</a> - Data Scripting Language.
 * <br>
 * <a href="https://github.com/influxdata/platform/blob/master/query/docs/SPEC.md">Flux Specification</a>
 *
 * <h3>The operators:</h3>
 * <ul>
 * <li>{@link FromFlux}</li>
 * <li>{@link CountFlux}</li>
 * <li>{@link CovarianceFlux}</li>
 * <li>cumulativeSum - Not defined in documentation or SPEC</li>
 * <li>{@link DerivativeFlux}</li>
 * <li>{@link DifferenceFlux}</li>
 * <li>{@link DistinctFlux}</li>
 * <li>{@link DropFlux}</li>
 * <li>TODO - duplicate</li>
 * <li>{@link FilterFlux}</li>
 * <li>{@link FirstFlux}</li>
 * <li>{@link GroupFlux}</li>
 * <li>TODO - histogram</li>
 * <li>{@link IntegralFlux}</li>
 * <li>{@link JoinFlux}</li>
 * <li>{@link KeepFlux}</li>
 * <li>{@link LastFlux}</li>
 * <li>{@link LimitFlux}</li>
 * <li>TODO - LinearBuckets</li>
 * <li>TODO - LogrithmicBuckets</li>
 * <li>{@link MapFlux}</li>
 * <li>{@link MaxFlux}</li>
 * <li>{@link MeanFlux}</li>
 * <li>{@link MinFlux}</li>
 * <li>percentile - Not defined in documentation or SPEC</li>
 * <li>TODO - pivot</li>
 * <li>{@link RangeFlux}</li>
 * <li>{@link RenameFlux}</li>
 * <li>{@link SampleFlux}</li>
 * <li>{@link SetFlux}</li>
 * <li>{@link ShiftFlux}</li>
 * <li>{@link SkewFlux}</li>
 * <li>{@link SortFlux}</li>
 * <li>{@link SpreadFlux}</li>
 * <li>stateTracking - Not defined in documentation or SPEC</li>
 * <li>{@link StddevFlux}</li>
 * <li>{@link SumFlux}</li>
 * <li>TODO - to</li>
 * <li>{@link ToBoolFlux}</li>
 * <li>{@link ToIntFlux}</li>
 * <li>{@link ToFloatFlux}</li>
 * <li>{@link ToDurationFlux}</li>
 * <li>{@link ToStringFlux}</li>
 * <li>{@link ToTimeFlux}</li>
 * <li>{@link ToUIntFlux}</li>
 * <li>{@link WindowFlux}</li>
 * <li>{@link YieldFlux}</li>
 * <li>toHttp - Not defined in documentation or SPEC</li>
 * <li>toKafka - Not defined in documentation or SPEC</li>
 * <li>{@link ExpressionFlux}</li>
 * </ul>
 *
 * @author Jakub Bednar (bednar@github) (22/06/2018 10:16)
 * @since 1.0.0
 */
@SuppressWarnings({"FileLength"})
public abstract class Flux {

    protected OperatorProperties operatorProperties = OperatorProperties.of();

    /**
     * Get data from the specified database.
     *
     * @param bucket Bucket name
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String bucket) {
        Arguments.checkNonEmpty(bucket, "Bucket name");

        return new FromFlux().withPropertyValueEscaped("bucket", bucket);
    }

    /**
     * Get data from the specified database.
     *
     * @param bucket Bucket name
     * @param hosts  the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String bucket, @Nonnull final Collection<String> hosts) {
        Arguments.checkNonEmpty(bucket, "Bucket name");
        Arguments.checkNotNull(hosts, "Hosts are required");

        return new FromFlux()
                .withPropertyValueEscaped("bucket", bucket)
                .withPropertyValue("hosts", hosts);
    }

    /**
     * Get data from the specified database.
     *
     * @param bucket Bucket name
     * @param hosts  the Fluxd hosts
     * @return {@link FromFlux}
     */
    @Nonnull
    public static Flux from(@Nonnull final String bucket, @Nonnull final String[] hosts) {
        Arguments.checkNonEmpty(bucket, "Database name");
        Arguments.checkNotNull(hosts, "Hosts are required");

        return new FromFlux()
                .withPropertyValueEscaped("bucket", bucket)
                .withPropertyValue("hosts", hosts);
    }

    /**
     * Join two time series together on time and the list of tags.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link JoinFlux#withTable(String, Flux)}</li>
     * <li>{@link JoinFlux#withOn(String)}</li>
     * <li>{@link JoinFlux#withOn(String[])}</li>
     * <li>{@link JoinFlux#withOn(Collection)}</li>
     * <li>{@link JoinFlux#withMethod(String)}</li>
     * <li>{@link JoinFlux#withPropertyNamed(String)}</li>
     * <li>{@link JoinFlux#withPropertyNamed(String, String)}</li>
     * </ul>
     *
     * @return {@link JoinFlux}
     */
    @Nonnull
    public static JoinFlux join() {

        return new JoinFlux();
    }

    /**
     * Join two time series together on time and the list of tags.
     *
     * @param name1  table 1 name
     * @param table1 table 1 Flux script
     * @param name2  table 2 name
     * @param table2 table 2 Flux script
     * @param tag    tag key to join
     * @param method the type of join to be performed
     * @return {@link JoinFlux}
     */
    @Nonnull
    public static JoinFlux join(@Nonnull final String name1,
                                @Nonnull final Flux table1,
                                @Nonnull final String name2,
                                @Nonnull final Flux table2,
                                @Nonnull final String tag,
                                @Nonnull final String method) {

        return new JoinFlux()
                .withTable(name1, table1)
                .withTable(name2, table2)
                .withOn(tag)
                .withMethod(method);
    }

    /**
     * It will return a table containing only columns that are specified.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link KeepFlux#withColumns(String[])}</li>
     * <li>{@link KeepFlux#withFunction(String)}</li>
     * <li>{@link KeepFlux#withPropertyNamed(String)}</li>
     * <li>{@link KeepFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link KeepFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link KeepFlux}
     */
    @Nonnull
    public final KeepFlux keep() {
        return new KeepFlux(this);
    }

    /**
     * It will return a table containing only columns that are specified.
     *
     * @param columns The list of columns that should be included in the resulting table.
     * @return {@link KeepFlux}
     */
    @Nonnull
    public final KeepFlux keep(@Nonnull final Collection<String> columns) {
        return new KeepFlux(this).withColumns(columns);
    }

    /**
     * It will return a table containing only columns that are specified.
     *
     * @param columns The list of columns that should be included in the resulting table.
     * @return {@link DropFlux}
     */
    @Nonnull
    public final KeepFlux keep(@Nonnull final String[] columns) {
        return new KeepFlux(this).withColumns(columns);
    }

    /**
     * It will return a table containing only columns that are specified.
     *
     * @param function The function which takes a column name as a parameter and returns a boolean indicating whether
     *                 or not the column should be included in the resulting table.
     * @return {@link DropFlux}
     */
    @Nonnull
    public final KeepFlux keep(@Nonnull final String function) {
        return new KeepFlux(this).withFunction(function);
    }

    /**
     * Counts the number of results.
     *
     * @return {@link CountFlux}
     */
    @Nonnull
    public final CountFlux count() {
        return new CountFlux(this);
    }

    /**
     * Counts the number of results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link CountFlux}
     */
    @Nonnull
    public final CountFlux count(final boolean useStartTime) {
        return new CountFlux(this)
                .withUseStartTime(useStartTime);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link CovarianceFlux#withColumns(String[])}</li>
     * <li>{@link CovarianceFlux#withColumns(Collection)}</li>
     * <li>{@link CovarianceFlux#withPearsonr(boolean)}</li>
     * <li>{@link CovarianceFlux#withValueDst(String)}</li>
     * <li>{@link CovarianceFlux#withPropertyNamed(String)}</li>
     * <li>{@link CovarianceFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link CovarianceFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance() {
        return new CovarianceFlux(this);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final Collection<String> columns) {
        return new CovarianceFlux(this).withColumns(columns);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final String[] columns) {
        return new CovarianceFlux(this).withColumns(columns);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns  list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @param pearsonr indicates whether the result should be normalized to be the Pearson R coefficient
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final Collection<String> columns, final boolean pearsonr) {
        return new CovarianceFlux(this).withColumns(columns).withPearsonr(pearsonr);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns  list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @param pearsonr indicates whether the result should be normalized to be the Pearson R coefficient
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final String[] columns, final boolean pearsonr) {
        return new CovarianceFlux(this).withColumns(columns).withPearsonr(pearsonr);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns  list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @param valueDst column into which the result will be placed.
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final Collection<String> columns,
                                           @Nonnull final String valueDst) {
        return new CovarianceFlux(this).withColumns(columns).withValueDst(valueDst);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns  list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @param valueDst column into which the result will be placed.
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final String[] columns,
                                           @Nonnull final String valueDst) {
        return new CovarianceFlux(this).withColumns(columns).withValueDst(valueDst);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns  list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @param pearsonr indicates whether the result should be normalized to be the Pearson R coefficient
     * @param valueDst column into which the result will be placed.
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final Collection<String> columns,
                                           final boolean pearsonr,
                                           @Nonnull final String valueDst) {
        return new CovarianceFlux(this).withColumns(columns).withPearsonr(pearsonr).withValueDst(valueDst);
    }

    /**
     * Covariance computes the covariance between two columns.
     *
     * @param columns  list of columns on which to compute the covariance. Exactly two columns must be provided.
     * @param pearsonr indicates whether the result should be normalized to be the Pearson R coefficient
     * @param valueDst column into which the result will be placed.
     * @return {@link CovarianceFlux}
     */
    @Nonnull
    public final CovarianceFlux covariance(@Nonnull final String[] columns,
                                           final boolean pearsonr,
                                           @Nonnull final String valueDst) {
        return new CovarianceFlux(this).withColumns(columns).withPearsonr(pearsonr).withValueDst(valueDst);
    }

    /**
     * Computes the time based difference between subsequent non null records.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link DerivativeFlux#withUnit(Long, ChronoUnit)}</li>
     * <li>{@link DerivativeFlux#withNonNegative(boolean)}</li>
     * <li>{@link DerivativeFlux#withColumns(String[])}</li>
     * <li>{@link DerivativeFlux#withTimeSrc(String)}</li>
     * <li>{@link DerivativeFlux#withPropertyNamed(String)}</li>
     * <li>{@link DerivativeFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link DerivativeFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link DerivativeFlux}
     */
    @Nonnull
    public final DerivativeFlux derivative() {
        return new DerivativeFlux(this);
    }

    /**
     * Computes the time based difference between subsequent non null records.
     *
     * @param duration the time duration to use for the result
     * @param unit     a {@code ChronoUnit} determining how to interpret the {@code duration} parameter
     * @return {@link DerivativeFlux}
     */
    @Nonnull
    public final DerivativeFlux derivative(@Nonnull final Long duration, @Nonnull final ChronoUnit unit) {
        return new DerivativeFlux(this).withUnit(duration, unit);
    }

    /**
     * Difference computes the difference between subsequent non null records.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link DifferenceFlux#withNonNegative(boolean)}</li>
     * <li>{@link DifferenceFlux#withColumns(String[])}</li>
     * <li>{@link DifferenceFlux#withColumns(Collection)}</li>
     * <li>{@link DifferenceFlux#withPropertyNamed(String)}</li>
     * <li>{@link DifferenceFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link DifferenceFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link DifferenceFlux}
     */
    @Nonnull
    public final DifferenceFlux difference() {
        return new DifferenceFlux(this);
    }

    /**
     * Difference computes the difference between subsequent non null records.
     *
     * @param nonNegative indicates if the derivative is allowed to be negative
     * @return {@link DifferenceFlux}
     */
    @Nonnull
    public final DifferenceFlux difference(final boolean nonNegative) {
        return new DifferenceFlux(this).withNonNegative(nonNegative);
    }

    /**
     * Difference computes the difference between subsequent non null records.
     *
     * @param columns list of columns on which to compute the difference
     * @return {@link DifferenceFlux}
     */
    @Nonnull
    public final DifferenceFlux difference(@Nonnull final Collection<String> columns) {
        return new DifferenceFlux(this).withColumns(columns);
    }

    /**
     * Difference computes the difference between subsequent non null records.
     *
     * @param columns list of columns on which to compute the difference
     * @return {@link DifferenceFlux}
     */
    @Nonnull
    public final DifferenceFlux difference(@Nonnull final String[] columns) {
        return new DifferenceFlux(this).withColumns(columns);
    }

    /**
     * Difference computes the difference between subsequent non null records.
     *
     * @param nonNegative indicates if the derivative is allowed to be negative
     * @param columns     list of columns on which to compute the difference
     * @return {@link DifferenceFlux}
     */
    @Nonnull
    public final DifferenceFlux difference(@Nonnull final Collection<String> columns, final boolean nonNegative) {
        return new DifferenceFlux(this).withColumns(columns).withNonNegative(nonNegative);
    }

    /**
     * Difference computes the difference between subsequent non null records.
     *
     * @param nonNegative indicates if the derivative is allowed to be negative
     * @param columns     list of columns on which to compute the difference
     * @return {@link DifferenceFlux}
     */
    @Nonnull
    public final DifferenceFlux difference(@Nonnull final String[] columns, final boolean nonNegative) {
        return new DifferenceFlux(this).withColumns(columns).withNonNegative(nonNegative);
    }

    /**
     * Distinct produces the unique values for a given column.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link DistinctFlux#withColumn(String)}</li>
     * <li>{@link DistinctFlux#withPropertyNamed(String)}</li>
     * <li>{@link DistinctFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link DistinctFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link DistinctFlux}
     */
    @Nonnull
    public final DistinctFlux distinct() {
        return new DistinctFlux(this);
    }

    /**
     * Distinct produces the unique values for a given column.
     *
     * @param column The column on which to track unique values.
     * @return {@link DistinctFlux}
     */
    @Nonnull
    public final DistinctFlux distinct(@Nonnull final String column) {
        return new DistinctFlux(this).withColumn(column);
    }

    /**
     * Drop will exclude specified columns from a table.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link DropFlux#withColumns(String[])}</li>
     * <li>{@link DropFlux#withFunction(String)}</li>
     * <li>{@link DropFlux#withPropertyNamed(String)}</li>
     * <li>{@link DropFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link DropFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link DropFlux}
     */
    @Nonnull
    public final DropFlux drop() {
        return new DropFlux(this);
    }

    /**
     * Drop will exclude specified columns from a table.
     *
     * @param columns The list of columns which should be excluded from the resulting table.
     * @return {@link DropFlux}
     */
    @Nonnull
    public final DropFlux drop(@Nonnull final Collection<String> columns) {
        return new DropFlux(this).withColumns(columns);
    }

    /**
     * Drop will exclude specified columns from a table.
     *
     * @param columns The list of columns which should be excluded from the resulting table.
     * @return {@link DropFlux}
     */
    @Nonnull
    public final DropFlux drop(@Nonnull final String[] columns) {
        return new DropFlux(this).withColumns(columns);
    }

    /**
     * Drop will exclude specified columns from a table.
     *
     * @param function The function which takes a column name as a parameter and returns a boolean indicating whether
     *                 or not the column should be excluded from the resulting table.
     * @return {@link DropFlux}
     */
    @Nonnull
    public final DropFlux drop(@Nonnull final String function) {
        return new DropFlux(this).withFunction(function);
    }

    /**
     * Returns the first result of the query.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link FilterFlux#withRestrictions(Restrictions)}</li>
     * <li>{@link FilterFlux#withPropertyNamed(String)}</li>
     * <li>{@link FilterFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link FilterFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link FilterFlux}
     */
    @Nonnull
    public final FilterFlux filter() {
        return new FilterFlux(this);
    }

    /**
     * Returns the first result of the query.
     *
     * @param restrictions filter restrictions
     * @return {@link FilterFlux}
     */
    @Nonnull
    public final FilterFlux filter(@Nonnull final Restrictions restrictions) {

        Arguments.checkNotNull(restrictions, "Restrictions are required");

        return new FilterFlux(this).withRestrictions(restrictions);
    }

    /**
     * Returns the first result of the query.
     *
     * @return {@link FirstFlux}
     */
    @Nonnull
    public final FirstFlux first() {
        return new FirstFlux(this);
    }

    /**
     * Returns the first result of the query.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link FirstFlux}
     */
    @Nonnull
    public final FirstFlux first(final boolean useStartTime) {
        return new FirstFlux(this)
                .withUseStartTime(useStartTime);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link GroupFlux#withBy(String[])}</li>
     * <li>{@link GroupFlux#withBy(Collection)}</li>
     * <li>{@link GroupFlux#withKeep(String[])}</li>
     * <li>{@link GroupFlux#withKeep(Collection)}</li>
     * <li>{@link GroupFlux#withExcept(String[])}</li>
     * <li>{@link GroupFlux#withExcept(Collection)}</li>
     * <li>{@link GroupFlux#withPropertyNamed(String)}</li>
     * <li>{@link GroupFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link GroupFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux group() {

        return new GroupFlux(this);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag name.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupBy(@Nonnull final String groupBy) {
        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this).withBy(groupBy);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupBy(@Nonnull final Collection<String> groupBy) {
        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this).withBy(groupBy);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @param keep    Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupBy(@Nonnull final Collection<String> groupBy, @Nonnull final Collection<String> keep) {
        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");
        Arguments.checkNotNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withBy(groupBy).withKeep(keep);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupBy(@Nonnull final String[] groupBy) {
        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");

        return new GroupFlux(this).withBy(groupBy);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param groupBy Group by these specific tag names.
     * @param keep    Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupBy(@Nonnull final String[] groupBy, @Nonnull final String[] keep) {
        Arguments.checkNotNull(groupBy, "GroupBy Columns are required");
        Arguments.checkNotNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withBy(groupBy).withKeep(keep);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupExcept(@Nonnull final Collection<String> except) {
        Arguments.checkNotNull(except, "GroupBy Except Columns are required");

        return new GroupFlux(this).withExcept(except);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @param keep   Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupExcept(@Nonnull final Collection<String> except,
                                       @Nonnull final Collection<String> keep) {

        Arguments.checkNotNull(except, "GroupBy Except Columns are required");
        Arguments.checkNotNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withExcept(except).withKeep(keep);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupExcept(@Nonnull final String[] except) {
        Arguments.checkNotNull(except, "GroupBy Except Columns are required");

        return new GroupFlux(this).withExcept(except);
    }

    /**
     * Groups results by a user-specified set of tags.
     *
     * @param except Group by all but these tag keys Cannot be used.
     * @param keep   Keep specific tag keys that were not in {@code groupBy} in the results.
     * @return {@link GroupFlux}
     */
    @Nonnull
    public final GroupFlux groupExcept(@Nonnull final String[] except, @Nonnull final String[] keep) {
        Arguments.checkNotNull(except, "GroupBy Except Columns are required");
        Arguments.checkNotNull(keep, "Keep Columns are required");

        return new GroupFlux(this).withExcept(except).withKeep(keep);
    }

    /**
     * For each aggregate column, it outputs the area under the curve of non null records.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link IntegralFlux#withUnit(Long, ChronoUnit)}</li>
     * <li>{@link IntegralFlux#withPropertyNamed(String)}</li>
     * <li>{@link IntegralFlux#withPropertyNamed(String, String)}</li>
     * </ul>
     *
     * @return {@link IntegralFlux}
     */
    @Nonnull
    public final IntegralFlux integral() {

        return new IntegralFlux(this);
    }

    /**
     * For each aggregate column, it outputs the area under the curve of non null records.
     *
     * @param duration Time duration to use when computing the integral
     * @param unit     a {@code ChronoUnit} determining how to interpret the {@code duration} parameter
     * @return {@link IntegralFlux}
     */
    @Nonnull
    public final IntegralFlux integral(@Nonnull final Long duration, @Nonnull final ChronoUnit unit) {

        Arguments.checkNotNull(duration, "Duration is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        return new IntegralFlux(this).withUnit(duration, unit);
    }

    /**
     * Returns the last result of the query.
     *
     * @return {@link LastFlux}
     */
    @Nonnull
    public final LastFlux last() {
        return new LastFlux(this);
    }

    /**
     * Returns the last result of the query.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link LastFlux}
     */
    @Nonnull
    public final LastFlux last(final boolean useStartTime) {
        return new LastFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Restricts the number of rows returned in the results.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link LimitFlux#withN(int)}</li>
     * <li>{@link LimitFlux#withPropertyNamed(String)}</li>
     * <li>{@link LimitFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link LimitFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link LimitFlux}
     */
    @Nonnull
    public final LimitFlux limit() {

        return new LimitFlux(this);
    }

    /**
     * Restricts the number of rows returned in the results.
     *
     * @param numberOfResults The number of results
     * @return {@link LimitFlux}
     */
    @Nonnull
    public final LimitFlux limit(final int numberOfResults) {

        return new LimitFlux(this).withN(numberOfResults);
    }

    /**
     * Applies a function to each row of the table.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link MapFlux#withFunction(String)}</li>
     * <li>{@link MapFlux#withPropertyNamed(String)}</li>
     * <li>{@link MapFlux#withPropertyNamed(String, String)}</li>
     * </ul>
     *
     * @return {@link MapFlux}
     */
    @Nonnull
    public final MapFlux map() {

        return new MapFlux(this);
    }

    /**
     * Applies a function to each row of the table.
     *
     * @param function The function for map row of table. Example: "r._value * r._value".
     * @return {@link MapFlux}
     */
    @Nonnull
    public final MapFlux map(@Nonnull final String function) {

        return new MapFlux(this).withFunction(function);
    }

    /**
     * Returns the max value within the results.
     *
     * @return {@link MaxFlux}
     */
    @Nonnull
    public final MaxFlux max() {
        return new MaxFlux(this);
    }

    /**
     * Returns the max value within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MaxFlux}
     */
    @Nonnull
    public final MaxFlux max(final boolean useStartTime) {
        return new MaxFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @return {@link MeanFlux}
     */
    @Nonnull
    public final MeanFlux mean() {
        return new MeanFlux(this);
    }

    /**
     * Returns the mean of the values within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MeanFlux}
     */
    @Nonnull
    public final MeanFlux mean(final boolean useStartTime) {
        return new MeanFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Returns the min value within the results.
     *
     * @return {@link MinFlux}
     */
    @Nonnull
    public final MinFlux min() {
        return new MinFlux(this);
    }

    /**
     * Returns the min value within the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link MinFlux}
     */
    @Nonnull
    public final MinFlux min(final boolean useStartTime) {
        return new MinFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Filters the results by time boundaries.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link RangeFlux#withStart(Instant)}</li>
     * <li>{@link RangeFlux#withStart(Long, ChronoUnit)}</li>
     * <li>{@link RangeFlux#withStop(Instant)}</li>
     * <li>{@link RangeFlux#withStop(Long, ChronoUnit)}</li>
     * <li>{@link RangeFlux#withPropertyNamed(String)}</li>
     * <li>{@link RangeFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link RangeFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link RangeFlux}
     */
    @Nonnull
    public final RangeFlux range() {

        return new RangeFlux(this);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @return {@link RangeFlux}
     */
    @Nonnull
    public final RangeFlux range(@Nonnull final Instant start) {
        Arguments.checkNotNull(start, "Start is required");

        return new RangeFlux(this).withStart(start);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @param stop  Specifies the exclusive newest time to be included in the results
     * @return {@link RangeFlux}
     */
    @Nonnull
    public final RangeFlux range(@Nonnull final Instant start, @Nonnull final Instant stop) {
        Arguments.checkNotNull(start, "Start is required");
        Arguments.checkNotNull(stop, "Stop is required");

        return new RangeFlux(this).withStart(start).withStop(stop);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} parameter
     * @return {@link RangeFlux}
     */
    @Nonnull
    public final RangeFlux range(@Nonnull final Long start, @Nonnull final ChronoUnit unit) {
        Arguments.checkNotNull(start, "Start is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        return new RangeFlux(this).withStart(start, unit);
    }

    /**
     * Filters the results by time boundaries.
     *
     * @param start Specifies the oldest time to be included in the results
     * @param stop  Specifies the exclusive newest time to be included in the results
     * @param unit  a {@code ChronoUnit} determining how to interpret the {@code start} and {@code stop} parameter
     * @return {@link RangeFlux}
     */
    @Nonnull
    public final RangeFlux range(@Nonnull final Long start, @Nonnull final Long stop, @Nonnull final ChronoUnit unit) {
        Arguments.checkNotNull(start, "Start is required");
        Arguments.checkNotNull(stop, "Stop is required");
        Arguments.checkNotNull(unit, "ChronoUnit is required");

        return new RangeFlux(this).withStart(start, unit).withStop(stop, unit);
    }

    /**
     * Rename will rename specified columns in a table.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link RenameFlux#withColumns(Map)} </li>
     * <li>{@link RenameFlux#withFunction(String)}</li>
     * <li>{@link RenameFlux#withPropertyNamed(String)}</li>
     * <li>{@link RenameFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link RenameFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link RenameFlux}
     */
    @Nonnull
    public final RenameFlux rename() {
        return new RenameFlux(this);
    }

    /**
     * Rename will rename specified columns in a table.
     *
     * @param columns The map of columns to rename and their corresponding new names.
     * @return {@link RenameFlux}
     */
    @Nonnull
    public final RenameFlux rename(@Nonnull final Map<String, String> columns) {
        return new RenameFlux(this).withColumns(columns);
    }

    /**
     * Rename will rename specified columns in a table.
     *
     * @param function The function which takes a single string parameter (the old column name) and
     *                 returns a string representing the new column name.
     * @return {@link RenameFlux}
     */
    @Nonnull
    public final RenameFlux rename(@Nonnull final String function) {
        return new RenameFlux(this).withFunction(function);
    }

    /**
     * Sample values from a table.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link SampleFlux#withN(int)}</li>
     * <li>{@link SampleFlux#withPos(int)}</li>
     * <li>{@link SampleFlux#withPropertyNamed(String)}</li>
     * <li>{@link SampleFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link SampleFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link SampleFlux}
     */
    @Nonnull
    public final SampleFlux sample() {

        return new SampleFlux(this);
    }

    /**
     * Sample values from a table.
     *
     * @param n Sample every Nth element.
     * @return {@link SampleFlux}
     */
    @Nonnull
    public final SampleFlux sample(final int n) {

        return new SampleFlux(this)
                .withN(n);
    }

    /**
     * Sample values from a table.
     *
     * @param n   Sample every Nth element.
     * @param pos Position offset from start of results to begin sampling. Must be less than @{code n}.
     * @return {@link SampleFlux}
     */
    @Nonnull
    public final SampleFlux sample(final int n, final int pos) {

        if (pos >= n) {
            throw new IllegalArgumentException("pos must be less than n");
        }

        return new SampleFlux(this)
                .withN(n)
                .withPos(pos);
    }

    /**
     * Assigns a static value to each record.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link SetFlux#withKeyValue(String, String)}</li>
     * <li>{@link SetFlux#withPropertyNamed(String)}</li>
     * <li>{@link SetFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link SetFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link SetFlux}
     */
    @Nonnull
    public final SetFlux set() {
        return new SetFlux(this);
    }

    /**
     * Assigns a static value to each record.
     *
     * @param key   label for the column. Has to be defined.
     * @param value value for the column. Has to be defined.
     * @return {@link SetFlux}
     */
    @Nonnull
    public final SetFlux set(@Nonnull final String key, @Nonnull final String value) {
        return new SetFlux(this).withKeyValue(key, value);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link ShiftFlux#withShift(Long, ChronoUnit)}</li>
     * <li>{@link ShiftFlux#withColumns(String[])}</li>
     * <li>{@link ShiftFlux#withColumns(Collection)} )}</li>
     * <li>{@link ShiftFlux#withPropertyNamed(String)}</li>
     * <li>{@link ShiftFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link ShiftFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public final ShiftFlux shift() {
        return new ShiftFlux(this);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * @param amount The amount to add to each time value
     * @param unit   a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public final ShiftFlux shift(@Nonnull final Long amount,
                                 @Nonnull final ChronoUnit unit) {

        return new ShiftFlux(this).withShift(amount, unit);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * @param amount  The amount to add to each time value
     * @param unit    a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @param columns The list of all columns that should be shifted.
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public final ShiftFlux shift(@Nonnull final Long amount,
                                 @Nonnull final ChronoUnit unit,
                                 @Nonnull final String[] columns) {

        return new ShiftFlux(this).withShift(amount, unit).withColumns(columns);
    }

    /**
     * Shift add a fixed duration to time columns.
     *
     * @param amount  The amount to add to each time value
     * @param unit    a {@code ChronoUnit} determining how to interpret the {@code amount} parameter
     * @param columns The list of all columns that should be shifted.
     * @return {@link ShiftFlux}
     */
    @Nonnull
    public final ShiftFlux shift(@Nonnull final Long amount,
                                 @Nonnull final ChronoUnit unit,
                                 @Nonnull final Collection<String> columns) {

        return new ShiftFlux(this).withShift(amount, unit).withColumns(columns);
    }

    /**
     * Skew of the results.
     *
     * @return {@link SkewFlux}
     */
    @Nonnull
    public final SkewFlux skew() {
        return new SkewFlux(this);
    }

    /**
     * Skew of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SkewFlux}
     */
    @Nonnull
    public final SkewFlux skew(final boolean useStartTime) {
        return new SkewFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @return {@link SortFlux}
     */
    @Nonnull
    public final SortFlux sort() {
        return new SortFlux(this);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param desc use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public final SortFlux sort(final boolean desc) {
        return new SortFlux(this).withDesc(desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @return {@link SortFlux}
     */
    @Nonnull
    public final SortFlux sort(@Nonnull final String[] columns) {
        Arguments.checkNotNull(columns, "Columns are required");

        return new SortFlux(this).withCols(columns);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @return {@link SortFlux}
     */
    @Nonnull
    public final SortFlux sort(@Nonnull final Collection<String> columns) {
        Arguments.checkNotNull(columns, "Columns are required");

        return new SortFlux(this).withCols(columns);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @param desc    use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public final SortFlux sort(@Nonnull final String[] columns, final boolean desc) {
        Arguments.checkNotNull(columns, "Columns are required");

        return new SortFlux(this)
                .withCols(columns)
                .withDesc(desc);
    }

    /**
     * Sorts the results by the specified columns Default sort is ascending.
     *
     * @param columns columns used to sort
     * @param desc    use the descending sorting
     * @return {@link SortFlux}
     */
    @Nonnull
    public final SortFlux sort(@Nonnull final Collection<String> columns, final boolean desc) {
        Arguments.checkNotNull(columns, "Columns are required");

        return new SortFlux(this)
                .withCols(columns)
                .withDesc(desc);
    }

    /**
     * Difference between min and max values.
     *
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public final SpreadFlux spread() {
        return new SpreadFlux(this);
    }

    /**
     * Difference between min and max values.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SpreadFlux}
     */
    @Nonnull
    public final SpreadFlux spread(final boolean useStartTime) {
        return new SpreadFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Standard Deviation of the results.
     *
     * @return {@link StddevFlux}
     */
    @Nonnull
    public final StddevFlux stddev() {
        return new StddevFlux(this);
    }

    /**
     * Standard Deviation of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link StddevFlux}
     */
    @Nonnull
    public final StddevFlux stddev(final boolean useStartTime) {
        return new StddevFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Sum of the results.
     *
     * @return {@link SumFlux}
     */
    @Nonnull
    public final SumFlux sum() {
        return new SumFlux(this);
    }

    /**
     * Sum of the results.
     *
     * @param useStartTime Use the start time as the timestamp of the resulting aggregate
     * @return {@link SumFlux}
     */
    @Nonnull
    public final SumFlux sum(final boolean useStartTime) {
        return new SumFlux(this).withUseStartTime(useStartTime);
    }

    /**
     * Convert a value to a bool.
     *
     * @return {@link ToBoolFlux}
     */
    @Nonnull
    public final ToBoolFlux toBool() {
        return new ToBoolFlux(this);
    }

    /**
     * Convert a value to a int.
     *
     * @return {@link ToIntFlux}
     */
    @Nonnull
    public final ToIntFlux toInt() {
        return new ToIntFlux(this);
    }

    /**
     * Convert a value to a float.
     *
     * @return {@link ToFloatFlux}
     */
    @Nonnull
    public final ToFloatFlux toFloat() {
        return new ToFloatFlux(this);
    }

    /**
     * Convert a value to a duration.
     *
     * @return {@link ToDurationFlux}
     */
    @Nonnull
    public final ToDurationFlux toDuration() {
        return new ToDurationFlux(this);
    }

    /**
     * Convert a value to a string.
     *
     * @return {@link ToStringFlux}
     */
    @Nonnull
    public final ToStringFlux toStringConvert() {
        return new ToStringFlux(this);
    }

    /**
     * Convert a value to a time.
     *
     * @return {@link ToTimeFlux}
     */
    @Nonnull
    public final ToTimeFlux toTime() {
        return new ToTimeFlux(this);
    }

    /**
     * Convert a value to a uint.
     *
     * @return {@link ToUIntFlux}
     */
    @Nonnull
    public final ToUIntFlux toUInt() {
        return new ToUIntFlux(this);
    }

    /**
     * Groups the results by a given time range.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link WindowFlux#withEvery(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withPeriod(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withOffset(Long, ChronoUnit)}</li>
     * <li>{@link WindowFlux#withOffset(Instant)}</li>
     * <li>{@link WindowFlux#withColumn(String)}</li>
     * <li>{@link WindowFlux#withStartCol(String)}</li>
     * <li>{@link WindowFlux#withStartCol(String)}</li>
     * <li>{@link WindowFlux#withPropertyNamed(String)}</li>
     * <li>{@link WindowFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link WindowFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window() {
        return new WindowFlux(this);
    }

    /**
     * Groups the results by a given time range.
     *
     * @param every     duration of time between windows
     * @param everyUnit a {@code ChronoUnit} determining how to interpret the {@code every}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window(@Nonnull final Long every,
                                   @Nonnull final ChronoUnit everyUnit) {

        Arguments.checkNotNull(every, "Every is required");
        Arguments.checkNotNull(everyUnit, "Every ChronoUnit is required");

        return new WindowFlux(this).withEvery(every, everyUnit);
    }

    /**
     * Groups the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window(@Nonnull final Long every,
                                   @Nonnull final ChronoUnit everyUnit,
                                   @Nonnull final Long period,
                                   @Nonnull final ChronoUnit periodUnit) {

        Arguments.checkNotNull(every, "Every is required");
        Arguments.checkNotNull(everyUnit, "Every ChronoUnit is required");

        Arguments.checkNotNull(period, "Period is required");
        Arguments.checkNotNull(periodUnit, "Period ChronoUnit is required");

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit);
    }

    /**
     * Groups the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param offset     The offset duration relative to the location offset
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window(@Nonnull final Long every,
                                   @Nonnull final ChronoUnit everyUnit,
                                   @Nonnull final Long period,
                                   @Nonnull final ChronoUnit periodUnit,
                                   @Nonnull final Instant offset) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withOffset(offset);
    }

    /**
     * Groups the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param offset     The offset duration relative to the location offset
     * @param offsetUnit a {@code ChronoUnit} determining how to interpret the {@code offset}
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window(@Nonnull final Long every,
                                   @Nonnull final ChronoUnit everyUnit,
                                   @Nonnull final Long period,
                                   @Nonnull final ChronoUnit periodUnit,
                                   @Nonnull final Long offset,
                                   @Nonnull final ChronoUnit offsetUnit) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withOffset(offset, offsetUnit);
    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param offset     The offset duration relative to the location offset
     * @param offsetUnit a {@code ChronoUnit} determining how to interpret the {@code offset}
     * @param timeColumn name of the time column to use
     * @param startCol   name of the column containing the window start time
     * @param stopCol    name of the column containing the window stop time
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window(@Nonnull final Long every,
                                   @Nonnull final ChronoUnit everyUnit,
                                   @Nonnull final Long period,
                                   @Nonnull final ChronoUnit periodUnit,
                                   @Nonnull final Long offset,
                                   @Nonnull final ChronoUnit offsetUnit,
                                   @Nonnull final String timeColumn,
                                   @Nonnull final String startCol,
                                   @Nonnull final String stopCol) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withOffset(offset, offsetUnit)
                .withColumn(timeColumn)
                .withStartCol(startCol)
                .withStopCol(stopCol);

    }

    /**
     * Partitions the results by a given time range.
     *
     * @param every      duration of time between windows
     * @param everyUnit  a {@code ChronoUnit} determining how to interpret the {@code every}
     * @param period     duration of the windowed partition
     * @param periodUnit a {@code ChronoUnit} determining how to interpret the {@code period}
     * @param offset     The offset duration relative to the location offset
     * @param timeColumn name of the time column to use
     * @param startCol   name of the column containing the window start time
     * @param stopCol    name of the column containing the window stop time
     * @return {@link WindowFlux}
     */
    @Nonnull
    public final WindowFlux window(@Nonnull final Long every,
                                   @Nonnull final ChronoUnit everyUnit,
                                   @Nonnull final Long period,
                                   @Nonnull final ChronoUnit periodUnit,
                                   @Nonnull final Instant offset,
                                   @Nonnull final String timeColumn,
                                   @Nonnull final String startCol,
                                   @Nonnull final String stopCol) {

        return new WindowFlux(this)
                .withEvery(every, everyUnit)
                .withPeriod(period, periodUnit)
                .withOffset(offset)
                .withColumn(timeColumn)
                .withStartCol(startCol)
                .withStopCol(stopCol);
    }

    /**
     * Yield a query results to yielded results.
     *
     * <h3>The parameters had to be defined by:</h3>
     * <ul>
     * <li>{@link YieldFlux#withName(String)}</li>
     * <li>{@link YieldFlux#withPropertyNamed(String)}</li>
     * <li>{@link YieldFlux#withPropertyNamed(String, String)}</li>
     * <li>{@link YieldFlux#withPropertyValueEscaped(String, String)}</li>
     * </ul>
     *
     * @return {@link YieldFlux}
     */
    @Nonnull
    public final YieldFlux yield() {
        return new YieldFlux(this);
    }

    /**
     * Yield a query results to yielded results.
     *
     * @param name The unique name to give to yielded results. Has to be defined.
     * @return {@link YieldFlux}
     */
    @Nonnull
    public final YieldFlux yield(@Nonnull final String name) {
        return new YieldFlux(this).withName(name);
    }

    /**
     * Write the custom Flux expression.
     *
     * @param expression flux expression
     * @return {@link ExpressionFlux}
     */
    @Nonnull
    public final ExpressionFlux expression(@Nonnull final String expression) {

        Arguments.checkNonEmpty(expression, "Expression");

        return new ExpressionFlux(this, expression);
    }

    /**
     * Create new operator with type {@code type}.
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .operator(FilterMeasurement.class)
     *          .withName("cpu")
     *      .sum();
     * </pre>
     *
     * @param type operator type
     * @param <F>  operator type
     * @return operator with {@code type}
     */
    @Nonnull
    public final <F extends AbstractParametrizedFlux> F operator(@Nonnull final Class<F> type) {

        Arguments.checkNotNull(type, "Operator type is required");

        try {
            return type.getConstructor(Flux.class).newInstance(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Add named property to current operator.
     *
     * <pre>
     *  FluxChain fluxChain = new FluxChain()
     *      .withPropertyNamed("every", 15, ChronoUnit.MINUTES)
     *      .withPropertyNamed("period", 20L, ChronoUnit.SECONDS)
     *      .withPropertyNamed("start", -50, ChronoUnit.DAYS)
     *      .withPropertyNamed("round", 1L, ChronoUnit.HOURS);
     *
     *  Flux flux = Flux.from("telegraf")
     *      .window()
     *          .withPropertyNamed("every")
     *          .withPropertyNamed("period")
     *          .withPropertyNamed("start")
     *          .withPropertyNamed("round")
     *      .sum();
     *
     * flux.print(fluxChain);
     * </pre>
     *
     * @param property name in Flux query and in named properties
     * @return a current operator.
     */
    @Nonnull
    public final Flux withPropertyNamed(@Nonnull final String property) {
        return withPropertyNamed(property, property);
    }

    /**
     * Add named property to current operator.
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .limit()
     *          .withPropertyNamed("n", "limit")
     *      .sum();
     *
     * FluxChain fluxChain = new FluxChain()
     *      .withPropertyNamed("limit", 15);
     *
     * flux.print(fluxChain);
     * </pre>
     *
     * @param fluxName      name in Flux query
     * @param namedProperty name in named properties
     * @return a current operator
     */
    @Nonnull
    public final Flux withPropertyNamed(@Nonnull final String fluxName, @Nonnull final String namedProperty) {

        Arguments.checkNonEmpty(fluxName, "Flux property name");
        Arguments.checkNonEmpty(namedProperty, "Named property");

        this.operatorProperties.putPropertyNamed(fluxName, namedProperty);

        return this;
    }

    /**
     * Add property value to current operator.
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .limit()
     *          .withPropertyValue("n", 5)
     *      .sum();
     * </pre>
     *
     * @param propertyName name in Flux query
     * @param value        value of property. If null than ignored.
     * @return a current operator
     */
    @Nonnull
    public final Flux withPropertyValue(@Nonnull final String propertyName, @Nullable final Object value) {

        Arguments.checkNonEmpty(propertyName, "Flux property name");

        this.operatorProperties.putPropertyValue(propertyName, value);

        return this;
    }

    /**
     * Add string property value to current operator that will be quoted (value =&gt; "value").
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .window(5, ChronoUnit.MINUTES)
     *          .withPropertyValueEscaped("startCol", "differentCol")
     *      .sum();
     * </pre>
     *
     * @param property name of property in Flux query
     * @param amount   the amount of the duration, measured in terms of the unit, positive or negative
     * @param unit     the unit that the duration is measured in, must have an exact duration.  If null than ignored.
     * @return a current operator
     */
    @Nonnull
    public final Flux withPropertyValue(@Nonnull final String property,
                                        @Nullable final Long amount,
                                        @Nullable final ChronoUnit unit) {

        Arguments.checkNonEmpty(property, "Flux property name");

        this.operatorProperties.putPropertyValue(property, amount, unit);

        return this;
    }

    /**
     * Add string property value to current operator that will be quoted (value =&gt; "value").
     *
     * <pre>
     * Flux flux = Flux
     *      .from("telegraf")
     *      .window(5, ChronoUnit.MINUTES)
     *          .withPropertyValueEscaped("startCol", "differentCol")
     *      .sum();
     * </pre>
     *
     * @param property name of property in Flux query
     * @param value    value of property. If null than ignored.
     * @return a current operator
     */
    @Nonnull
    public final Flux withPropertyValueEscaped(@Nonnull final String property, @Nullable final String value) {

        Arguments.checkNonEmpty(property, "Flux property name");

        this.operatorProperties.putPropertyValueString(property, value);

        return this;
    }

    /**
     * Append actual Flux function to Flux query.
     *
     * @param parameters named parameters for Flux query
     * @param builder    Flux query chaing
     */
    public abstract void appendActual(@Nonnull final Map<String, Object> parameters,
                                      @Nonnull final StringBuilder builder);

    /**
     * Create the Flux query that can be executed by FluxService.
     *
     * @return Flux query
     */
    @Nonnull
    @Override
    public String toString() {
        return toString(new HashMap<>());
    }

    /**
     * Create the Flux query that can be executed by FluxService.
     *
     * @param parameters flux query named parameters
     * @return Flux query
     */
    @Nonnull
    public String toString(@Nonnull final Map<String, Object> parameters) {

        Arguments.checkNotNull(parameters, "Parameters are required");

        StringBuilder builder = new StringBuilder();

        appendActual(parameters, builder);

        return builder.toString();
    }
}