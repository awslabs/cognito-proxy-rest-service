package com.budilov.cognito.services.cognito

import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.budilov.cognito.Properties
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.DefaultCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient
import software.amazon.awssdk.services.cognitoidentityprovider.model.*
import java.net.URL
import java.security.interfaces.RSAKey

data class AuthServiceResult(val successful: Boolean = true,
                             val result: Any? = null,
                             val errorMessage: String? = null,
                             val errorType: String? = null)

/**
 * @author Vladimir Budilov
 *
 * The API is documented here:
 * http://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_Operations.html
 *
 */
class CognitoService {


    private val logger = LoggerFactory.getLogger("CognitoService")
    private val cognitoUPClient: CognitoIdentityProviderClient = CognitoIdentityProviderClient.builder()
            .region(Region.of(Properties.regionName))
            .credentialsProvider(DefaultCredentialsProvider.builder().build())
            .build()

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
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminInitiateAuth.html
     *
     * @return the auth payload or throws an Exception
     */
    fun signInNoSRP(username: String, password: String): AuthServiceResult {
        val authParametersMap = mutableMapOf("USERNAME" to username, "PASSWORD" to password)
        val authRequest = AdminInitiateAuthRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .userPoolId(Properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParametersMap)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminInitiateAuth(authRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun signUp(username: String, password: String): AuthServiceResult {
        logger.debug("entering function")
        val attr = AttributeType.builder().name("email").value(username).build()

        val signUpRequest = SignUpRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .username(username)
                .password(password)
                .userAttributes(attr)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.signUp(signUpRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    /**
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminConfirmSignUp.html
     */
    fun adminConfirmSignUp(username: String): AuthServiceResult {
        val confirmSignupRequest = AdminConfirmSignUpRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminConfirmSignUp(confirmSignupRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    /**
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminConfirmSignUp.html
     */
    fun confirmSignUp(username: String, confirmationCode: String): AuthServiceResult {
        val confirmSignupRequest = ConfirmSignUpRequest.builder().clientId(Properties.cognitoAppClientId)
                .confirmationCode(confirmationCode)
                .username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.confirmSignUp(confirmSignupRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    /**
     *
     * https://docs.aws.amazon.com/cognito-user-identity-pools/latest/APIReference/API_AdminInitiateAuth.html
     */
    fun adminRefreshTokens(refreshToken: String): AuthServiceResult {
        val authParametersMap = mutableMapOf("REFRESH_TOKEN" to refreshToken)
        logger.info("map: ${authParametersMap}")
        val authRequest = AdminInitiateAuthRequest.builder()
                .clientId(Properties.cognitoAppClientId)
                .userPoolId(Properties.cognitoUserPoolId)
                .authFlow(AuthFlowType.REFRESH_TOKEN_AUTH)
                .authParameters(authParametersMap)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminInitiateAuth(authRequest))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun adminResetPassword(username: String): AuthServiceResult {
        val request = AdminResetUserPasswordRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminResetUserPassword(request))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun adminDeleteUser(username: String): AuthServiceResult {
        val request = AdminDeleteUserRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminDeleteUser(request))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun adminGetUser(username: String): AuthServiceResult {
        val request = AdminGetUserRequest.builder().userPoolId(Properties.cognitoUserPoolId).username(username).build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminGetUser(request))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun adminUpdateUserAttributes(username: String, userAttributes: Array<AttributeType>): AuthServiceResult {
        val request = AdminUpdateUserAttributesRequest.builder()
                .userPoolId(Properties.cognitoUserPoolId)
                .username(username)
                .userAttributes(*userAttributes)
                .build()

        return try {
            AuthServiceResult(successful = true, result = cognitoUPClient.adminUpdateUserAttributes(request))
        } catch (e: Exception) {
            AuthServiceResult(successful = false, errorMessage = ExceptionUtils.getRootCauseMessage(e), errorType = e.javaClass.simpleName)
        }
    }

    fun confirmEmailAddress(username: String) {
        CognitoService().adminUpdateUserAttributes(username, arrayOf(AttributeType.builder().name("email_verified").value("true").build()))
    }

}

fun main(args: Array<String>) {
    val username = "vladimir@budilov.com"
    val password = "Vovan)))1"
    val service = CognitoService()

    println("deleteUser body: " + service.adminDeleteUser(username))

    println("signup body: " + service.signUp(username = username, password = password))
    println("signIn body: " + service.signInNoSRP(username = username, password = password))
    println("adminGetUser: " + service.adminGetUser(username))
    println("adminUpdateEmail: " + service.confirmEmailAddress(username))
    println("adminGetUser: " + service.adminGetUser(username))
    println("adminResetPassword: " + service.adminResetPassword(username))
    println("signIn body: " + service.signInNoSRP(username = username, password = password))

//    println("confirmSignUp body: " + service.confirmSignUp(username = username, confirmationCode = "262580"))
//    println("adminConfirmSignUp body: " + service.adminConfirmSignUp(username = username))
//    println("deleteUser body: " + service.adminDeleteUser(username))

}