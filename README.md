# deploy-spring-boot-admin
deploy-spring-boot-admin is a plugin for spring-boot-admin

## Screenshots

![DeployPage](/images/deployPage.png?raw=true)
*Page with deploy actions*

![DeployPage Edit](/images/deployEdit.png?raw=true)
*Page when edit deploy action*

![DeployPage After Execute](/images/deployPageAfterExecute.png?raw=true)
*Page after executing deploy actions*

## quick-start

Add below dependencies to your pom
```xml
    <dependency>
      <groupId>de.codecentric</groupId>
      <artifactId>spring-boot-admin-server</artifactId>
      <version>${spring.boot.admin.version}</version>
    </dependency>
    <dependency>
      <groupId>de.codecentric</groupId>
      <artifactId>spring-boot-admin-server-ui</artifactId>
      <version>${spring.boot.admin.version}</version>
    </dependency>
    <dependency>
      <groupId>io.github.jianzhichun</groupId>
      <artifactId>deploy-spring-boot-admin</artifactId>
      <version>0.1.0</version>
    </dependency>
```
Meanwhile, you can add spring-boot-starter-mail for mail-notification function
```xml
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
```

<p>

Add configuration to your application.yml
```yaml

spring.boot.admin:
  notify:
    mail:
      enabled: false
  url: http://localhost:8080
  deploy:
    # when project start will run the actions in bootstrap
    bootstrap:
      - foo
      - bar
    # charset should be charset in deployed server
    charset: gbk
    mail:
      enabled: false
      simpleMailMessage:
        from: xxxxx@gmail.com
        to:
          - xxxx@qq.com
        subject: test
        text: Hi, 
    # action means script in deployed server
    actions:
      foo: 
        -
          executable: ipconfig
      bar: 
        -
          executable: ping
          args:
            - wwww.baidu.com
        -
          executable: ipconfig
```
## thanks
[xigongdaEricyang](https://github.com/xigongdaEricyang)

## enjoy