FROM openjdk:11.0.6-jre

ENV TZ=America/Sao_Paulo
RUN echo $TZ > /etc/timezone

WORKDIR /opt

COPY build/distributions/server-*.zip server.zip

RUN unzip server.zip && \
    rm -rf *.zip && \
    mv server-* server

# ENTRYPOINT ["/opt/entrypoint.sh"]
# CMD ["/bin/bash","entrypoint.sh"]

CMD ["/bin/bash","server/bin/server"]