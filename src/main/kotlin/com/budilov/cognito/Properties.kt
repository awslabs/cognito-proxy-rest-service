package com.budilov.cognito

/**
 * @author Vladimir Budilov
 *
 * JWKs are used to verify that JWT tokens. Here's how to download your keys manually:
 * https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json
 *
 * I'm using environment variables for some property values. The default values are setup in sam.yaml
 *
 */
object Properties {

    val regionName: String = System.getenv("REGION_NAME")
    val cognitoUserPoolId: String = System.getenv("COGNITO_USER_POOL_ID")
    val cognitoAppClientId: String = System.getenv("COGNITO_APP_CLIENT_ID")
    val jwksUrl = "https://cognito-idp.$regionName.amazonaws.com/$cognitoUserPoolId/.well-known/jwks.json"
    val jwtTokenIssuer = "https://cognito-idp.$regionName.amazonaws.com/$cognitoUserPoolId"
}
