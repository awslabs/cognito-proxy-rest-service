### Why

[Serverless Application Model -- How-To](https://github.com/awslabs/serverless-application-model/blob/master/HOWTO.md)

##### Build your code
```
./gradlew jar
```

##### Package it
```
aws cloudformation package --template-file sam.yaml --s3-bucket code.budilovv > /tmp/deployment
```

##### Deploy it
```
aws cloudformation deploy --template-file /tmp/deployment --stack-name my-new-stack --capabilities CAPABILITY_IAM
```

##### Test the Flows

###### Sign Up
```
curl -XPOST 'https://ymq3q8axhj.execute-api.us-east-1.amazonaws.com/Prod/cognito/user/signup' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```

###### Sign In
```
curl -XPOST 'https://ymq3q8axhj.execute-api.us-east-1.amazonaws.com/Prod/cognito/user/signin' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```

###### Password Reset
```
curl -XPOST 'https://ymq3q8axhj.execute-api.us-east-1.amazonaws.com/Prod/cognito/signup' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```

###### SignUp
```
curl -XPOST 'https://ymq3q8axhj.execute-api.us-east-1.amazonaws.com/Prod/cognito/signup' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```

###### SignUp
```
curl -XPOST 'https://ymq3q8axhj.execute-api.us-east-1.amazonaws.com/Prod/cognito/signup' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```
