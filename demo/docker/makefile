LACHESIS_VERSION=0.2.1
DUMMY_VERSION=0.2.1
WATCHER_VERSION=0.0.2

docker-all: lachesis-image dummy-image watcher-image

compile:
	cd ../.. && mvn package -DskipTests=true
	cp ../../target/jlachesis-$(LACHESIS_VERSION)-jar-with-dependencies.jar jlachesis-$(LACHESIS_VERSION).jar

lachesis-image: 
	docker build --no-cache=true -t quan8/jlachesis:$(LACHESIS_VERSION) -f Dockerfile.jlachesis .
	
dummy-image: 
	docker build --no-cache=true -t quan8/dummy:$(DUMMY_VERSION) -f Dockerfile.dummy .

watcher-image:
	docker build --no-cache=true -t quan8/watcher:$(WATCHER_VERSION) -t quan8/watcher:latest -f Dockerfile.watcher .

.PHONY: docker-all lachesis-image dummy-image watcher-image
