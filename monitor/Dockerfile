FROM openjdk:11.0.6-jre

ENV TZ=America/Sao_Paulo
RUN echo $TZ > /etc/timezone

# ADD https://storage.googleapis.com/kubernetes-release/release/v1.15.2/bin/linux/amd64/kubectl /usr/local/bin/kubectl
# RUN chmod +x /usr/local/bin/kubectl

WORKDIR /opt

# COPY entrypoint.sh entrypoint.sh
# RUN chmod +x entrypoint.sh

COPY build/distributions/monitorWorker-*.zip monitorWorker.zip

RUN unzip monitorWorker.zip && \
    rm -rf *.zip && \
    mv monitorWorker-* monitorWorker

# ENTRYPOINT ["/opt/entrypoint.sh"]
# CMD ["/bin/bash","entrypoint.sh"]

CMD ["/bin/bash","monitorWorker/bin/monitorWorker"]