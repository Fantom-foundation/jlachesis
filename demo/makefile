nodes = 4
txs = 10

up: conf start watch

# build jlachesis and dummy docker images for local registry.
compile:
	cd docker && make compile

build :
	cd docker && make docker-all

# generate keys and peers.json file
conf : 
	rm -rf conf
	./scripts/build-conf.sh $(nodes)

# launch testnet
start:
	./scripts/run-testnet.sh $(nodes)

# monitor stats
watch: 
	./scripts/watch.sh $(nodes)

# stop testnet
stop: 
	./scripts/stop-testnet.sh

# open terminals to use the dummy chat application
demo: 
	./scripts/demo.sh $(nodes)

# send a bunch of transactions
bombard: 
	./scripts/bombard.sh $(nodes) $(txs)

.PHONY: up build conf start watch stop demo bombard
	