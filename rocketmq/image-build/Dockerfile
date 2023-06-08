#
# 通过 https://github.com/apache/rocketmq-docker 修改的
#
FROM --platform="linux/arm64"  eclipse-temurin:8u372-b07-jre

ARG user=rocketmq
ARG group=rocketmq
ARG uid=3000
ARG gid=3000

ARG version=5.1.1

ENV ROCKETMQ_VERSION ${version}

ENV ROCKETMQ_HOME  /home/rocketmq/rocketmq-${ROCKETMQ_VERSION}

EXPOSE 9876 10909 10911 10912 8080


RUN addgroup -gid ${gid} ${group} \
    && adduser --uid ${uid} --gid ${gid} ${user}

COPY --chown=${uid}:${gid} scripts/ ${ROCKETMQ_HOME}/bin/

# 需要提前将 runserver.sh 文件中添加上 JAVA_HOME=/opt/java/openjdk
COPY --chown=${uid}:${gid} rocketmq-${version}/ ${ROCKETMQ_HOME}

RUN chmod -R a+x ${ROCKETMQ_HOME}/bin/ \
     && export JAVA_OPT=" -Duser.home=/opt" \
     && sed -i 's/${JAVA_HOME}\/jre\/lib\/ext/${JAVA_HOME}\/jre\/lib\/ext:${JAVA_HOME}\/lib\/ext/' ${ROCKETMQ_HOME}/bin/tools.sh \
     && chown -R ${uid}:${gid} ${ROCKETMQ_HOME}

USER ${user}

WORKDIR ${ROCKETMQ_HOME}/bin

ENTRYPOINT ["./docker-entrypoint.sh"]