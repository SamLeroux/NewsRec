echo "Start zookeeper"
/home/sam/storm/zookeeper-3.4.6/bin/zkServer.sh start &
sleep 20
echo "Start nimbus"
/home/sam/storm/apache-storm-0.9.1-incubating/bin/storm nimbus &
sleep 20
echo "Start supervisor"
/home/sam/storm/apache-storm-0.9.1-incubating/bin/storm supervisor &
sleep 20
echo "Start ui"
/home/sam/storm/apache-storm-0.9.1-incubating/bin/storm ui &
sleep 20
ps -x
