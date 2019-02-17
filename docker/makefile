LACHESIS_VERSION=0.2.1
DUMMY_VERSION=0.2.1
WATCHER_VERSION=0.0.2

all: lachesis-image dummy-image watcher-image

demo: lachesis-image dummy-image watcher-image

compile:
	cd .. && mvn package -DskipTests=true
	cp ../target/jlachesis-$(LACHESIS_VERSION)-jar-with-dependencies.jar jlachesis-$(LACHESIS_VERSION).jar

lachesis-image: 
	docker build --no-cache=true -t quan8/jlachesis:$(LACHESIS_VERSION) -f lachesis/Dockerfile .
	
dummy-image: 
	docker build --no-cache=true -t quan8/dummy:$(DUMMY_VERSION) -f dummy/Dockerfile .

watcher-image:
	docker build --no-cache=true -t quan8/watcher:$(WATCHER_VERSION) -t quan8/watcher:latest watcher/

.PHONY: all demo lachesis-image dummy-image watcher-image
