#!/bin/bash

N=${1:-4}

docker run -it --rm --name=watcher --net=jlachesisnet --ip=182.88.8.99 quan8/watcher /watch.sh $N
