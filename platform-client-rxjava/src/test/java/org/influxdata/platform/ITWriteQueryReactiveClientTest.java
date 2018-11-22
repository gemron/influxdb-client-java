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
package org.influxdata.platform;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

import org.influxdata.flux.domain.FluxRecord;
import org.influxdata.platform.annotations.Column;
import org.influxdata.platform.annotations.Measurement;
import org.influxdata.platform.domain.Authorization;
import org.influxdata.platform.domain.Bucket;
import org.influxdata.platform.domain.Permission;
import org.influxdata.platform.domain.RetentionRule;
import org.influxdata.platform.domain.User;
import org.influxdata.platform.option.WriteOptions;
import org.influxdata.platform.write.Point;
import org.influxdata.platform.write.event.WriteSuccessEvent;

import io.reactivex.Flowable;
import io.reactivex.Maybe;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.reactivestreams.Publisher;

/**
 * @author Jakub Bednar (bednar@github) (22/11/2018 06:59)
 */
@RunWith(JUnitPlatform.class)
class ITWriteQueryReactiveClientTest extends AbstractITPlatformClientTest {

    private WriteClientReactive writeClient;
    private QueryClientReactive queryClient;

    private Bucket bucket;

    @BeforeEach
    void setUp() throws Exception {

        super.setUp();


        PlatformClient platformClient = PlatformClientFactory.create(platformURL, "my-user",
                "my-password".toCharArray());

        RetentionRule retentionRule = new RetentionRule();
        retentionRule.setType("expire");
        retentionRule.setEverySeconds(3600L);

        bucket = platformClient.createBucketClient()
                .createBucket(generateName("h2o"), retentionRule, "my-org");

        //
        // Add Permissions to read and write to the Bucket
        //
        String bucketResource = Permission.bucketResource(bucket.getId());

        Permission readBucket = new Permission();
        readBucket.setResource(bucketResource);
        readBucket.setAction(Permission.READ_ACTION);

        Permission writeBucket = new Permission();
        writeBucket.setResource(bucketResource);
        writeBucket.setAction(Permission.WRITE_ACTION);

        User loggedUser = platformClient.createUserClient().me();
        Assertions.assertThat(loggedUser).isNotNull();

        Authorization authorization = platformClient.createAuthorizationClient()
                .createAuthorization(loggedUser, Arrays.asList(readBucket, writeBucket));

        String token = authorization.getToken();

        platformClient.close();
        this.platformClient.close();
        this.platformClient = PlatformClientReactiveFactory.create(platformURL, token.toCharArray());
        queryClient = this.platformClient.createQueryClient();
    }

    @AfterEach
    void tearDown() throws Exception {

        if (writeClient != null) {
            writeClient.close();
        }

        platformClient.close();
    }

    @Test
    void writeRecord() {

        String bucketName = bucket.getName();

        writeClient = platformClient.createWriteClient(WriteOptions.DISABLED_BATCHING);

        String lineProtocol = "h2o_feet,location=coyote_creek level\\ water_level=1.0 1";
        Maybe<String> record = Maybe.just(lineProtocol);

        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> {

                    Assertions.assertThat(event).isNotNull();
                    Assertions.assertThat(event.getBucket()).isEqualTo(bucket.getName());
                    Assertions.assertThat(event.getOrganization()).isEqualTo("my-org");
                    Assertions.assertThat(event.getLineProtocol()).isEqualTo(lineProtocol);

                    countDownLatch.countDown();
                });

        writeClient.writeRecord(bucketName, "my-org", ChronoUnit.NANOS, record);

        waitToCallback();

        Assertions.assertThat(countDownLatch.getCount()).isEqualTo(0);

        Flowable<FluxRecord> result = queryClient.query("from(bucket:\"" + bucketName + "\") |> range(start: 0) |> last()", "my-org");

        result.test().assertValueCount(1).assertValue(fluxRecord -> {

            Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
            Assertions.assertThat(fluxRecord.getValue()).isEqualTo(1.0D);
            Assertions.assertThat(fluxRecord.getField()).isEqualTo("level water_level");
            Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.ofEpochSecond(0, 1));

            return true;
        });
    }

    @Test
    void writeRecordEmpty() {

        writeClient = platformClient.createWriteClient(WriteOptions.DISABLED_BATCHING);

        writeClient.writeRecord(bucket.getName(), "my-org", ChronoUnit.SECONDS, Maybe.empty());
    }

    @Test
    void writeRecords() {

        writeClient = platformClient.createWriteClient(WriteOptions.DISABLED_BATCHING);

        Publisher<String> records = Flowable.just(
                "h2o_feet,location=coyote_creek level\\ water_level=1.0 1",
                "h2o_feet,location=coyote_creek level\\ water_level=2.0 2");

        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writeRecords(bucket.getName(), "my-org", ChronoUnit.NANOS, records);

        waitToCallback();

        Flowable<FluxRecord> results = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0)", "my-org");

        results.test()
                .assertValueCount(2)
                .assertValueAt(0, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(1.0D);
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("level water_level");
                    Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.ofEpochSecond(0, 1));

                    return true;
                })
                .assertValueAt(1, fluxRecord -> {

                    Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
                    Assertions.assertThat(fluxRecord.getValue()).isEqualTo(2.0D);
                    Assertions.assertThat(fluxRecord.getField()).isEqualTo("level water_level");
                    Assertions.assertThat(fluxRecord.getTime()).isEqualTo(Instant.ofEpochSecond(0, 2));

                    return true;
                });
    }

    @Test
    void writePoint() {

        writeClient = platformClient.createWriteClient();

        Instant now = Instant.now();
        Point point = Point.name("h2o_feet").addTag("location", "south").addField("water_level", 1).time(now, ChronoUnit.MILLIS);

        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writePoint(bucket.getName(), "my-org", Maybe.just(point));

        waitToCallback();

        Flowable<FluxRecord> result = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0) |> last()", "my-org");

        result.test().assertValueCount(1).assertValue(fluxRecord -> {

            Assertions.assertThat(fluxRecord.getMeasurement()).isEqualTo("h2o_feet");
            Assertions.assertThat(fluxRecord.getValue()).isEqualTo(1L);
            Assertions.assertThat(fluxRecord.getField()).isEqualTo("water_level");
            Assertions.assertThat(fluxRecord.getValueByKey("location")).isEqualTo("south");
            Assertions.assertThat(fluxRecord.getTime()).isEqualTo(now);

            return true;
        });
    }

    @Test
    void writePoints() {

        writeClient = platformClient.createWriteClient();

        countDownLatch = new CountDownLatch(2);

        Instant time = Instant.now();

        Point point1 = Point.name("h2o_feet").addTag("location", "west").addField("water_level", 1).time(time, ChronoUnit.MILLIS);
        Point point2 = Point.name("h2o_feet").addTag("location", "west").addField("water_level", 2).time(time.plusMillis(10), ChronoUnit.SECONDS);

        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writePoints(bucket.getName(), "my-org", (Publisher<Point>) Flowable.just(point1, point2));

        waitToCallback();

        Flowable<FluxRecord> result = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0)", "my-org");

        result
                .test()
                .assertValueCount(2);
    }

    @Test
    void writeMeasurement() {

        H2O measurement = new H2O();
        measurement.location = "coyote_creek";
        measurement.level = 2.927;
        measurement.time = Instant.now();

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writeMeasurement(bucket.getName(), "my-org", ChronoUnit.NANOS, Maybe.just(measurement));

        waitToCallback();

        Flowable<H2O> measurements = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0) |> last() |> rename(columns:{_value: \"water_level\"})", "my-org", H2O.class);

        measurements.test().assertValueCount(1).assertValueAt(0, h2O -> {

            Assertions.assertThat(h2O.location).isEqualTo("coyote_creek");
            Assertions.assertThat(h2O.description).isNull();
            Assertions.assertThat(h2O.level).isEqualTo(2.927);
            Assertions.assertThat(h2O.time).isEqualTo(measurement.time);

            return true;
        });
    }

    @Test
    void writeMeasurements() {

        Instant now = Instant.now();

        H2O measurement1 = new H2O();
        measurement1.location = "coyote_creek";
        measurement1.level = 2.927;
        measurement1.time = now;

        H2O measurement2 = new H2O();
        measurement2.location = "bohemia";
        measurement2.level = 3.927;
        measurement2.time = now.plusSeconds(1);

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writeMeasurements(bucket.getName(), "my-org", ChronoUnit.NANOS, (Publisher<H2O>) Flowable.just(measurement1, measurement2));

        waitToCallback();

        Flowable<H2O> measurements = queryClient.query("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0) |> sort(desc: false, columns:[\"_time\"]) |>rename(columns:{_value: \"water_level\"})", "my-org", H2O.class);

        measurements
                .test()
                .assertValueCount(2)
                .assertValueAt(1, h2O -> {

                    Assertions.assertThat(h2O.location).isEqualTo("coyote_creek");
                    Assertions.assertThat(h2O.description).isNull();
                    Assertions.assertThat(h2O.level).isEqualTo(2.927);
                    Assertions.assertThat(h2O.time).isEqualTo(measurement1.time);

                    return true;
                })
                .assertValueAt(0, h2O -> {

                    Assertions.assertThat(h2O.location).isEqualTo("bohemia");
                    Assertions.assertThat(h2O.description).isNull();
                    Assertions.assertThat(h2O.level).isEqualTo(3.927);
                    Assertions.assertThat(h2O.time).isEqualTo(measurement2.time);

                    return true;
                });
    }

    @Test
    void queryRaw() {

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writeRecord(bucket.getName(), "my-org", ChronoUnit.NANOS, Maybe.just("h2o_feet,location=coyote_creek level\\ water_level=1.0 1"));

        waitToCallback();

        Flowable<String> result = queryClient.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0) |> last()", "my-org");

        result.test()
                .assertValueCount(6)
                .assertValueAt(0, "#datatype,string,long,dateTime:RFC3339,dateTime:RFC3339,dateTime:RFC3339,double,string,string,string")
                .assertValueAt(1, "#group,false,false,true,true,false,false,true,true,true")
                .assertValueAt(2, "#default,_result,,,,,,,,")
                .assertValueAt(3, ",result,table,_start,_stop,_time,_value,_field,_measurement,location")
                .assertValueAt(4, value -> {

                    Assertions.assertThat(value).endsWith("1970-01-01T00:00:00.000000001Z,1,level water_level,h2o_feet,coyote_creek");

                    return true;
                })
                .assertValueAt(5, "");
    }

    @Test
    void queryRawDialect() {

        writeClient = platformClient.createWriteClient();
        writeClient
                .listenEvents(WriteSuccessEvent.class)
                .subscribe(event -> countDownLatch.countDown());

        writeClient.writeRecord(bucket.getName(), "my-org", ChronoUnit.NANOS, Maybe.just("h2o_feet,location=coyote_creek level\\ water_level=1.0 1"));

        waitToCallback();

        Flowable<String> result = queryClient.queryRaw("from(bucket:\"" + bucket.getName() + "\") |> range(start: 0) |> last()", null, "my-org");

        result.test()
                .assertValueCount(3)
                .assertValueAt(0, ",result,table,_start,_stop,_time,_value,_field,_measurement,location")
                .assertValueAt(1, value -> {

                    Assertions.assertThat(value).endsWith("1970-01-01T00:00:00.000000001Z,1,level water_level,h2o_feet,coyote_creek");

                    return true;
                })
                .assertValueAt(2, "");
    }

    @Measurement(name = "h2o")
    public static class H2O {

        @Column(name = "location", tag = true)
        String location;

        @Column(name = "water_level")
        Double level;

        @Column(name = "level description")
        String description;

        @Column(name = "time", timestamp = true)
        Instant time;
    }
}