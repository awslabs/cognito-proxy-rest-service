package com.budilov.cognito.services.cognito

import com.amazonaws.auth.AWSCredentialsProviderChain
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.budilov.cognito.Properties
import com.budilov.cognito.PropertyFileConverter
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.*
import java.security.interfaces.RSAKey


/**
 * Created by Vladimir Budilov
 *
 * The API is documented here: http://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_Operations.html
 *
 */
class CognitoService {

    constructor(props: Properties) {
        properties = props
        cognitoUPClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(properties.regionName))
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build()
    }

    val properties: Properties
    private val logger = LoggerFactory.getLogger("CognitoService")
    private val cognitoUPClient: CognitoIdentityProviderClient

    /**
     *
     * Requires:
     * https://github.com/auth0/jwks-rsa-java
     *
     * Another option is to use this one:
     * https://github.com/jwtk/jjwt
     *
     */
    @Throws(Exception::class)
    fun isTokenValid(token: String): Boolean {
        val resource = javaClass.classLoader.getResource("jwks.json")
        val provider = UrlJwkProvider(resource)
        val jwk = provider.get(properties.jwtIdTokenKid)

        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAKey)
        val verifier = JWT.require(algorithm)
                .withIssuer("https://cognito-idp.${properties.regionName}.amazonaws.com/${properties.cognitoUserPoolId}")
                .build() //Reusable verifier instance
        val jwt = try {
            verifier.verify(token)
        } catch (e: Exception) {
            false
        }

        return (jwt != null)
    }

    fun getUsername(idToken: String):String {
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
                .clientId(properties.cognitoClientId)
                .userPoolId(properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
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
                .clientId(properties.cognitoClientId)
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
        val confirmSignupRequest = AdminConfirmSignUpRequest.builder().userPoolId(properties.cognitoUserPoolId).username(username).build()

        val response: Any = try {
            cognitoUPClient.adminConfirmSignUp(confirmSignupRequest)
        } catch (e: Exception) {
            logger.error("Couldn't confirm signup because ${e.message}")
            e.message ?: "Error"
        }

        return response
    }


    fun adminResetPassword(username: String): Any {
        val request = AdminResetUserPasswordRequest.builder().userPoolId(properties.cognitoUserPoolId).username(username).build()

        val response: Any = try {
            cognitoUPClient.adminResetUserPassword(request)
        } catch (e: Exception) {
            logger.error("Couldn't reset because ${e.message}")
            e.message ?: "Error"
        }

        return response
    }

    fun adminDeleteUser(username: String): Any {
        val request = AdminDeleteUserRequest.builder().userPoolId(properties.cognitoUserPoolId).username(username).build()

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
    val service = CognitoService(PropertyFileConverter.readCredentials())

    println("signup response: " + service.signUp(username = username, password = password))


    println("signIn response: " + service.signInNoSRP(username = username, password = password))
    println("confirmSignUp response: " + service.adminConfirmSignUp(username = username))
    println("signIn response: " + service.signInNoSRP(username = username, password = password))
}