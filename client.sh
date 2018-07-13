#!/bin/bash


for i in `seq 1 10000`;
do
	uuid=$(uuidgen)
	echo $i, $uuid
	curl http://localhost:8080/key/$uuid
done

curl http://localhost:8080/keys




