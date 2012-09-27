#!/bin/bash

echo "Need an authenticated box"

cachedir=~/mongo/download-cache
mongodir=~/mongo/mongo-workspace/common
mongoversion=2.0.6
authnode="authnode"
authport=27018
authreplicaset=gds-authreplica-set

function error_exit
{
    echo "FATAL: ${1:-"Unknown Error"}" 1>&2
    exit 1
}

# cleanup database files
rm -Rf $mongodir/db/$authnode

case `uname` in
	Linux)
		mongoFile=mongodb-linux-x86_64-${mongoversion}.tgz
		mongoPath=mongodb-linux-x86_64-${mongoversion}
		mongoOs="linux"
		;;

	Darwin)
		mongoFile=mongodb-osx-x86_64-${mongoversion}.tgz
		mongoPath=mongodb-osx-x86_64-${mongoversion}
		mongoOs="osx"
		;;

	*)
		echo "Unsupported OS"
		exit 1
esac

cp ./gds-auth-testing.key $mongodir/$mongoPath/gds-auth-testing.key
mkdir $mongodir/db/$authnode

echo "J5onghIFXsta4H9lDGwTZvVNqqHbiuigEczqhI5xB3A" > $mongodir/$mongoPath/gds-auth-testing.key

echo "Starting authenticated mongodb node"
$mongodir/$mongoPath/bin/mongod --dbpath $mongodir/db/$authnode \
	--logpath $mongodir/logs/mongodb-$authnode.log \
	-v \
	--smallfiles \
	--rest \
	--pidfilepath $mongodir/mongo$authnode.pid \
	--fork \
	--directoryperdb \
	--replSet $authreplicaset \
	--port $authport \
	--keyFile $mongodir/$mongoPath/gds-auth-testing.key

echo "Wait 5 secs to let the nodes start up"
for i in {1..5}
do
	echo $[5 - $i]
	sleep 1
done

echo "Init the replica set"
$mongodir/$mongoPath/bin/mongo localhost:$authport/admin --eval 'printjson(db.runCommand({"replSetInitiate" : {"_id" : "gds-authreplica-set", "members" : [ { "_id" : 1, "host" : "localhost:27018" }]}}))' || error_exit "Failed to init replica set"

echo "Wait 30 secs for the replica set"
for i in {1..30}
do
	echo $[30 - $i]
	sleep 1
done

echo "Create admin user"
$mongodir/$mongoPath/bin/mongo localhost:$authport --quiet ./setup-users.js
#--eval "db.addUser('admin', 'germany');"


