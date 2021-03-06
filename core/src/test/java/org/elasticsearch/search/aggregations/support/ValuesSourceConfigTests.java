/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.search.aggregations.support;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.SortedNumericDocValues;
import org.apache.lucene.util.BytesRef;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexService;
import org.elasticsearch.index.engine.Engine.Searcher;
import org.elasticsearch.index.fielddata.SortedBinaryDocValues;
import org.elasticsearch.index.query.QueryShardContext;
import org.elasticsearch.test.ESSingleNodeTestCase;

public class ValuesSourceConfigTests extends ESSingleNodeTestCase {

    public void testKeyword() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type",
                "bytes", "type=keyword");
        client().prepareIndex("index", "type", "1")
                .setSource("bytes", "abc")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Bytes> config = ValuesSourceConfig.resolve(
                    context, null, "bytes", null, null, null, null);
            ValuesSource.Bytes valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedBinaryDocValues values = valuesSource.bytesValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(new BytesRef("abc"), values.valueAt(0));
        }
    }

    public void testEmptyKeyword() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type",
                "bytes", "type=keyword");
        client().prepareIndex("index", "type", "1")
                .setSource("{ \"foo\":\"bar\" }")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Bytes> config = ValuesSourceConfig.resolve(
                    context, null, "bytes", null, null, null, null);
            ValuesSource.Bytes valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedBinaryDocValues values = valuesSource.bytesValues(ctx);
            values.setDocument(0);
            assertEquals(0, values.count());

            config = ValuesSourceConfig.resolve(
                    context, null, "bytes", null, "abc", null, null);
            valuesSource = config.toValuesSource(context);
            values = valuesSource.bytesValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(new BytesRef("abc"), values.valueAt(0));
        }
    }

    public void testUnmappedKeyword() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type");
        client().prepareIndex("index", "type", "1")
                .setSource("{ \"foo\":\"bar\" }")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);
            ValuesSourceConfig<ValuesSource.Bytes> config = ValuesSourceConfig.resolve(
                    context, ValueType.STRING, "bytes", null, null, null, null);
            ValuesSource.Bytes valuesSource = config.toValuesSource(context);
            assertNull(valuesSource);

            config = ValuesSourceConfig.resolve(
                    context, ValueType.STRING, "bytes", null, "abc", null, null);
            valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedBinaryDocValues values = valuesSource.bytesValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(new BytesRef("abc"), values.valueAt(0));
        }
    }

    public void testLong() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type",
                "long", "type=long");
        client().prepareIndex("index", "type", "1")
                .setSource("long", 42)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Numeric> config = ValuesSourceConfig.resolve(
                    context, null, "long", null, null, null, null);
            ValuesSource.Numeric valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedNumericDocValues values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(42, values.valueAt(0));
        }
    }

    public void testEmptyLong() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type",
                "long", "type=long");
        client().prepareIndex("index", "type", "1")
                .setSource("{ \"foo\":\"bar\" }")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Numeric> config = ValuesSourceConfig.resolve(
                    context, null, "long", null, null, null, null);
            ValuesSource.Numeric valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedNumericDocValues values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(0, values.count());

            config = ValuesSourceConfig.resolve(
                    context, null, "long", null, 42, null, null);
            valuesSource = config.toValuesSource(context);
            values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(42, values.valueAt(0));
        }
    }

    public void testUnmappedLong() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type");
        client().prepareIndex("index", "type", "1")
                .setSource("{ \"foo\":\"bar\" }")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Numeric> config = ValuesSourceConfig.resolve(
                    context, ValueType.NUMBER, "long", null, null, null, null);
            ValuesSource.Numeric valuesSource = config.toValuesSource(context);
            assertNull(valuesSource);

            config = ValuesSourceConfig.resolve(
                    context, ValueType.NUMBER, "long", null, 42, null, null);
            valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedNumericDocValues values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(42, values.valueAt(0));
        }
    }

    public void testBoolean() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type",
                "bool", "type=boolean");
        client().prepareIndex("index", "type", "1")
                .setSource("bool", true)
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Numeric> config = ValuesSourceConfig.resolve(
                    context, null, "bool", null, null, null, null);
            ValuesSource.Numeric valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedNumericDocValues values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(1, values.valueAt(0));
        }
    }

    public void testEmptyBoolean() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type",
                "bool", "type=boolean");
        client().prepareIndex("index", "type", "1")
                .setSource("{ \"foo\":\"bar\" }")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Numeric> config = ValuesSourceConfig.resolve(
                    context, null, "bool", null, null, null, null);
            ValuesSource.Numeric valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedNumericDocValues values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(0, values.count());

            config = ValuesSourceConfig.resolve(
                    context, null, "bool", null, true, null, null);
            valuesSource = config.toValuesSource(context);
            values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(1, values.valueAt(0));
        }
    }

    public void testUnmappedBoolean() throws Exception {
        IndexService indexService = createIndex("index", Settings.EMPTY, "type");
        client().prepareIndex("index", "type", "1")
                .setSource("{ \"foo\":\"bar\" }")
                .setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE)
                .get();

        try (Searcher searcher = indexService.getShard(0).acquireSearcher("test")) {
            QueryShardContext context = indexService.newQueryShardContext(0, searcher.reader(), () -> 42L);

            ValuesSourceConfig<ValuesSource.Numeric> config = ValuesSourceConfig.resolve(
                    context, ValueType.BOOLEAN, "bool", null, null, null, null);
            ValuesSource.Numeric valuesSource = config.toValuesSource(context);
            assertNull(valuesSource);

            config = ValuesSourceConfig.resolve(
                    context, ValueType.BOOLEAN, "bool", null, true, null, null);
            valuesSource = config.toValuesSource(context);
            LeafReaderContext ctx = searcher.reader().leaves().get(0);
            SortedNumericDocValues values = valuesSource.longValues(ctx);
            values.setDocument(0);
            assertEquals(1, values.count());
            assertEquals(1, values.valueAt(0));
        }
    }
}
