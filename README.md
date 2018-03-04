# blockchain
blockchain - 一个简单的区块链与比特币的实现，包含区块链与比特币的一些基础特性，如去中心化，P2P通讯，比特币交易， 挖矿，共识算法，比特币钱包等

### Quick start
```
git clone https://github.com/aaronrao/blockchain.git
cd blockchain
mvn clean install
java -jar blockchain.jar 8081 7001
java -jar blockchain.jar 8082 7002 ws://localhost:7001

```


### HTTP API

- 查询区块链

  ```
  curl http://localhost:8081/chain

  ```
- 创建钱包

  ```
  curl http://localhost:8081/wallet/create

  ```
- 挖矿

  ```
  curl http://localhost:8081/mine

  ```

- 转账交易

  ```
  curl http://localhost:8081/transactions/new

  ```
- 查询钱余额

  ```
  curl http://localhost:8081/wallet/balance/get?address=518522f475ab591cf55d5f79bef629a0

  ```