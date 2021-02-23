### Descrição

- No Kubernetes o followerWorker poderá conectar em qq instância
- Como é mantida conexão aberta com o cache já que o mesmo não é feito diretamente pelo IP?

Usar este parâmetro para subir o monitorWorker que irá consumir o proxy do k8s

-Djdk.tls.followerWorker.protocols=TLSv1.2

$ kubectl proxy --port=9090

### TODO

- (DONE) Tratar queda de conexão do followerWorker para não gerar erro infinitamente no server
- (DONE) Implementar o uso de um cache como Caffeine ou EhCache
- (DONE) Ter identificação de todas as instâncias e informações como hostname, ip, data de criação
- (DONE) Quando uma instância cair interromper as threads dos outros nós
- Controle pra saber se o nó pode receber requisições 
- Logar a thread nos logs
- Controle o TTL pelo tempo que falta. Se um key/value foi inserido no cache pra viver por 80 segundos e depois de 40 segundos
  outra instância subiu, logo ocorreu a replicação, mas o novo nó deve saber que o key/value deve viver por 40 segundos e não mais os 80 iniciais
- Verificar regra de replicação já que é possível ter uma chave e mesma sofrer atualização durante a replicação e mais antiga prevalecer (implementar um mecanismo para controlar por tempo) 
- Quando uma nova instância foi adicionada ao cluster, deve fazer a replicação do cache com o novo nó
- Implementar feature de TTL no cache
- Tratar queda de conexão do server para não gerar erro infinitamente no followerWorker
  - Tratamento no followerWorker para retentar quando o Server estiver fora
- Implementar cluster
  - Nodes acknowlegment
  - Sync dos dados entre os nós
- Quantidade de conexões (threads) vs hardware (processador e memória ram) vs response time
- Implementar comunicação entre clusters
- Verificar a possibilidade de implementar comunicação non-blocking com os clients
- Tratar melhor os shutdown das conexões
- Criar lib followerWorker e tratar melhor o estabelecimento da conexão, como reconexão, gracefully close
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

https://github.com/kubernetes-followerWorker/java/wiki
https://kubernetes.io/docs/reference/generated/kubernetes-api/v1.20/#watch-list-pod-v1-core
https://kubernetes.io/docs/tasks/inject-data-application/environment-variable-expose-pod-information/

https://www.geeksforgeeks.org/introducing-threads-socket-programming-java/