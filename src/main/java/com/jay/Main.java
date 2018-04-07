package com.jay;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.kafka.*;
import org.apache.storm.kafka.trident.*;
import org.apache.storm.kafka.trident.mapper.FieldNameBasedTupleToKafkaMapper;
import org.apache.storm.kafka.trident.selector.DefaultTopicSelector;
import org.apache.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.trident.Stream;
import org.apache.storm.trident.TridentTopology;
import org.apache.storm.trident.operation.BaseFunction;
import org.apache.storm.trident.operation.TridentCollector;
import org.apache.storm.trident.testing.FixedBatchSpout;
import org.apache.storm.trident.tuple.TridentTuple;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;

import java.util.Properties;

/**
 * Created by Administrator on 2018/4/6.
 */
public class Main {

    public static void main(String[] args) throws InterruptedException, InvalidTopologyException, AuthorizationException, AlreadyAliveException {

        ZkHosts hosts = new ZkHosts("localhost:2181");
        TridentKafkaConfig config = new TridentKafkaConfig(hosts, "test");
        config.scheme = new SchemeAsMultiScheme(new StringKeyValueScheme());
        // Consume new data from the topic
        config.startOffsetTime = kafka.api.OffsetRequest.LatestTime();

        TridentTopology topology = new TridentTopology();
        TridentKafkaConfig tridentKafkaConfig = new TridentKafkaConfig(hosts, "test");
        /**
         * 支持事物,支持失败重发
         *
         */
        TransactionalTridentKafkaSpout transactionalTridentKafkaSpout = new TransactionalTridentKafkaSpout(tridentKafkaConfig);
        topology.newStream("name", transactionalTridentKafkaSpout)
                .shuffle()
                .each(new Fields("bytes"), new BaseFunction() {
                    @Override
                    public void execute(TridentTuple tuple, TridentCollector collector) {
                        System.out.println(new String(tuple.getBinary(0)));
                        collector.emit(tuple);
                    }
                }, new Fields("sentence"))
                .parallelismHint(5);

        Config con = new Config();
        LocalCluster cluster=new LocalCluster();
        cluster.submitTopology("XXX", con,topology.build());

    }
}
