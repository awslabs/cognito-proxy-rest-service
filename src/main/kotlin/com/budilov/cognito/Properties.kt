package com.budilov.cognito

/**
 * Created by Vladimir Budilov
 * https://cognito-idp.{region}.amazonaws.com/{userPoolId}/.well-known/jwks.json -->
 *
 * https://cognito-idp.us-east-1.amazonaws.com/us-east-1_PGSbCVZ7S/.well-known/jwks.json
 */
object Properties {

    val regionName: String = System.getenv("REGION_NAME")
    val cognitoUserPoolId: String = System.getenv("COGNITO_USER_POOL_ID")
    val cognitoAppClientId: String = System.getenv("COGNITO_APP_CLIENT_ID")
    val jwksUrl = "https://cognito-idp.$regionName.amazonaws.com/$cognitoUserPoolId/.well-known/jwks.json"
    val jwtTokenIssuer = "https://cognito-idp.$regionName.amazonaws.com/$cognitoUserPoolId"
}
