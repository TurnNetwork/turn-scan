## Deployment steps
### 1. Go to http://192.168.18.31:8080/job/TurnScan/job/Turn-job/build?delay=0sec and use the specified commitid to build scan-job
### 2. Download the built program package, upload it to the target deployment machine, and unzip it
### 3. Execute update.sql in the browser database to create an address table
### 4. Modify the application-prod.yml configuration file
#### 4.1. Modify mysql database link, user name, and password
#### 4.2. Modify chain ID and web3j link
#### 4.3. Modify the cron expression as needed
### 5. Start the program:
```
nohup java -jar scan-job*.jar --spring.profiles.active=prod &
```
