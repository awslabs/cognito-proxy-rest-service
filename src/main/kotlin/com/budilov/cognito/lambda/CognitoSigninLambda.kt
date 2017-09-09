package com.budilov.cognito.lambda

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.budilov.cognito.PropertyFileConverter
import com.budilov.cognito.services.cognito.CognitoService
import com.google.gson.Gson

class CognitoSigninLambda : RequestHandler<ApiGatewayRequest.Input,
        CognitoSigninLambda.AuthResponse> {

    data class AuthResponse(val statusCode: Int,
                            val body: String)

    val cognito = CognitoService(PropertyFileConverter.readCredentials())

    /**
     * 1. Get the request from API Gateway. Unmarshal (automatically) the request
     * 2. Get the
     */
    override fun handleRequest(request: ApiGatewayRequest.Input?,
                               context: Context?): AuthResponse? {

        val logger = context?.logger
        val username = request?.headers?.get("username")
        val password = request?.headers?.get("password")
        var status = 400
        val resultBody = if (username != null && password != null) {
            status = 200;
            Gson().toJson(cognito.signInNoSRP(username = username,
                    password = password))
        } else "Username and password are required"

        logger?.log("request payload: " + Gson().toJson(request))

        return AuthResponse(status, resultBody)
    }
}