#!/bin/bash

read -p "Do you wish to cleanup? (y/n)" yn

if [ $yn = "y" ] ; then
	cp db/_empty.sqlite db/db.sqlite
	rm -r -f -d clones/*
	rm -r -f -d export/*
	touch export/_blacklist.txt
	
fi 
