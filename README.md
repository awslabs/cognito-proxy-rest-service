### What is it? 
It's a set of [AWS Lambda](https://aws.amazon.com/lambda/) functions that, once deployed using the provided [SAM](https://github.com/awslabs/serverless-application-model) template, act as 
an [Amazon Cognito](https://aws.amazon.com/cognito/) proxy. 

*Note: In most cases you should consider using the SDKs directly on the client side, without using a proxy, especially 
if your business use-case allows it*

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
aws cloudformation deploy --template-file /tmp/UpdatedSAMTemplate.yaml --stack-name auth-stack \ 
    --parameter-overrides \
        RegionParameter=REGION \
        CognitoUserPoolIdParameter=REGION_xxxxxxxxx \
        CognitoAppClientIdParameter=xxxxxxxxxxxxxxxxxxxxx \
        CognitoAutoconfirmUserParameter=true \
    --capabilities CAPABILITY_IAM

```

### Test the Flows

```
export SAMPLE_EMAIL=myemail@email.com
export SAMPLE_PASSWORD=myPassword**^1
export REGION=us-east-1
export API_GATEWAY_ID=

# Signup
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/signup' --header "username: $SAMPLE_EMAIL" --header "password: $SAMPLE_PASSWORD"

# SignIn
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/admin/signin' --header "username: $SAMPLE_EMAIL" --header "password: $SAMPLE_PASSWORD"

# Refresh Tokens
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/admin/refresh' --header "refreshToken: JWT_REFRESH_TOKEN"

# Check if the token is valid
curl -XGET 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/token/valid' --header "idToken: JWT_ID_TOKEN"

# Reset Password
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/password/reset' --header "username: $SAMPLE_EMAIL"

# Create a new password
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/password/confirm' --header "username: $SAMPLE_EMAIL" --header "password: $SAMPLE_PASSWORD" --header "confirmationCode: CONFIRMATION_CODE"

# Update User Attribute
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/admin/user/attribute' --header "idToken: JWT_ID_TOKEN" --header "attributeName: name" --header "attributeValue: Vladimir Budilov"

# Delete User
curl -XDELETE 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/admin/user' --header "idToken: JWT_ID_TOKEN" 

```

##### The following endpoints are only valid when MFA verification is turned on. Not valid for a seamless migration experience for customers, but still good to showcase this functionality 
```
# Resend Confirmation Code 
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/resendcode' --header "username: $SAMPLE_EMAIL"

# Confirm SignUp
curl -XPOST 'https://$API_GATEWAY_ID.execute-api.$REGION.amazonaws.com/Prod/confirmsignup' --header "username: $SAMPLE_EMAIL" --header "confirmationCode: CONFIRMATION_CODE"

```
