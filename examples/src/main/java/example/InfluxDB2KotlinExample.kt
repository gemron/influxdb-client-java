/**
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
package example

import com.influxdb.client.kotlin.InfluxDBClientKotlinFactory
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.filter
import kotlinx.coroutines.channels.take
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) = runBlocking {

    val influxDBClient = InfluxDBClientKotlinFactory
            .create("http://localhost:9999", "my-token".toCharArray())

    val fluxQuery = ("from(bucket: \"my-bucket\")\n"
            + " |> range(start: -1d)"
            + " |> filter(fn: (r) => (r[\"_measurement\"] == \"cpu\" and r[\"_field\"] == \"usage_system\"))")

    //Result is returned as a stream
    val results = influxDBClient.getQueryKotlinApi().query(fluxQuery, "my-org")

    //Example of additional result stream processing on client side
    results
            //filter on client side using `filter` built-in operator
            .filter { "cpu0" == it.getValueByKey("cpu") }
            //take first 20 records
            .take(20)
            //print results
            .consumeEach { println("Measurement: ${it.measurement}, value: ${it.value}") }

    influxDBClient.close()
}
