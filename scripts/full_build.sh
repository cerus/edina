#!/bin/bash

./scripts/generate_asm.sh
./scripts/asm_to_code.sh
./scripts/cleanup.sh
mvn clean package