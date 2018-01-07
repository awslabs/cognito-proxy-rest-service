package com.budilov.cognito.services.cognito

import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.budilov.cognito.Properties
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.*
import java.net.URL
import java.security.interfaces.RSAKey
import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.JwkProvider




data class JwtKey(val alg: String, val e: String, val kid: String, val kty: String, val n: String, val use: String)

/**
 * Created by Vladimir Budilov
 *
 * The API is documented here:
 * http://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_Operations.html
 *
 */
class CognitoService {


    private val logger = LoggerFactory.getLogger("CognitoService")
    private val cognitoUPClient: CognitoIdentityProviderClient

    constructor() {
        cognitoUPClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(Properties.regionName))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build()

        // Download the JWKs from Cognito
    }

    fun getKidFromToken(token: String) {

    }

    @Throws(Exception::class)
    fun isTokenValid(token: String): Boolean {

        // Decode the key and set the kid
        val decodedJwtToken = JWT.decode(token)
        val kid = decodedJwtToken.keyId

        val http = UrlJwkProvider(URL(Properties.jwksUrl))
        // Let's cache the result from Cognito for the default of 10 hours
        val provider = GuavaCachedJwkProvider(http)
        val jwk = provider.get(kid)

        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAKey)
        val verifier = JWT.require(algorithm)
                .withIssuer(Properties.jwtTokenIssuer)
                .build() //Reusable verifier instance
        val jwt = try {
            verifier.verify(token)
        } catch (e: Exception) {
            false
        }

        return (jwt != null)
    }

    fun getUsername(idToken: String): String {
        return JWT.decode(idToken).getClaim("cognito:username").asString()
    }

    /**
     * Signs in a user without SRP with the provided [username] and [password]
     *
     * @return
     * {
    "AuthenticationResult": {
    "AccessToken": "string",
    "ExpiresIn": number,
    "IdToken": "string",
    "NewDeviceMetadata": {
    "DeviceGroupKey": "string",
    "DeviceKey": "string"
    },
    "RefreshToken": "string",
    "TokenType": "string"
    },
    "ChallengeName": "string",
    "ChallengeParameters": {
    "string" : "string"
    },
    "Session": "string"
    }
     */
    fun signInNoSRP(username: String, password: String): Any {
        val authParametersMap = mutableMapOf("USERNAME" to username, "PASSWORD" to password)
        logger.info("map: ${authParametersMap}")
        var authRequest = AdminInitiateAuthRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .userPoolId(Properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParametersMap)
                .build()

        val response: Any = try {
            cognitoUPClient.adminInitiateAuth(authRequest)
        } catch (e: Exception) {
            logger.error("Couldn't retrieve authToken because ${e.stackTrace}")
            """ {"responseType": "error", "message": "${e.message}"} """
        }

        return response
    }

    /**
     * http://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminInitiateAuth.html
     *
     * Response:
     * {
    "AuthenticationResult": {
    "AccessToken": "string",
    "ExpiresIn": number,
    "IdToken": "string",
    "NewDeviceMetadata": {
    "DeviceGroupKey": "string",
    "DeviceKey": "string"
    },
    "RefreshToken": "string",
    "TokenType": "string"
    },
    "ChallengeName": "string",
    "ChallengeParameters": {
    "string" : "string"
    },
    "Session": "string"
    }
     */
    fun adminRefreshTokens(refreshToken: String): Any {
        val authParametersMap = mutableMapOf("REFRESH_TOKEN" to refreshToken)
        logger.info("map: ${authParametersMap}")
        var authRequest = AdminInitiateAuthRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .userPoolId(Properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .authParameters(authParametersMap)
                .build()

        val response: Any = try {
            cognitoUPClient.adminInitiateAuth(authRequest)
        } catch (e: Exception) {
            logger.error("Couldn't retrieve authToken because ${e.stackTrace}")
            e.message ?: "Error"
        }

        return response
    }

    /**
     * @return
    {
    "CodeDeliveryDetails": {
    "AttributeName": "string",
    "DeliveryMedium": "string",
    "Destination": "string"
    },
    "UserConfirmed": boolean,
    "UserSub": "string"
    }
     */
    fun signUp(username: String, password: String): Any {
        logger.debug("entering function")
        val attr = AttributeType.builder().name("email").value(username).build()

        val signUpRequest = SignUpRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .username(username)
                .password(password)
                .userAttributes(attr)
                .build()
        val response = try {
            cognitoUPClient.signUp(signUpRequest)
        } catch (e: Exception) {
            logger.error("Couldn't signUp because ${e.message}")
            e.message ?: "Error"
        }

        return response
    }

    fun adminConfirmSignUp(username: String): Any {
        val confirmSignupRequest = AdminConfirmSignUpRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        val response: Any = try {
            cognitoUPClient.adminConfirmSignUp(confirmSignupRequest)
        } catch (e: Exception) {
            logger.error("Couldn't confirm signup because ${e.message}")
            e.message ?: "Error"
        }

        return response
    }


    fun adminResetPassword(username: String): Any {
        val request = AdminResetUserPasswordRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        val response: Any = try {
            cognitoUPClient.adminResetUserPassword(request)
        } catch (e: Exception) {
            logger.error("Couldn't reset because ${e.message}")
            e.message ?: "Error"
        }

        return response
    }

    fun adminDeleteUser(username: String): Any {
        val request = AdminDeleteUserRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        val response: Any = try {
            cognitoUPClient.adminDeleteUser(request)
        } catch (e: Exception) {
            logger.error("Couldn't delete user because ${e.message}")
            e.message ?: "Error"
        }

        return response
    }


}

fun main(args: Array<String>) {
    val username = "vladimirbudilov@budilov.com"
    val password = "SomethingInteresting23!"
    val service = CognitoService()

    println("signup response: " + service.signUp(username = username, password = password))

    println("signIn response: " + service.signInNoSRP(username = username, password = password))
    println("confirmSignUp response: " + service.adminConfirmSignUp(username = username))
    println("signIn response: " + service.signInNoSRP(username = username, password = password))
}