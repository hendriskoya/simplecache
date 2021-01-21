### Descrição

- No Kubernetes o client poderá conectar em qq instância
- Como é mantida conexão aberta com o cache já que o mesmo não é feito diretamente pelo IP?

### TODO

- (DONE) Tratar queda de conexão do client para não gerar erro infinitamente no server
- Tratar queda de conexão do server para não gerar erro infinitamente no client
  - Tratamento no client para retentar quando o Server estiver fora
- Implementar cluster
  - Nodes acknowlegment
  - Sync dos dados entre os nós
- Quantidade de conexões (threads) vs hardware (processador e memória ram) vs response time
- Quando uma nova instância foi adicionada ao cluster, deve fazer a replicação do cache com o novo nó
- Implementar o uso de um cache como Caffeine ou EhCache
- Implementar comunicação entre clusters
- Verificar a possibilidade de implementar comunicação non-blocking com os clients

{
  "command": "SET",
  "key": "",
  "value": ""
}

{
  "command": "GET",
  "key": ""
}


{
  "command": "SET",
  "attributes": {
    "value": "Hendris",
    "key": "name"
  }
}

{
  "command": "GET",
  "attributes": {
    "key": "name"
  }
}

{
  "command": "GET_OUT",
  "attributes": {
    "key": "name",
    "value": "Hendris"
  }
}

https://github.com/kubernetes-client/java/wiki
https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#watch-list-pod-v1-core
https://kubernetes.io/docs/tasks/inject-data-application/environment-variable-expose-pod-information/

https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/