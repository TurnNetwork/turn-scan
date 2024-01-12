Environmental preparation

mysql/ES/redis/xxljob

1. Execute scan-v2.0.0.0.sql

2. Upload scan-agent, scan-api, scan-job, compile and package with docker

3. Modify configuration file

scan-agent


spring:
application:
name: scan-agent
#Data source configuration
datasource:
url: jdbc:mysql://192.168.31.114:12012/scan_${profile}?useUnicode=true&characterEncoding=utf-8&useSSL=false&useTimezone=true&serverTimezone=GMT%2B8&allowMultiQueries=true
username: 
password: 
#Redis key configuration
redis:
password: "ffca@sn!u934"
database: 0
cluster:
max-redirects: 3
nodes: 192.168.31.115:6381,192.168.31.115:6382,192.168.31.115:6383,192.168.31.115:6384,192.168.31.115:6385,192.168.31.115:6386
key:
#How many pieces of data can be stored at most?
max-item: 500000
#Block list
blocks: ${turn.redis-namespace}:blocks
#Transaction list
transactions: ${turn.redis-namespace}:transactions
#Statistics
networkStat: ${turn.redis-namespace}:networkStat
#Internal transfer transaction
transferTx: ${turn.redis-namespace}:transferTx
#erc20trade
erc20Tx: ${turn.redis-namespace}:erc20Tx
#erc721 Transaction
erc721Tx: ${turn.redis-namespace}:erc721Tx
#erc1155 Transaction
erc1155Tx: ${turn.redis-namespace}:erc1155Tx
#Joined games list
addrGames: ${turn.redis-namespace}:addrGames
#bubble information
bubbleInfo: ${turn.redis-namespace}:bubbleInfo
#ES index configuration
elasticsearch:
high-level-client:
hosts: 192.168.31.105:19200,192.168.31.105:29200,192.168.31.105:39200
port: 19200
schema: http
username:
password:
#Turn config
turn:
# Web3j access address
web3j:
#HTTPorWS
protocol: WS
addresses: 192.168.31.117:7790
xxl:
job:
admin:
#Deployment and address of dispatch center
addresses: http://192.168.31.110:18080/xxl-job-admin
#Dispatch center communication TOKEN
accessToken: qiliu123456
executor:
#ExecutorAppName
appname: turn-scan-agent
ip: 192.168.31.212


scan-api


#Spring application configuration
spring:
application:
name: scan-api
#Data source configuration
datasource:
url: jdbc:mysql://8.218.229.173:12010/scan_${profile}?useUnicode=true&characterEncoding=utf-8&useSSL=false&useTimezone=true&serverTimezone=GMT%2B8&allowMultiQueries=true
username: 
password: 
#Redis key configuration
redis:
password: ""
database: 0
cluster:
max-redirects: 3
nodes: 192.168.31.115:6381,192.168.31.115:6382,192.168.31.115:6383,192.168.31.115:6384,192.168.31.115:6385,192.168.31.115:6386
key:
#How many pieces of data can be stored at most?
max-item: 500000
#Block list
blocks: ${turn.redis-namespace}:blocks
#Transaction list
transactions: ${turn.redis-namespace}:transactions
#Statistics
networkStat: ${turn.redis-namespace}:networkStat
#Internal transfer transaction
transferTx: ${turn.redis-namespace}:transferTx
#erc20trade
erc20Tx: ${turn.redis-namespace}:erc20Tx
#erc721 Transaction
erc721Tx: ${turn.redis-namespace}:erc721Tx
#erc1155 Transaction
erc1155Tx: ${turn.redis-namespace}:erc1155Tx
#Joined games list
addrGames: ${turn.redis-namespace}:addrGames
#bubble information
bubbleInfo: ${turn.redis-namespace}:bubbleInfo
#ES index configuration
elasticsearch:
high-level-client:
hosts: 
port: 19200
schema: http
username:
password:
#Turn config
turn:
# Web3j access address
web3j:
#HTTPorWS
protocol: WS
addresses:
public/config.json Modify configuration related chain information

{
"context":"/browser-server",
"siteName":"Bubble",
"headerChainName":"Bubble Test",
"chainName":"Bubble",
"copyRight":"Bubble",
"logo":"/images/logo.svg",
"metamask":{
"title":

Unknown macro: { "en"}
,
"rpcUrl": "https://rpc-test.bubbonet.com", 
"chainId": "100",
"chainName":"Bubble Test",
"nativeCurrency":

Unknown macro: { "symbol"}
,
"blockExplorerUrl": ""
},
"links": [
],
"social":[
{
"order": 1,
"name": "wechat",
"logo": "",
"qrcode": "/images/wechat.jpg",
"url":

Unknown macro: {"cn"}
}
]
}









scan-job


spring:
application:
name: scan-job
#Data source configuration
datasource:
url: jdbc:mysql://192.168.31.114:12012/scan_${profile}?useUnicode=true&characterEncoding=utf-8&useSSL=false&useTimezone=true&serverTimezone=GMT%2B8&allowMultiQueries=true
username: 
password:  
#ES index configuration
elasticsearch:
high-level-client:
hosts: 192.168.31.105:19200,192.168.31.105:29200,192.168.31.105:39200
port: 19200
schema: http
username:
password:
# Currently activated configuration name (please do not modify it)
profile: turn
#Turn related configurations
turn:
#Address HRP prefix
addressPrefix: AA
# Amount display unit
valueUnit: AA
# Number of parallel decoding threads for transaction input parameters
txLogDecodeThreadNum: 200
# Web3j access address
web3j:
#HTTPorWS
protocol: WS
addresses: 192.168.31.117:7790
#chainid
chainId: 100
xxl:
job:
admin:
#Deployment and address of dispatch center
addresses: http://192.168.31.110:18080/xxl-job-admin
#Dispatch center communication TOKEN
accessToken: 
executor:
#ExecutorAppName
appname: turn-scan-job
IP: 192.168.31.212
#Executor port number
port: 9997


4. Start scan-agent, scan-api, scan-job in sequence

5. Start the xxljob scheduled task









scan-web


1. Pull the project http://192.168.31.12:10015/browser-web

2. Execute npm install in the project root directory

3. Modify the corresponding configuration file in the project root directory

          .env.production_test #Test environment

          .env.production_uat #uat environment

          .env.production #Formal environment

   Modify code

        VUE_APP_FAUCET_API_ROOT = "xxx" # xxx is the faucet access address

4. Execute the packaging command npm run build

5. Copy the contents of the dist folder in the root directory to the directory specified by the nginx configuration

6. Start nginx
              

       

