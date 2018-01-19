### Why
* During 
* Serves as a guide on how to use the Cognito Admin Java SDK
* QuickStart for any custom IdP--> Cognito migration backend

[Serverless Application Model -- How-To](https://github.com/awslabs/serverless-application-model/blob/master/HOWTO.md)

#### Deployment

The AWS Lambda functions use environment variables for easier deployments. These are the 
3 parameters that you will need to pass in: 

```
RegionParameter - the region where the Lambda functions will be deployed to
CognitoUserPoolIdParameter - the Cognito User Pool Id 
CognitoAppClientIdParameter - the Cognito App Client Id

```

##### Build and deploy
```
# Build the code
./gradlew jar
# Package it
aws cloudformation package --template-file sam.yaml --s3-bucket code.budilovv > /tmp/deployment
# Deploy it
aws cloudformation deploy --template-file /tmp/deployment --stack-name auth-stack --parameter-overrides RegionParameter=REGION CognitoUserPoolIdParameter=REGION_PGSbCVZ7S CognitoAppClientIdParameter=hikoo0i7jmt9lplrd2j0n9jqo --capabilities CAPABILITY_IAM

```

#### Test the Flows

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

#### CodePipeline
```
https://docs.aws.amazon.com/lambda/latest/dg/build-pipeline.html
```