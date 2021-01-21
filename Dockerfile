FROM openjdk:11.0.6-jre

ENV TZ=America/Sao_Paulo
RUN echo $TZ > /etc/timezone

WORKDIR /opt

COPY build/distributions/simplecache-*.zip simplecache.zip

RUN unzip simplecache.zip && \
    rm -rf *.zip && \
    mv simplecache-* simplecache

#ENTRYPOINT ["producer/bin/producer"]
CMD ["/bin/bash","simplecache/bin/simplecache"]