### What is it? 
It's a set of [AWS Lambda](https://aws.amazon.com/lambda/) functions that, once deployed using the provided [SAM](https://github.com/awslabs/serverless-application-model) template, act as 
an [Amazon Cognito](https://aws.amazon.com/cognito/) proxy. 

*Note: In most cases you should consider using the SDKs directly on the client, without using a proxy, especially 
if your business usecase allows it*

### Why was this project created? 
* QuickStart for any custom IdP --> Cognito migration service
* Serves as a guide on how to use the Cognito Admin Java SDK

[Serverless Application Model -- How-To](https://github.com/awslabs/serverless-application-model/blob/master/HOWTO.md)

### Deployment

The AWS Lambda functions use environment variables for easier deployments. These are the 
3 parameters that you will need to pass in: 


Property | Description 
--- | --- 
RegionParameter | the region where the Lambda functions will be deployed to 
CognitoUserPoolIdParameter | the Cognito User Pool Id 
CognitoAppClientIdParameter | the Cognito App Client Id
CognitoAutoconfirmUserParameter | Setting this value to 'true' will auto-confirm newly signed-up users


##### Build and deploy
```
# Build the code
./gradlew jar
# Package it
aws cloudformation package --template-file sam.yaml --s3-bucket code.budilovv --output-template-file /tmp/UpdatedSAMTemplate.yaml
# Deploy it
aws cloudformation deploy --template-file /tmp/UpdatedSAMTemplate.yaml --stack-name auth-stack --parameter-overrides RegionParameter=REGION CognitoUserPoolIdParameter=REGION_PGSbCVZ7S CognitoAppClientIdParameter=hikoo0i7jmt9lplrd2j0n9jqo --capabilities CAPABILITY_IAM

```

### Test the Flows

##### Sign Up
```
curl -XPOST 'https://API_GATEWAY_ID.execute-api.REGION.amazonaws.com/Prod/signup' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```

##### Sign In
```
curl -XPOST 'https://API_GATEWAY_ID.execute-api.REGION.amazonaws.com/Prod/signin' --header "username: test333@gmail.com" --header "password: Cognito&&1"
```

##### Password Reset
```
curl -XPOST 'https://API_GATEWAY_ID.execute-api.REGION.amazonaws.com/Prod/password/reset' --header "idToken: AAAAAAAAAAAAaa"
```

##### Refresh
```
curl -XPOST 'https://API_GATEWAY_ID.execute-api.REGION.amazonaws.com/Prod/refresh' --header "refreshToken: BBBBBBBBBBBBBBb"
```

##### Check for token validity
```
curl -XPOST 'https://API_GATEWAY_ID.execute-api.REGION.amazonaws.com/Prod/token/valid' --header "idToken: AAAAAAAAAAAAaa"
```

##### Delete User
```
curl -XDELETE 'https://API_GATEWAY_ID.execute-api.REGION.amazonaws.com/Prod/user' --header "idToken: AAAAAAAAAAAAaa"
```
