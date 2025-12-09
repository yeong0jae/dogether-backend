#!/bin/bash

# local.env 파일에서 환경 변수 로드
set -a
source local.env
set +a

# 애플리케이션 실행
java -jar build/libs/dogether.jar --spring.profiles.active=local --server.port=8080
