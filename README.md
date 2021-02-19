### Descrição

- No Kubernetes o client poderá conectar em qq instância
- Como é mantida conexão aberta com o cache já que o mesmo não é feito diretamente pelo IP?

Usar este parâmetro para subir o monitor que irá consumir o proxy do k8s

-Djdk.tls.client.protocols=TLSv1.2

### TODO

- (DONE) Tratar queda de conexão do client para não gerar erro infinitamente no server
- (DONE) Implementar o uso de um cache como Caffeine ou EhCache
- (DONE) Ter identificação de todas as instâncias e informações como hostname, ip, data de criação
- (DONE) Quando uma instância cair interromper as threads dos outros nós
- Controle pra saber se o nó pode receber requisições 
- Logar a thread nos logs
- Quando uma nova instância foi adicionada ao cluster, deve fazer a replicação do cache com o novo nó
- Implementar feature de TTL no cache
- Tratar queda de conexão do server para não gerar erro infinitamente no client
  - Tratamento no client para retentar quando o Server estiver fora
- Implementar cluster
  - Nodes acknowlegment
  - Sync dos dados entre os nós
- Quantidade de conexões (threads) vs hardware (processador e memória ram) vs response time
- Implementar comunicação entre clusters
- Verificar a possibilidade de implementar comunicação non-blocking com os clients
- Tratar melhor os shutdown das conexões
- Criar lib client e tratar melhor o estabelecimento da conexão, como reconexão, gracefully close
- Verificar tempo de startup das instâncias dos servers
- Definir hora de criação/atualização do dado no cache
- Definir ID na instância para ser compartilhado com as outras instâncias
- Deixar um pool de threads criado no startup para ser reutilizado conforme as outras instâncias forem criadas
- Inserir regra pra permitir que a instância já está apta pra receber conexões de clientes
  - Essa regra tem que considerar quado é feito um novo deployment onde o cache está vazio
  - E para novas instâncias se for o caso
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