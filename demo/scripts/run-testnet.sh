#!/bin/bash

set -eux

N=${1:-4}
MPWD=$(pwd)

docker network create \
  --driver=bridge \
  --subnet=182.88.0.0/16 \
  --ip-range=182.88.8.0/24 \
  --gateway=182.88.8.254 \
  jlachesisnet

for i in $(seq 1 $N)
do
    docker create --name=node$i --net=jlachesisnet --ip=182.88.8.$i quan8/jlachesis:0.2.1 \
    	/bin/sh -c \
    		"java -cp jlachesis-0.2.1.jar lachesis.LachesisMain" \
	    --cache_size=50000 \
	    --tcp_timeout=200 \
	    --heartbeat=10 \
	    --node_addr="182.88.8.$i:1337" \
	    --proxy_addr="182.88.8.$i:1338" \
	    --client_addr="182.88.8.$(($N+$i)):1339" \
	    --service_addr="182.88.8.$i:80" \
	    --sync_limit=500 \
	    --store="inmem"
    docker cp $MPWD/conf/node$i node$i:.jlachesis
    docker start node$i
    #docker start node$i /bin/sh -c "java -cp jlachesis-0.2.1.jar lachesis.KeygenMain"

    docker run -d --name=client$i --net=jlachesisnet --ip=182.88.8.$(($N+$i)) -it quan8/dummy:0.2.1 \
	    /bin/sh -c \
	    	"java -cp jlachesis-0.2.1.jar dummy.DummyMain" \
	    --name="client$i" \
	    --client_addr="182.88.8.$(($N+$i)):1339" \
	    --proxy_addr="182.88.8.$i:1338" \
	    --log_level="info"
done
